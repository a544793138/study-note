# CompletableFuture  调用返回 CompletableFuture 的方法

如果在 CompletableFuture 中，希望调用一个返回 CompletableFuture 的方法，那么就不能使用 `thenApplyAsync` 方法了。

因为这样会让调用返回 CompletableFuture 方法后，返回一个 CompletableFuture<CompletableFuture<XXX>> 的类，也就是 CompletableFuture 嵌套了 CompletableFuture。

因为如果发送了嵌套，接下来表达式中就不好利用 CompletableFuture 泛型中的值。

所以应该使用 `thenComposeAsync` 的方法，这样就不会嵌套了，调用后返回的依然只有一层 CompletableFuture。

示例：
```java
CompletableFuture<a的实际类> a = ...
CompletableFuture<b的实际类> b = bMethod(a的实际类 a)

a.thenComposeAsync(atemp -> bMethod(atemp))
```