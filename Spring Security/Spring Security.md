# Spring Security

> 这里将介绍 Spring boot 项目中 Spring Security 的基本用法，以及它的基本 demo。
> Spring Security 能与很多其它的安全标准、规范相结合，最终的样子比基本用法要复杂很多。可以该目录中的其他笔记。

Spring Security 是一个安全框架，主要用于对系统进行安全认证和授权方面的。所谓的认证和授权，其实是两个步骤，
并且可以用“用户登录”这种简单例子很多的进行说明：

那么以用户登录的场景为例子：

- **认证** - 认证就是对用户的身份进行验证，证明进入系统的用户是我们认可的用户。通常我们会使用账号、密码的方式来验证用户身份。
- **授权** - 授权通常是紧接 **认证** 之后的，它指的是：为进入系统且认证成功的用户，指定特定的角色。
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
2. 重写 `configure(HttpSecurity http)` 方法，在该方法中主要是设置系统的安全管理：
  - 可以设置 URL 与角色的对应关系，从而设置角色权限和控制 URL 访问
  - 可以开启 / 关闭 Spring Security 自带的登录 / 登出功能（会附带上 Spring Security 提供的登录页，登出页），也可以设定登录 / 登出时经过的 Restful 接口
  - 可以控制 session 失效时的处理情况
  - 可以开启 / 关闭 csrf 防护
  - ...
3. 重写 `configure(AuthenticationManagerBuilder auth)` 方法，在该方法中，主要是用来完成用户的认证和授权的：
  - spring security 支持多种的认证方式，比如结合一些安全规范 JWT 和 SAML 等等，但也有提供最常用的帐号密码认证方式，还可以连接到数据库进行帐号密码的对比。
  - 授权方式基本都是类似的，最终结果就是为用户添加上他拥有的角色。在实际情况中，通常在认证结束后，能获得登录用户的 `username` 以提供查询用户拥有的角色并赋予。
  - demo 中只是用最简单的账号密码认证方式，与直接授权，更多的还请阅读 Spring Security 目录下的其他笔记。

以上 3 步后，基本就完成了一个简单的 Spring Security 调用 demo，剩下的就是 Controller、前端页面以及两者之间的跳转问题。

上述 3 步的示例代码，配合注释阅读：
```java
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // authorizeRequests 表示开始指定 URL 与角色关系，以及对 URL 进行权限的检查
        http.authorizeRequests()
                // /css/**、/index 允许任何人访问，不需要认证授权都可以访问，通常用于通过前端页面的静态资源
                .antMatchers("/css/**", "/index").permitAll()
                // /admin/** 的地址需要认证用户拥有 ADMIN 的角色
                .antMatchers("/admin/**").hasRole("ADMIN")
                // /user/** 的地址需要认证用户拥有 USER 的角色
                .antMatchers("/user/**").hasRole("USER")
                // 这个通常写到最后，anyRequest 表示除以上已经规定好的 URL 外，authenticated 表示需要经过认证的用户才可以访问
                // 合起来就表示除以上已经规定好的 URL 外，其他的 URL 都需要认证过的用户才能正常访问
                .anyRequest().authenticated();

        // 配置 Spring Security 自带的登录功能，指定登录页面地址，指定登录失败的地址
        http.formLogin().loginPage("/login").failureUrl("/login-error");
        // 将 CSRF 关闭，当登出使用 GET 方式时，需要关闭才能正常登出; 若登出使用 POST 则不需要关闭
        http.csrf().disable();
        // 开启登出功能
        http.logout();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                // 表示用户认证保存到内容中
                .inMemoryAuthentication()
                // 较新版本的 Spring Security 必须指定密码编码器，这个密码编码器不影响实际使用时用户输入的密码，只是 Spring Security 内部的机制。
                .passwordEncoder(new BCryptPasswordEncoder())
                // 对指定用户授权
                // 这里告诉 Spring Security，它可以认证通过账号为 linxlAdmin，密码为 123456 的用户，而且认证通过后，要为这个用户赋予 ADMIN 的角色
                .withUser("linxlAdmin").password(new BCryptPasswordEncoder().encode("123456")).roles("ADMIN")
                .and()
                // 与上述同理
                .withUser("linxl").password(new BCryptPasswordEncoder().encode("123456")).roles("USER");
    }
}
```

## 注意

1. `@EnableWebSecurity` 不一定要放到当前的 Spring Security 配置类上，也可以放到 Spring boot 的启动类上
2. 在配置 URL 权限时，请注意覆盖问题，即后填写的同样的 URL 会覆盖前面填写的同样 URL，覆盖后权限也会改变，变为后填写的 URL 所指定的角色。
所以当一个 URL 可以被多个角色访问时，应该使用 `hasAnyRole` 来指定多个角色：
```java
    // 表示 /public/** URL 只要是指定角色列表中任一个角色，都可以访问
    .antMatchers("/public/**").hasAnyRole("ADMIN", "USER")
```
