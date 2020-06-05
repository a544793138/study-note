[TOC]

# Hazelcast

##  springboot 切换 hazelcast 的方法

1. **引入依赖包**

```xml
<dependency>
	<groupId>com.hazelcast</groupId>
	<artifactId>hazelcast</artifactId>
</dependency>

<dependency>
	<groupId>com.hazelcast</groupId>
	<artifactId>hazelcast-spring</artifactId>
</dependency>
```

2. **将 Hazelcast 配置或 Hazelcast 实例加入到 ioc 容器中**

> 1. 从 springboot 官方文档可知，当我们加入 Hazelcast 配置到容器中时，springboot 启动则会自动为我们生成 Hazelcast 实例。当容器中存在 Hazelcast 实例时，则 springboot 不再自动创建 Hazelcast 实例
> 2.  Hazelcast 配置或 Hazelcast 实例可以都加入到容器中

```java
// Hazelcast 的缓存配置
@Bean
public Config config() {
	NetworkConfig networkConfig = new NetworkConfig();
    // port，默认为8199，用于集群成员通信的端口，同一机器身上不同成员需要配置不同端口
	networkConfig.setPort(port);
    /*
     * portCount，默认为1，用于表示尝试端口，如：
     * (1) port==8199,portCount==1 => 当8199已被占用时，启动失败
     * (2) port==8199,portCount==2 => 当8199已被占用时，尝试使用8200启动，当8200也被占用时，启动失败
     * 等等
     */ 
	networkConfig.setPortCount(portCount);
	final JoinConfig joinConfig = new JoinConfig();
	joinConfig.getMulticastConfig().setEnabled(false);
    final TcpIpConfig tcpIpConfig = new TcpIpConfig();
    // members，成员列表的 IP 列表，必须配置
    if (null != members && members.size() > 0 && StringUtils.isNotBlank(members.get(0))) {
		tcpIpConfig.setMembers(members);
	}
    tcpIpConfig.setEnabled(true);
    joinConfig.setTcpIpConfig(tcpIpConfig);
    networkConfig.setJoin(joinConfig);
    final Config config = new Config();
    config.setNetworkConfig(networkConfig);

    // 创建一个名为 book-rmap 的 ReplicatedMap
    config.addReplicatedMapConfig(new ReplicatedMapConfig().setName("book-rmap"));
    // 创建一个名为 book-map 的 Map
	config.addMapConfig(new MapConfig().setName("book-map"));
	return config;
}

// Hazelcast 实例
@Bean
public HazelcastInstance hazelcastInstance(Config config) {
	final HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config);
    // 给指定的数据结构添加监听器
	hazelcastInstance.getReplicatedMap("book-rmap").addEntryListener(new CacheListener());
	hazelcastInstance.getMap("book-map").addEntryListener(new CacheListener(), true);
	return hazelcastInstance;
}
```

3. 结构 springboot 缓存抽象组件使用

```java
@Cacheable(value = "book-rmap", key = "#id", unless = "#result == null")
public String findBook(int id) {
	log.info("============ Enter the method findBook ============");
    return books.get(id).toString();
}
```

### 问题出现：

从 `com.hazelcast.spring.cache.HazelcastCacheManager#getCache` 方法可以看到

```java
@Override
public Cache getCache(String name) {
	Cache cache = caches.get(name);
    if (cache == null) {
        IMap<Object, Object> map = hazelcastInstance.getMap(name);
	    cache = new HazelcastCache(map);
        long cacheTimeout = calculateCacheReadTimeout(name);
        ((HazelcastCache) cache).setReadTimeout(cacheTimeout);
	    Cache currentCache = caches.putIfAbsent(name, cache);
        if (currentCache != null) {
	        cache = currentCache;
		}
	}
    return cache;
}
```

这个方法是当我们使用 `@Cacheable(value = "book-rmap" ...)`  时，缓存会调用的方法，用于获取缓存对应的空间。而我们从 `hazelcastInstance.getMap(name);` 可以看到 Hazelcast 默认使用 Map 作为缓存空间的数据库结构，从之前的调研中我们希望使用的是 ReplicatedMap。

暂时还未能发现有方法可以默认使用 ReplicatedMap 作为缓存空间的数据结构。

目前已经向 Hazelcast 项目组提出 issue，等待解答中...

<https://stackoverflow.com/questions/57195637/how-to-use-replicatedmap-instead-of-map-in-the-springboot-hzelcast-cache#>

stackouverflow 提问后，回答者说目前这个是不可能的，不可配置的。但可以考虑使用替代方案 Near Cache。即是在 Map 数据结构的基础上，配置 [Near Cache](<https://docs.hazelcast.org/docs/3.12.1/manual/html-single/index.html#near-cache>) 。

**经过测试，使用 Near Cache 是可以达到预期的，但同样的会带来数据一致性的问题，这个需要更仔细的测试。**

4. 使用 Near Cache 配置

```java
@Bean
public Config config() {
	NetworkConfig networkConfig = new NetworkConfig();
    networkConfig.setPort(port);
	networkConfig.setPortCount(portCount);
	final JoinConfig joinConfig = new JoinConfig();
    joinConfig.getMulticastConfig().setEnabled(false);
    final TcpIpConfig tcpIpConfig = new TcpIpConfig();
    if (null != members && members.size() > 0 && StringUtils.isNotBlank(members.get(0))) {
        tcpIpConfig.setMembers(members);
        }
	tcpIpConfig.setEnabled(true);
	joinConfig.setTcpIpConfig(tcpIpConfig);
	networkConfig.setJoin(joinConfig);
    final Config config = new Config();
	config.setNetworkConfig(networkConfig);

    EvictionConfig evictionConfig = new EvictionConfig()
            .setEvictionPolicy(EvictionPolicy.LFU)
            .setMaximumSizePolicy(EvictionConfig.MaxSizePolicy.ENTRY_COUNT)
		    .setSize(90);

	// near cache
	NearCacheConfig nearCacheConfig = new NearCacheConfig()
			.setInMemoryFormat(InMemoryFormat.OBJECT)
			.setEvictionConfig(evictionConfig);

	// 配置所有 Map 都使用 near cache
	config.addMapConfig(new MapConfig().setName("*").setNearCacheConfig(nearCacheConfig).setBackupCount(0));
		return config;
}

@Bean
public HazelcastInstance hazelcastInstance(Config config) {
	final HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config);
	// 对指定缓存空间添加监听器
    hazelcastInstance.getMap("book").addEntryListener(new CacheListener(), true);
    return hazelcastInstance;
}
```

## Hazelcast 的一些注意事项

### 不使用 NearCache 的缓存空间

**HSM 连接** 的缓存空间不使用 NearCache，因为没有必要使用。

HSM 连接是特殊的缓存对象。比如新增密码机方法，传入的只是 IP 和端口，而我们需要手动将 IP 和端口变成 HSM 连接再存到缓存中。

### 缓存事件

- **缓存失效/过期**：缓存达到所指定的失效时间，从而触发缓存失效/过期的事件。**缓存失效/过期事件的生效范围是不包括 Nearcache 的**，但由于该事件只是引起再次调用数据库查询，所以当缓存失效/过期时不作任何其他处理。
- **缓存驱逐**：主动调用了 **缓存驱逐方法（@CacheEvict）** 时触发缓存驱逐事件。**缓存驱逐事件的生效范围是整个集群，包括 Nearcache的**。
- **缓存更新**：CaaS 中使用到缓存更新的有 HSM 连接 和 SessionId 两类缓存对象。**Hazelcast 中缓存更新事件作为范围是整个集群，包括 Nearcache**。

### 集群配置

在将缓存组件切换到 Hazelcast 后，同时带来了集群配置的改动。

#### 使用 Hazelcast 时集群配置

```properties
#### Cluster start ####
# members can be an array :
# caas.cluster.members[0]=ip0:port0
# caas.cluster.members[1]=ip1:port1
# ...
caas.cluster.members=
#caas.cluster.port=8199
#### Cluster end ####
```

#### 配置解析

>  前提：一个 CaaS 实例一台服务器

**caas.cluster.members** - 集群所有成员的发现列表，Hazelcast 会根据该列表逐个尝试发现其他节点。应该填写集群里所有成员的 ip:port。

**caas.cluster.port** - 该 CaaS 实例的 Hazelcast 监听端口，默认 8199。当 8199 端口被占用时，可修改为其他端口，但注意同步修改 `caas.cluster.members` 中对应的 port。

**隐藏配置**

> 非开发人员无需关心

**caas.cluster.port-count** - 尝试端口，默认为 1。

```
例如，port 为 8199 时：
1. portCount == 1 时，那么当 8199 端口被占用时，直接报启动错误。
2. portCount == 2 时，那么当 8199 端口被占用时，尝试 8200 端口，若 8200 也被占用时，报启动错误。
3. portCount == ...
```

**caas.cluster.max-idle-seconds** - 缓存失效时间，单位：秒，默认 172800，即两天。