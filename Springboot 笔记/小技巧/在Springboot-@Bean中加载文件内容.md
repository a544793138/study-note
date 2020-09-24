# 在 Spring boot @Bean 中加载文件内容

不知道是因为 Bean 的高优先级加载原因，还是因为 Spring boot 本身的原因，在其中使用 FileInputStream 是无法读取到文件的。

这时则需要使用 Spring 的工具类 ResourceLoader。

使用时自动注入即可，或者 `ResourceLoader resourceLoader = new DefaultResourceLoader();` ?

例如：
```java
// privateKeyLocation 为文件位置，可以加 "classpath:"
Resource keyRes = resourceLoader.getResource(privateKeyLocation);
// StreamUtils 也是 Spring 的代码
byte[] keyBytes = StreamUtils.copyToByteArray(keyRes.getInputStream());
```