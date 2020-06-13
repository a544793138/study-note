## 关于 Hazelcast 集群合并的行为研究

![](/home/user/image/hazelcast-cluster-merge.png)

以上是从[官方文档](https://docs.hazelcast.org/docs/latest/manual/html-single/#split-brain-recovery) 中截取的图段落，并进行里机翻。

从图片中发现，我们之前对 Hazelcast 集群合并的行为存在误解：我们误以为合并策略就是决定着集群合并的行为与合并方向。

但是，实际上，我们发现**合并策略只是用于处理集群合并后数据冲突的问题**，也就是说合并策略只是用于集群合并后的数据合并。

那么集群合并是谁决定的呢？

从图中文字看到，Hazelcast 中应该**只有一种固定的集群合并行为，就是对比分区的集群的成员数。成员数少的集群合并到成员数多的集群中**。**为了方便接下来的理解，我们这里将成员数少的集群称为源，将成员数多的集群称为目标**。

**当合并的集群的成员数相等时，由一个哈希算法来决定集群合并的行为。**

然后，我们从代码中找到了这个决定相同成员数集群合并行为的方法：

```java
// targetDataMemberCount == currentDataMemberCount
if (shouldMergeTo(node.getThisAddress(), joinMessage.getAddress())) {
	logger.info("We should merge to " + joinMessage.getAddress()
                    + ", both have the same data member count: " + currentDataMemberCount);
	return LOCAL_NODE_SHOULD_MERGE;
}

logger.info(joinMessage.getAddress() + " should merge to us "
	+ ", both have the same data member count: " + currentDataMemberCount);

    
private boolean shouldMergeTo(Address thisAddress, Address targetAddress) {
	String thisAddressStr = "[" + thisAddress.getHost() + "]:" + thisAddress.getPort();
    String targetAddressStr = "[" + targetAddress.getHost() + "]:" + targetAddress.getPort();

	if (thisAddressStr.equals(targetAddressStr)) {
        throw new IllegalArgumentException("Addresses should be different! This: "
                    + thisAddress + ", Target: " + targetAddress);
	}

    // Since strings are guaranteed to be different, result will always be non-zero.
	int result = thisAddressStr.compareTo(targetAddressStr);
    return result > 0;
}

public int compareTo(String var1) {
	int var2 = this.value.length;
    int var3 = var1.value.length;
	int var4 = Math.min(var2, var3);
    char[] var5 = this.value;
	char[] var6 = var1.value;

    for(int var7 = 0; var7 < var4; ++var7) {
		char var8 = var5[var7];
	    char var9 = var6[var7];
        if (var8 != var9) {
			return var8 - var9;
		}
	}

    return var2 - var3;
}
```

从上面代码中可以发现，**当两个集群的成员数相等时**，会有如下流程：

我们将这两个集群分别称为 A 、B。

A 的 IP : Port = 192.1.8.101:8199

B 的 IP : Port = 192.1.8.102:8199

1. Hazelcast 会对比 A、B 两个集群的 IP:Port 字符串
2. 当出现不相同的字符时，例如 A 中的 `1` 对比 B 中的 `2` 时，两者相减，1 - 2 < 0，这就说明 A 是目标，B 是 源。





B 合并到 A，B 称为源，A 称为目标

- DiscardMergePolicy - 丢弃源的全部缓存，使用目标的全部缓存。

- PassThroughMergePolicy - 如果数据合并冲突（目标和源都拥有相同的 key 的缓存），则使用源的缓存。**源中的其他缓存也会被保留到目标中。**
- ExpirationTimeMergePolicy - 对于数据合并冲突的缓存，对比源和目标的缓存的到期时间，谁的时间更长（说明缓存更新）就用谁的。**源中的其他缓存不会保留到目标中。**
- 









## 研究集群分区的相关事项

### 查看是否存在配置可以固定集群节点总数

经过查看官方的文档，并**未能找到关于固定集群节点总数的配置**。

我们是通过 TCP/IP 方法进行节点的发现并形成集群的。在浏览过程中发现，TCP/IP 方法还可以配置一种 `required-member`  的属性：

![](/home/user/Downloads/tcp_ip_required_member.png)

我们现在使用的是 `member` 属性，根据上面图片显示，`required-member`  属性更有利于集群成员的控制。

不过，配置了 `required-member`  属性后，集群将仅在此必须成员启动时才启动，如果没有该成员，集群将无法启动。而且，`required-member`  属性无法配置多个 IP 地址。

所以 `required-member`  属性的作用性也不大。

经上，Hazelcast 无法固定集群节点总数，在使用时应该将全部的成员 IP 地址填写到集群配置中。

### 查看 Hazelcast 的集群分区的处理方法

从 Hazelcast 文档得知，在 Hazelcast 中集群脑裂产生分区时，有一个被称为 “仲裁” 的功能发挥着作用。

> 术语“仲裁”仅指成功完成操作所需的群集中的成员数。它在Hazelcast中提供的机制可以在群集中的节点数降至指定节点数以下的情况下保护用户。

仲裁有以下功能：

1. 使用 `quorum-size` 属性**指定集群中集群保持运行状态所需的最小成员数**。如果成员数少于定于的最小值时，**所有对该集群的操作（读/写）都会被拒绝**，并返回 `QuorumException` 异常。
2. 使用 `quorum-type` 属性指定集群仲裁类型。当仲裁成功后，即**经过判断**当前集群的成员数大于指定的最小值（quorum-size）时，该集群允许的服务类型（读、写、读和写）。

在上述第二点中，提到的 “经过判断” 是指经过一些特定的方法进行判断当前集群的成员数。在 Hazelcast 中**内置三种**判断方法：

1. **Member Count Quorum**

   直接判断当前集群的成员数是否大于 `quorum-size` 。大于则为仲裁成功，否则失败。

   ```java
   @Override
   public boolean apply(Collection<Member> members) {
   	return members.size() >= quorumSize;
   }
   ```

2. **Probabilistic Quorum Function**

   通过一个 [Hazelcast 定义的故障检测器](<https://docs.hazelcast.org/docs/latest/manual/html-single/#phi-accrual-failure-detector>) 确定集群中存活的成员数，并判断该数量是否大于  `quorum-size` 。

   ```java
   @Override
   public boolean apply(Collection<Member> members) {
   	if (members.size() < quorumSize) {
   		return false;
   	}
   
       int count = 0;
       long timestamp = Clock.currentTimeMillis();
       for (Member member : members) {
           if (!isAlivePerIcmp(member)) {
   			continue;
   		}
   
   	    if (member.localMember() || failureDetector.isAlive(member, timestamp)) {
   			count++;
   		}
   	}
   	return count >= quorumSize;
   }
   ```

3. **Recently-Active Quorum Function**

   通过一个自定义的心跳最大空闲时间，判断集群中存活的成员数，并判断该数量是否大于  `quorum-size` 。

   ```java
   @Override
   public boolean apply(Collection<Member> members) {
   	if (members.size() < quorumSize) {
           return false;
   	}
   
       int count = 0;
       long now = currentTimeMillis();
       for (Member member : members) {
           if (!isAlivePerIcmp(member)) {
   			continue;
   		}
   
   	    if (member.localMember()) {
               count++;
   			continue;
   		}
   
           // apply and onHeartbeat are never executed concurrently
           Long latestTimestamp = latestHeartbeatPerMember.get(member);
           if (latestTimestamp == null) {
               continue;
           }
   
           if ((now - latestTimestamp) < heartbeatToleranceMillis) {
               count++;
           }
   	}
       return count >= quorumSize;
   }
   ```

经上所得，可以知道，Hazelcast 在处理集群分区时，并不需要让集群知道自己是大小集群，都是通过特定的方法判断成员数是否大于 `quorum-size` ，大于时才提供设置的读、写操作，否则拒绝任何请求。

### 测试集群中 `quorum-size` 的配置是否会共享

考虑总集群分区成两个集群，称为 A、B，当 A、B 配置的 `quorum-size` 属性不同时，会有什么样的情况。

测试：4个节点，分区成 A、B 两个集群，其中 A 集群的 `quorum-size` 配置为 2，B 集群配置为 3。

**结果：**A 集群正常运行，B 集群无法运行，报出下面的响应与异常

```json
// 响应
{
    "statusCode": 500,
    "error": "Internal Server Error",
    "requestId": "1571996284022:linux-102.local:106:1do14fd3m:1",
    "message": "Split brain protection exception: quorumRuleWithMembers has failed!"
}

// 控制台异常
2019-10-25 17:38:04.122  WARN 20076 --- [io2-8080-exec-5] c.k.w.c.f.a.ExceptionHandlerInterceptor  : Split brain protection exception: quorumRuleWithMembers has failed!

com.hazelcast.quorum.QuorumException: Split brain protection exception: quorumRuleWithMembers has failed!
...
```

经过上述测试，`quorum-size` 配置是个成员自身的属性，无法共享，也不会自动选用合适的值。

另外，关于动态配置仲裁属性，有如下文档描述：

`QuorumConfig`：无法动态添加新的仲裁配置，但其他配置可以引用现有静态配置中配置的仲裁



使用另一种仲裁方法验证，一样无法共享 `quorum-size` 配置。

但是，`quorum-type` 属性可以配置 读、写、读和写 三中类型。经过验证 `quorum-type` 属性是表示仲裁生效时，即当前集群成员数小于 `quorum-size` 时，不允许该集群做什么操作。例如，A 集群的当前成员数少于 `quorum-size` ，而它的 `quorum-type` 设置为读，那么 A 集群将无法执行读操作，但可以完成写操作。





















