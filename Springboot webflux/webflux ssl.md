# Netty SSL

- webflux SSL
webflux 中默认使用 netty 作为容器，netty ssl 默认使用 WANT 作为认证方式，即单向认证，但这种情况时 webfilter 的 request 的 sslinfo 中无法获取到客户端的证书。只有当认证方式为 NEED，即双向认证时，才可以从 sslinfo 中获取到客户端证书。

- 调整工作线程

用于调整工作线程，但其实默认就是最优的

```java
System.setProperty(ReactorNetty.IO_WORKER_COUNT, "300");
```