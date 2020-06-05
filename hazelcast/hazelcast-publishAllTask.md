## Hazelcast PublishAllTask 实例数过多的问题

1. 在启动 CaaS 时，Hazelcast 也随之启动，并运行到以下代码。可知 Hazelcast 每1秒都会执行 new PublishAllTask() 的操作。这是 PublishAllTask 类唯一被使用的地发。

```java
metricsRegistry.scheduleAtFixedRate(new PublishAllTask(), 1, SECONDS);
```

2. PublishAllTask 类实现 Runnable，并在 run() 方法中，进行关于 channels 的循环。经过调试，channels 只有在形成集群时，才不为 0，才会进行循环。

   在循环中，再次加入线程去并将打算在线程中执行 publishMetrics() 方法。

   查看 publishMetrics() 方法中的代码，其中的代码只是在在处理的都是 long 类型的属性。

```java
private class PublishAllTask implements Runnable {
	@Override
    public void run() {
        for (NioChannel channel : channels) {
	        final NioInboundPipeline inboundPipeline = channel.inboundPipeline;
			NioThread inputThread = inboundPipeline.owner();
			if (inputThread != null) {
				inputThread.addTaskAndWakeup(new Runnable() {
					@Override
					public void run() {		                                    								inboundPipeline.publishMetrics();
					}
				});
			}

			final NioOutboundPipeline outboundPipeline = channel.outboundPipeline;
			NioThread outputThread = outboundPipeline.owner();
			if (outputThread != null) {
				outputThread.addTaskAndWakeup(new Runnable() {
					@Override
					public void run() {
						outboundPipeline.publishMetrics();
					}
				});
			}
		}
	}
}

@Override
protected void publishMetrics() {
	if (currentThread() != owner) {
		return;
	}

	owner.bytesTransceived += bytesWritten.get() - bytesWrittenLastPublish;
	owner.framesTransceived += normalFramesWritten.get() - normalFramesWrittenLastPublish;
	owner.priorityFramesTransceived += priorityFramesWritten.get() - priorityFramesWrittenLastPublish;
	owner.processCount += processCount.get() - processCountLastPublish;

    bytesWrittenLastPublish = bytesWritten.get();
	normalFramesWrittenLastPublish = normalFramesWritten.get();
    priorityFramesWrittenLastPublish = priorityFramesWritten.get();
    processCountLastPublish = processCount.get();
}
```

3. 第 2 点中 PublishAllTask() 方法中加入的线程，被存放到 Queue\<Runnable\> taskQueue 变量中，并在 processTaskQueue() 方法中被取用。经过调试，如果断点打在 task.run(); 上，CaaS 形成集群时，经过一段时间，可以发现 taskQueue 的 size 会堆积，但放行后会快速减少。

```java
private final Queue<Runnable> taskQueue = new ConcurrentLinkedQueue<Runnable>();

private boolean processTaskQueue() {
	boolean tasksProcessed = false;
    while (!stop) {
		Runnable task = taskQueue.poll();
	    if (task == null) {
	        break;
        }
	    task.run();
		completedTaskCount.inc();
	    tasksProcessed = true;
    }
	return tasksProcessed;
}
```

## Hazelcast出现大量PublishAllTask实例的情况研究

出现问题的机器是服务器 192.1.8.110，因为启动它时没有改动配置文件的原因，它的集群发现地址填写的是 192.1.8.111 的地址。同时，192.1.8.111 也启动着 CaaS（用于别的测试，111 在使用过程中会存在多次启动和关闭 CaaS），111 填写的发现地址是自身。

基于以上的回忆，对 Hazelcast 的集群行为进行里实验，**实验并没有使用 110 和 111 服务器，只是模拟上述的配置和启动关闭情况**，并有如下发现（下面仍以 110、111 为例说明）：

1. 当 111 先启动，110 后启动时，两者会从一开始就形成集群。这是因为 110 的集群发现地址填写的时 111，而 111 也启动着，所以很正常的形成里集群。
2. 当 110 先启动，111 后启动时，两者一开始并不能形成集群，并且出现 “We should merge to [192.1.8.110]:8199, both have the same data member count: 1” 的语句。**在一段时间后，发现两者合并了，形成了集群**。在进行的两次测试中，分布经过了 2 分钟和 5 分钟，才出现合并行为。

对于第二点的 “111后启动” ，也很好的符合了 “111 先启动 ==> 110 后启动 ==>两者形成集群 ==> 111 重启，111 从集群离开 ==> 两者合并，形成集群” 的流程。

一开始并不能复现问题，期间对 111 进行里多次的启动关闭，并在启动时对两个 CaaS 都进行了接口发送。第二天回来发现，出现了大量PublishAllTask实例的情况，情况得到复现。

![](/home/user/image/publishAllTask_1.png)

## 解决Hazelcast出现大量PublishAllTask实例问题

经过上面的复现，可以更加确定是 Hazelcast 出现的问题。我们为了进一步的研究，拉取了 Hazelcast 项目的源码，并在一些地方加入了对线程的命名以及日志以帮助我们定位问题和观察情况，**可我们并不能从该次测试中复现出大量PublishAllTask实例的问题**。

当我们困惑的时候，我们发现拉取的 Hazelcast 代码是**最新的 4.0.0 的快照版本**，然而当前 CaaS 使用的是 3.11.2 版本。这让我们考虑到版本所带有的 BUG。当我们调查 [Hazelcast 的历史版本更新说明时](<https://docs.hazelcast.org/docs/rn/index.html#3-11-2>)，在 3.11.3 版本（即当前 CaaS 使用的下一个版本）中发现了一个[提及到 PublishAllTask 的问题修复](<https://github.com/hazelcast/hazelcast/pull/14699>)。这使我们更加怀疑出现大量 PublishAllTask 实例问题是 3.11.2 版本带有的一个 BUG。

然后，我们将 Hazelcast 项目切换到 3.11.2 版本，并加入上面同样的线程命名和日志，进行了同样的测试：

1. 启动两个 CaaS，分别称为 A、B。其中 A 的集群发现地址配置自身地址，B 的集群发现地址配置为 A 的地址。先启动 A，后启动 B。
2. 向两个接口都发送普通加密接口。
3. 重启 A 数次。

经过上面的测试流程，我们发现问题果然再次出现：

![](/home/user/image/publishAllTask_2.png)

另外，我们还发现：

1. 该三项实例数的增长不受 GC 影响。GC 无法将它们回收，所以时间一长肯定会导致内存泄漏。
2. 该三项实例数的增长不受发送接口的影响。即使停止发送接口，依然继续增长。

至此，基本确定 3.11.2 版本存在出现大量 PublishAllTask实例的问题。接下来应该做的是对 Hazelcast 的**最新稳定版本 3.12.3** (该文档编写日期 20191012)  进行上面的测试，查看是否仍会出现这个的问题。

测试 Hazelcast 3.12.3 版本是否还会出现大量 PublishAllTask 实例问题，经过上面的测试流程，**没有发现 Hazelcast 的类出现大量实例数的情况**。

所以可以确定出现大量 PublishAllTask 实例数问题是 Hazelcast 3.11.2 版本自身带有的 BUG。CaaS 应该切换 Hazelcast 版本并更新到最新版本 3.12.3。



### 