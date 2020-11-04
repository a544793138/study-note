# SimpleDateFormat 的非线程安全问题

提到格式化日期，我们很容易就想到了 SimpleDateFormat，但其实它的对象是非线程安全的，而我们更乐意用 static 来修饰它的对象。
这就导致了问题的产生，最终导致多线程下我们期待格式化的日期时间，结果并不是我们所希望的，而是其他线程的日期。

通常我们是这样使用的：
```java
private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

public void formData() {
    sdf.parse("2020-10-29");
    ...
}
```

解决方法：
- 同步锁 - 但会影响性能
- 将 SimpleDateFormat 变为局部变量 - 重复初始化
- 使用 ThreadLocal：
```java
private final static ThreadLocal<SimpleDateFormat> SIMPLE_DATE_FORMAT_THREAD_LOCAL = new ThreadLocal<SimpleDateFormat>();

public static SimpleDateFormat getSimpleDateFormat() {
        if (SIMPLE_DATE_FORMAT_THREAD_LOCAL.get() == null) {
            SIMPLE_DATE_FORMAT_THREAD_LOCAL.set(new SimpleDateFormat("yyyy-MM-dd"));
        }
        return SIMPLE_DATE_FORMAT_THREAD_LOCAL.get();
}

public void formData() {
    getSimpleDateFormat.parse("2020-10-29");
    ...
}
```

注意：
- 通常使用 static 来修饰 ThreadLocal，但不要使用静态代码块来设置 ThreadLocal，如：
```java
static {
    SIMPLE_DATE_FORMAT_THREAD_LOCAL.set(new SimpleDateFormat("yyyy-MM-dd"));
}
```
这样会导致只有一条线程的 ThreadLocal 中拥有 SimpleDateFormat，其余则没有。