# 使用 logback 采集 API 日志配置说明

Java-API 中默认采用 slf4j 与 log4j 的日志结构，但如果希望使用 slf4j 与 logback 作为日志结构，则只需要添加 logback 的日志配置文件：logback.xml 即可。

logback.xml：

```xml
<?xml version="1.0" encoding="utf-8" ?>

<configuration>

    <!-- 配置项， 通过此节点配置日志输出位置（控制台、文件、数据库）、输出格式等-->
    <!-- ConsoleAppender代表输出到控制台 -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <!-- layout代表输出格式 -->
        <layout class="ch.qos.logback.classic.PatternLayout">
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%-5p] %C{1} - %m%n</pattern>
        </layout>
    </appender>

    <!-- 日志输出文件 -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%-5p] %C{1} - %m%n</pattern>
        </encoder>
        <!-- 滚动记录文件，先将日志记录到指定文件，当符合某个条件时，将日志记录到其他文件 RollingFileAppender-->
        <!-- 滚动策略，它根据时间来制定滚动策略.既负责滚动也负责触发滚动 -->
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <!-- 定义日志文件输出路径 -->
            <fileNamePattern>log/Java-api-%d.log</fileNamePattern>
            <!-- 日志最大的历史 30天 -->
            <maxHistory>30</maxHistory>
        </rollingPolicy>
    </appender>

    <!-- 指定包路径下的日志级别，这个指定优先级高于 root-->
    <logger name="com.union" level="debug" />

    <!-- 根节点，表名基本的日志级别，里面可以由多个appender规则 -->
    <root level="debug">
        <!-- 引入控制台输出规则 -->
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
    </root>
</configuration>
```

说明：

- configuration - 配置 logback 
- appender - 用于配置输出日志的方式、格式等行为，如上述配置中配置了两个 appender ：`CONSOLE` 和 `FILE`
- logger - 用于指定包路径下的日志级别，使用 logger 指定的日志级别优先级高于 root 指定的全局日志级别。上述配置中，指定了 `com.union`  的包路径，该路径为 API 的包路径。
- root - 用于指定全局日志级别，即没有使用 logger 特别指定包路径日志级别的其它位置的日志，都采用该日志级别打印日志。同时，root 里也需要配置使用到的 appender。

