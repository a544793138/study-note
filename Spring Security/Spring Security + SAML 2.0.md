## Spring Boot + Spring Security demo

Spring Security 是一个安全框架，主要用于对系统进行安全认证和授权方面的补充。

**集成**

1. 创建配置类，继承 `WebSecurityConfigurerAdapter` 类，在类中使用 `@EnableWebSecurity` 注解。
2. 重写 `protected void configure(HttpSecurity http)`  方法以进行**认证**。在方法体中使用入参 `HttpSecurity http` 可以：
   - 对指定的 `url` 地址进行拦截并指定该 `url` 所需要的角色
   - 指定系统的登录、登录失败、登出等页面
3. 重写 `protected void configure(AuthenticationManagerBuilder auth)` 方法以进行**授权**。在方法体中使用入参 `AuthenticationManagerBuilder auth` 可以：
   - 授权指定用户拥有指定的角色

**完整配置**

```java
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                // /css/**、/index 允许任何人访问
                .antMatchers("/css/**", "/index").permitAll()
                // /user/** 的地址需要认证用户拥有 USER 的角色
                .antMatchers("/user/**").hasRole("USER");

        // 指定登录页面地址，指定登录失败的地址
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
                .passwordEncoder(new BCryptPasswordEncoder())
                // 对指定用户授权
                .withUser("linxl").password(new BCryptPasswordEncoder().encode("123456")).roles("USER");
    }
}
```

## Spring Security + SAML 2.0

Spring 公司对 Spring Security 和 SAML 2.0 两种技术，目前 Spring 公司有**两种**实现和支持：

- Spring Security SAML
  - 这是一个名为 Spring Security SAML 的独立扩展库，这个库完整的实现了 SAML 2.0
  - 因为支持完善的原因，配置相对复杂。
- Spring Security 的 SAML 2.0 Login 功能
  - 这是 Spring Security 框架的一个技术支持 / 功能，目前未完善，有一个相对重要的功能：` Single logout ` 未实现。
  - 配置相对简单。

对于以上的两种实现，都已经找到了相应的 demo。

上述的两种实现和支持，都有一个类似的配置，因为不需要考虑实现 IDP 端，所以只需要配置好 SP 端即可，其中包括：

- SP 的 entityId : 唯一标识
- 对端 url : IDP 的 URL 地址
- IDP 的元数据：可能是本地保存的静态 xml 格式数据，也可能是提供 IDP 对应地址，从该地址自动下载的元数据
- SP 与 IDP 交流时用于保密和验证数据的证书等

## Spring boot + Spring Security SAML demo

> [参考资料](<https://github.com/ulisesbocchio/spring-boot-security-saml-samples/tree/master/spring-security-saml-sample>)

参考网上的资料，搭建出了使用 Spring boot + Spring Security SAML 独立扩展库实现的 Spring Security + SAML 2.0 的需求。

在参考资料所给的 demo 中，提供了多个互联网上公开的 IDP，用于测试参考资料中的 demo。

我们则是选择了其中提到的 `Okta` 网站搭建自己的 IDP，搭建 IDP 时，有以下定义：

- **断言消费者（ACS）/ 单点登录网址 / 收件人 URL / 目标地址** ： 这是一个地址，用于表示 IDP 产生断言后，将断言发送回 SP，SP 负责处理断言的地址。如 demo 中的：`http://localhost:8080/saml/SSO`
-  **受众群体 / 观众** ：一个字符串，是 SP 的标识。demo：`urn:test:linxl:guangzhou:keyou`
- **默认 Relay State**：一个地址，表示 IDP 登录成功后，返回 / 跳转到 SP 的哪个地址上。demo：`http://localhost:8080/home`
- [其余的请参考 Okta 中 `SAML IDP` 应用的具体配置（IDP 的设置地址）](<https://dev-812782-admin.okta.com/admin/app/keyoudev812782_samlidp_1/instance/0oad69wmvVC6AlTTY4x6/#tab-general>)
  - 用户：linxl@keyou.cn
  - 密码：12345678aA



