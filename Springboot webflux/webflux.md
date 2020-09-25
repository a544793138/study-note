# Webflux

- 往 pom.xml 添加 webflux starter
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

- Controller

webflux 依然可以使用 Controller 来封装服务。

```java
@RestController
public class HelloController {

    @PostMapping("/webflux-mirror")
    public Mono<String> webfluxMirror(@RequestBody String message) {
        return Mono.just(message);
    }

}
```

- 或者 Handler 和 RouteConfig

在 webflux 中，除了 Controller，还支持了一种 Handler 和 RouteConfig 的组合，也是可以封装服务的。

```java
@Component
public class HelloHandler {

    public Mono<ServerResponse> ping(ServerRequest serverRequest) {

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue("{\n" +
                        "  \"status\": \"ok\"\n" +
                        "}"));
    }

    public Mono<ServerResponse> webfluxMirrorMethod(ServerRequest serverRequest) {

        final Mono<String> stringMono = serverRequest.bodyToMono(String.class);

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(stringMono, String.class);
    }
}

@Configuration
public class RouteConfig {

    @Bean
    public RouterFunction<ServerResponse> routerFunction(HelloHandler testController) {
        return RouterFunctions
                // 接受 GET /hello 请求，然后使用 HelloHandler#ping 方法处理该请求
                .route(RequestPredicates.GET("hello"), testController::ping)
                // 接受 POST /webflux-mirror-method 请求，然后使用 HelloHandler#webfluxMirrorMethod 方法处理该请求
                .andRoute(RequestPredicates.POST("/webflux-mirror-method")
                        .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), testController::webfluxMirrorMethod);
    }
}
```