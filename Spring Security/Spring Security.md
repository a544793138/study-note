# Spring Security

> 这里将介绍 Spring boot 项目中 Spring Security 的基本用法，以及它的基本 demo。
> Spring Security 能与很多其它的安全标准、规范相结合，最终的样子比基本用法要复杂很多。可以该目录中的其他笔记。

Spring Security 是一个安全框架，主要用于对系统进行安全认证和授权方面的。所谓的认证和授权，其实是两个步骤，
并且可以用“用户登录”这种简单例子很多的进行说明：

那么以用户登录的场景为例子：

- **认证** - 认证就是对用户的身份进行验证，证明进入系统的用户是我们认可的用户。通常我们会使用账号、密码的方式来验证用户身份。
- **授权** - 授权通常是紧接 **认证** 之后的，它指的是为进入系统且认证成功的用户，指定特定的角色。
因为 Spring Security 中是使用角色与 URL 结合来控制权限的，即 Spring Security 通常会设置某些 URL 需要 某个 / 某些 特定的角色才可以访问。
所以为认证成功的用户添加角色，就是为用户添加对应的权限，所以称之为授权。

## 依赖

- Maven

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.3.1.RELEASE</version>
</parent>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

## 简单 demo

根据以下基本，就可以构建一个基本的 demo 并运行起来：

1. 创建配置类，继承 `WebSecurityConfigurerAdapter`，并在配置类上使用 `@EnableWebSecurity` 注解
2. 重写 `configure(HttpSecurity http)` 方法，在该方法中：
  - 可以设置 URL 与角色的对应关系，从而设置角色权限和控制 URL 访问
  - 可以开启 / 关闭 Spring Security 自带的登录 / 登出功能（会附带上 Spring Security 提供的登录页，登出页），也可以设定登录 / 登出时经过的 Restful 接口
  - 可以控制 session 失效时的处理情况
  - 可以开启 / 关闭 csrf 防护