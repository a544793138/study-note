# Spring boot 开放 @Bean 中需要使用的配置项

Spring boot 开放配置项很简单，使用 @Value 或者 @ConfigurationProperties 即可。

但是，如果开放的配置项是用于 @Bean 中的，由于 @Bean 在 Spring boot 启动时加入 Spring IOC 的容器优先级比较高，
所以上述两种方法还不够，会出现 Spring boot 配置文件中配置了参数，但无法在代码中获取到的情况。

这个时候，可以用一个类包装需要开放的参数，依然使用  @ConfigurationProperties 注解来标注包装类。
但在需要使用参数的 @Bean 中的类上，使用 `@EnableConfigurationProperties(包装类.class)` 即可。

注意：这里的 Bean 仅仅指 @Bean，@Service 那些也可以，因为对于 Springboot 来说，这些都是 Bean。不过如果加载优先级不高，那么应该会优先获取到参数然后再使用的。

示例：
```java

@ConfigurationProperties(prefix = ...)
public class A {

    private String parameter;

    get / set
}

@Configuration
@EnableConfigurationProperties(A.class)
public class Aconfig {

    @Bean
    public ...
}
```