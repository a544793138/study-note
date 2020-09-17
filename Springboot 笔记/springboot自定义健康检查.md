# Springboot 自定义健康检查接口

- 引用依赖 `spring-boot-starter-actuator`
- 参考 `org.springframework.boot.actuate.jdbc.DataSourceHealthIndicator`
  - springboot 的健康检查接口，是以一个个插件报告状态的，例如硬盘空间、数据库、缓存插件等等。找到 `org.springframework.boot.actuate.health.AbstractHealthIndicator` 抽象类
  ，就可以发现有很多它的实现，其中就对应了各种各样的插件。继承并重写其中的 `doHealthCheck` 就可以完成自定义的健康检查接口。
- 请求 {url}/actuator/health 调用健康检查接口
  - 需要在配置文件上开启详情才能看到详细情况：
  ```properties
    management.endpoint.health.show-details=always
  ```
  - 一些默认插件的状态可以使用配置文件进行关闭：
  ```properties
    management.health.diskSpace.enabled=false
    management.health.livenessState.enabled=false
    management.health.ping.enabled=false
    management.health.readinessState.enabled=false
  ```