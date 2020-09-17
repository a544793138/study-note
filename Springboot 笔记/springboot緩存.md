# Springboot 緩存

！！！ springboot 的缓存注解，对于内部调用方式不起作用。只有方法调用的第一层生效。例如：

```java
a.getA()

a {
    // 注解在这里才会生效
    getA() {
        getAFromb();
        // 这里的缓存是可以生效的
        b.getbBya();
    }

    // 注解在这里就不生效了
    getAFromb();
}

b {
    // 注解在这里也可以生效
    getbBya();
}
```

