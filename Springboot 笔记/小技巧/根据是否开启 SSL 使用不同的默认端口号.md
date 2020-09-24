# 根据是否开启 SSL 使用不同的默认端口号

以下的代码配置，是发生在 Springboot 启动类启动之后，即 `springApplication.run(args);` 之后的。

> 但是有时默认配置可能需要在 Springboot 启动之前就确定好。请参考 [在Springboot启动前进行配置文件修改](#在Springboot启动前进行配置文件修改.md)

```java
@Configuration
public class PortConfig {

    @Bean
    public WebServerFactoryCustomizer<ConfigurableWebServerFactory> webServerFactoryCustomizer(ServerProperties serverProperties) {
        return factory -> {
            if (serverProperties.getPort() != null) {
                return;
            }
            if (serverProperties.getSsl().isEnabled()) {
                factory.setPort(8443);
            } else {
                factory.setPort(8080);
            }
        };
    }
}
```

**注意**

Linux下部署，非 root 用户启动时，无法对 1024 以下端口进行监听，如果使用则会包安全性异常：
```java
java.net.SocketException: Permission denied
```

所以如果仍需要将 HTTP 端口定为 80，HTTPS 端口定为 443，则在 linux root 下执行端口映射的命令：
```shell
$ iptables -t nat -A PREROUTING -p tcp --dport 80 -j REDIRECT --to-port 8080
$ iptables -t nat -A PREROUTING -p tcp --dport 443 -j REDIRECT --to-port 8443
$ service iptables save
```