# CompletableFuture 两个传入参数

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