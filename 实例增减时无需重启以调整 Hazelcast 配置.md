# 实例增减时无需重启以调整 Hazelcast 配置

集群功能是由 Hazelcast 提供的。

需要完成本 Issue 的目标，当前的有两个配置问题需要解决：
1. 我们要求在配置文件上，在成员列表配置项中填写集群中所有成员的地址。
2. 开启脑裂保护时，需要配置 Hazelcast 脑裂保护功能的最小集群的成员数量

## 配置集群成员的全部成员地址

- 增加实例：新增的实例仅需要配置已经存在与集群中的成员地址即可，这是 Hazelcast 发现机制本身拥有的特点。且在新增实例成功加入到集群后，新增的实例将获得完整的集群成员地址列表，此时即使新增实例依赖进入集群的成员离开集群，也不会影响新增实例。（需验证）
- 减少实例：直接关闭 CaaS 实例即可。

## 开启脑裂保护时，配置最小集群的成员数量

在 Hazelcast 配置中，定义了两种配置，一种是创建 Hazelcast 实例前，称为静态配置，另一种是创建 Hazelcast 实例后，称为动态配置。

在 [Hazelcast 官方文档](https://docs.hazelcast.org/docs/latest/manual/html-single/#dynamically-adding-data-structure-configuration-on-a-cluster) 中，有提供集群的动态添加配置的功能，该功能可以为已经创建的 Hazelcast 实例添加新的配置，并在添加后 Hazelcast 将会自动将配置更新到集群中每个成员中。

但该功能并不支持全部 Hazelcast 配置，不支持的列表中就包含 **脑裂保护**（请考虑 Hazelcast 禁止了这一操作的原因）。

> 注意：
> - Hazelcast 原有功能并不支持动态修改脑力保护中的最小成员数。
> - 最小成员数的作用是判断集群发生脑裂后当前实例所在的集群是否继续工作，若当前集群成员数小于最小成员数，则不允许进行工作。
> - 在原有机制上，最小成员数是每个实例独立的配置，不受其余实例的影响，所以脑裂保护机制的生效也独立的。

## 利用缓存动态修改其余实例的最小成员数

最小成员数是需要在初始化 Hazelcast 实例时进行配置的。
但发现利用已经初始化完成的 Hazelcast 实例（HazelcastInstance）可以获取到脑裂保护的相关配置，
同时还能对其进行修改。修改后的脑裂保护会生效（这点是实现动态修改后测试发现的）。

所以，针对以上发现，只要将新加入集群的成员的最小成员数进行缓存，并利用 Hazelcast 自身的缓存同步机制和缓存监听器，
在最小成员数这一缓存发生变化时，其余成员都可以接收到缓存变化的通知，从而修改其他成员自身的最小成员数。

大概代码如下：
```java
@Bean
// Hazelcast Config
public Config config() {
    ...
    // 开启脑裂保护时
    if (splitBrainProtectionEnable) {
        // 使用 Probabilistic 脑裂保护方法
        SplitBrainProtectionConfig quorumConfig = SplitBrainProtectionConfig.newProbabilisticSplitBrainProtectionConfigBuilder(SplitBrainProtectionName, splitBrainProtectionMinSize).enabled(true).build();
        quorumConfig.setProtectOn(getSplitBrainProtectionType(splitBrainProtectionType));

        // 为原有的缓存空间关联脑裂保护
        allMapConfig.setSplitBrainProtectionName(SplitBrainProtectionName);

        // SplitBrainMinSize 是为缓存最小成员数所开的缓存空间，注意不要关联脑裂保护，否则脑裂保护同样会阻止缓存最小成员数的操作
        MapConfig splitBrainConfigMapConfig = new MapConfig().setName(SplitBrainMinSizeListener.NAME)
                .setBackupCount(0).setInMemoryFormat(InMemoryFormat.OBJECT).setMergePolicyConfig(mergePolicyConfig);

        config.addMapConfig(splitBrainConfigMapConfig);
        config.addSplitBrainProtectionConfig(quorumConfig);
    }
    ...
}

@Bean
public HazelcastInstance hazelcastInstance(Config config, ApplicationContext context) {
    final HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config);
    ...
    // 对指定缓存空间添加监听器
    if (splitBrainProtectionEnable) {
        hazelcastInstance.getMap(SplitBrainMinSizeListener.NAME).addEntryListener(new SplitBrainMinSizeListener(context), true);
    }
    ...
    return hazelcastInstance;
}

@Bean
// 根据是否开启脑裂保护来判断是否加载这一个 Bean
@ConditionalOnProperty(value = "xxx.split-brain-protection-enable")
public SplitBrainMinSizeService splitBrainMinSizeService(CacheManager cacheManager) {
    final SplitBrainMinSizeService splitBrainMinSizeService = new SplitBrainMinSizeService();
    splitBrainMinSizeService.update(cacheManager, splitBrainProtectionMinSize);
    return splitBrainMinSizeService;
}

// 在监听器中
@Override
public void entryUpdated(EntryEvent<String, Integer> event) {
    log.debug("Cache entry {} = {} modified in Cache:: {}, Member:: {}", event.getKey(), event.getValue(), event.getName(), event.getMember());
    context.getBean(HazelcastInstance.class).getConfig()
            .getSplitBrainProtectionConfig(HazelcastConfiguration.SplitBrainProtectionName)
            .setMinimumClusterSize(event.getValue());
}
```

以上代码最终实现的效果：根据最新加入的成员的最小成员数，修改所加入集群中其余成员的最小成员数。

例如，有两个实例，A、B，分别设置最小成员数 2、15。则有如下情况：
- A 先启动，B 后加入到 A，形成集群，此时 A 的最小成员数从 2 变为 15，B 的最小成员数为 15。A、B 均无法正常工作（脑裂保护阻止）
- B 先启动，A 后加入到 B，形成集群，此时 B 的最小成员数从 15 变为 2，A 的最小成员数为 2。A、B 均正常工作（脑裂保护通过）