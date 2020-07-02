# Springboot 小技巧

## 接收接口参数的包装类

我们知道在 springboot 的接口中，如果想要接收到传入的参数，可以使用 `@RequestBody` 的注解来标注一个包含传入参数作为属性的包装类。但我们通常也只是用这个包装类来接收、传递参数。

其实我们可以更好的利用它：

1. springboot 其实是利用包装类中属性的 set 方法来给包装类对应的属性赋值的。所以，我们可以在 set 方法中添加对传入参数的校验，数据的处理等。
2. 同理，我们是通过 get 方法获取包装类里对应属性的，但是有时候因为接口传参的问题，不能将参数过于统一，必须拆分为更多的参数提供用户进行选择输入，所以，在包装类中，我们可以根据自己实现方法的需要，将接口传入的参数进行转换，转换成可以直接使用的参数，并将其传递给实现方法。

## CompletableFuture 两个传入参数

我们知道 `CompletableFuture ` 通常是链式调用的，使用 `lamda ` 表达式进行书写。这样一来，通常一个链式调用里，只有一个参数从上往下传递，但是如果你在某处需要使用从上面得到的两个参数时，就会陷入困境。

其实 `CompletableFuture` 提供了对应的方法，就是使用 `thenCombine` / `thenCombineAsync` 来连接两个 `CompletableFuture` 的结果。

例如：

```java
CompletableFuture<a的实际类> a = ...
CompletableFuture<b的实际类> b = ...

a.thenCombineAsync(b, (a的实际类参数，b的实际类参数) -> {
    ...
    return ...;
})
```

我们可以看到使用 `thenCombineAsync` 的参数中，同时获取到 a 和 b 的实际返回，在其后的 `lamda` 中，就可以同时使用这两个参数啦。

## CompletableFuture  调用返回 CompletableFuture 的方法

如果在 CompletableFuture 中，希望调用一个返回 CompletableFuture 的方法，那么就不能使用 `thenApplyAsync` 方法了。

因为这样会让调用返回 CompletableFuture 方法后，返回一个 CompletableFuture<CompletableFuture<XXX>> 的类，也就是 CompletableFuture 嵌套了 CompletableFuture。

如果嵌套了，那就不好继续利用了。

所以应该使用 `thenComposeAsync` 的方法，这样就不会嵌套了。

## Spring boot 自动建数据库表

```properties
spring.jpa.hibernate.ddl-auto=update
```

## 修改 Spring boot 插件版本号的问题

在项目拥有多个子项目的情况下，无论 maven / gradle，有时候无法在子项目中指定需要使用的插件版本号，导致 Spring boot 默认的插件版本号会与你指定的版本号一同出现，而 IDE 却无法识别，甚至选择 Spring boot 的默认支持的插件版本作为依赖，让你指定的版本号失效。

这个时候，可以到项目根目录的 pom.xml / build.gradle 中进行指定你需要的插件版本号，或者直接在其中进行依赖的声明，这样肯定就会生效。

## Spring boot 开放 @Bean 中需要使用的配置项

Spring boot 开放配置项很简单，使用 @Value 或者 @ConfigurationProperties 即可。

但是，如果开放的配置项是用于 @Bean 中的，由于 @Bean 在 Spring boot 启动时加入 Spring IOC 的容器优先级比较高，
所以上述两种方法还不够，会出现 Spring boot 配置文件中配置了参数，但无法在代码中获取到的情况。

这个时候，可以用一个类包装需要开放的参数，依然使用  @ConfigurationProperties 注解来标注包装类。
但在需要使用参数的 @Bean 中的类上，使用 `@EnableConfigurationProperties(包装类.class)` 即可。