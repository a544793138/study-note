## GC 研究

### 可参考的健康的 GC 状况

在网上查询了一些[资料](https://www.jianshu.com/p/5ace2a0cafa4)，文章里说一个可参考的健康的 GC 状况：

1. YoungGC 频率 5秒/次;（3秒～6秒也是比较合理的）
2. CMS GC 频率不超过 1天/次;
3. 每次 YoungGC 的时间不超过 20ms;
4. FullGC 频率尽可能完全杜绝（少）（4.5天/次）

### 常用命令

```java
// 观察 GC 情况
jstat -gc [进程号] [打印间隔时间] [打印次数（可选）]

// 观察 GC 统计情况
jstat -gcutil [进程号] [打印间隔时间] [打印次数（可选）]

// 观察 JVM 堆内存使用情况
jmap -heap [进程号]

==================== 以下命令需要加入启动脚本 ====================
// 开启 GC 详细日志
-XX:+PrintGCDetails

// 修改 JVM 堆内存大小
初始堆内存：-Xms1024m
最大堆内存：-Xmx1024m

// 修改 JVM 年轻代内存大小
初始：-XX:NewSize=1024m
最大：-XX:MaxNewSize=1024m
大小一致：-Xmn1024m

// 修改 JVM Metaspance 大小
初始：-XX:MetaspaceSize=128M
```

### 基本知识点

1.  JDK8 以及其之后，JVM 堆内存 = 年轻代 + 年老代（O） + Metaspance
2.  年轻代 = 两个幸存区（S0/S1） + 伊甸园区（E），占比 S0:S1:E = 1:1:8

3.  默认年轻代：年老代 = 1：2，sun 官方推荐年轻代占堆的 3/8

4.  Metaspance 是用于永久存放的，不占用堆内存而是直接占用机器内存。

5.  触发 GC：

   - E 区满了后，触发 young gc，将 E + S0/S1 复制到 S1/S0，溢出的部分将会晋升到 O 区
   - O 区 或 Metaspance 满里后触发 Full gc


### 现况

测试条件：

> 服务器：192.1.8.109
>
> 数据库：192.1.8.109
>
> splunk：192.1.8.109
>
> 客户端：远程桌面 JMeter
>
> 测试接口：sign/rsa-sha1，密钥长度 1024
>
> 日志：prod
>
> maxKeepAliveRequests: -1

****

结果：

1. 测试下，caas 年轻代 GC 1秒/次

### 尝试与结果

> 以下尝试与结果均在 192.1.8.109 服务器上进行

**调整 JVM 内存大小到 30 G：**

 	1. caas young gc 变为 2～3秒/次
 	2. 性能没有提高

**调整 JVM 内存大小到 60 G：**

1. caas young gc 变为 4秒/次
2. 性能没有提高
3. Full gc 发生频率很慢，（175并发，O 区每4秒涨 0.01%）

**调整 Metaspance 大小**

metraspance 默认大小是 21M，发现在启动 CaaS 的时候出现了 3次由它引起的 Full gc。将其调整至 128M后，启动不再看到 Full gc。后续 10 分钟内也没有发现其引起 Full gc。

### 总结

1. 调整 JVM 内存以改变性能和稳定性过于偏向底层技术，难以取得显著的成果。对于产品在不同机器上运行的调整又不相同，所以会加大产品在部署时的复杂性，再深入研究不太可取。
2. 适当调整 Metaspance 的大小应该可以减少 CaaS 在启动时的时间。同样，适当调整 JVM 内存的大小以增大年老区的大小，以减少 Full gc 频率应该可以提高稳定性，减少因为 GC 引起的波动。
3. 对 JVM 内存的调整存在疑惑，因为在 192.1.8.110 机器上的 JVM 堆内存并远少于 192.1.8.109 上的 JVM，可是 110 机器却可以达到 2～3秒/次的 young gc。



## 命令

```java
// 查看活着的对象
jmap -histo:live [进程号] 

// 输出到文件
jmap -histo:live [进程号] >> [文件名]
// 覆盖
jmap -histo:live [进程号] > [文件名]

// 搜索关键词
jmap -histo:live [进程号] | grep "[关键词]"

// 统计实例数
jmap -histo:live [进程号] | grep "[关键词]" |awk '{sum +=$2} END {print sum}'

// 对比两个文件
diff [文件1] [文件2]
```



