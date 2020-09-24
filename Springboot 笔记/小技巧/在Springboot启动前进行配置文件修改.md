# 在Springboot启动前进行配置文件修改

## 使用监听器的方式实现

springboot 其实提供了很多启动前中后的监听器，以方便开发者在某些事件发生时做出某些处理。

如果要做到标题的要求，那么就需要使用其中一个监听器 `ApplicationListener<ApplicationEnvironmentPreparedEvent>`。
该监听器用于监听 Springboot 环境准备时的事件，所以可以从事件中获取到环境变量，从而获取/修改配置文件的内容。

- 添加监听器
```java
public class Application {

    public static void main(String[] args) {
        final SpringApplication springApplication = new SpringApplication(Application.class);
        // Application.class.getResource("Application.class").toString() 可以获取到该类所在的运行路径，如果程序是以代码方式启动，则开头为 file:... 。如果以 jar 包方式启动，则开头为 jar:file:...
        springApplication.addListeners(new ApplicationPreparedListener(Application.class.getResource("Application.class").toString().startsWith("jar:file")));
        springApplication.run(args);
    }
}
```

- 实现监听器
```java
public class ApplicationPreparedListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    // 重写该方法，表示
    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        final ConfigurableEnvironment environment = event.getEnvironment();

        // 利用该方法获取配置文件中指定的配置项
        final String value = environment.getProperty("配置文件配置项", "获取不到时返回的默认值");

        // 利用如下方法修改配置文件中指定的配置项
        final HashMap<String, Object> default = new HashMap<>();
        default.put("希望修改的配置文件配置项", value);
        environment.getPropertySources().addFirst(new MapPropertySource("自定义的名字", default));
    }
}
```

## 使用添加启动参数的方式实现

对于 springboot 启动时传入的 args，并不是没有用的，它其实是可以传入配置文件中指定的配置项，从而在 springboot 启动前配置好配置文件，
或者根据不同的环境（如上面提到的是否使用 jar 启动）选择不同的默认配置。

使用这种方法实现时，有两种方式：
- 在启动时，使用 `--配置文件中配置项=配置值` 的格式添加启动参数，如 jar 启动时：`java -jar xxx.jar --server.port=8081 ...`
- 在 springboot 启动类中，在 springboot 启动前，手动为 args 添加上参数，同样是使用 `--配置文件中配置项=配置值` 的格式。