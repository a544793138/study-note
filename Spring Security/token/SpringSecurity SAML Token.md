# Spring Security SAML Token

在看这篇笔记前，请先看 [Spring Security JWT](Spring Security JWT.md) 。
因为两者的道理是一样的，同样是 Spring Security 为系统提供用户认证和授权功能，而用于认证和授权的信息载体，从 JWT 换成了 SAML token。
 
> 这里的 SAML token 其实是 SAML 响应 / SAML 断言，如果不清楚的可以先去了解一下 SAML 规范。
> 
> 这里简单说明一下，正常使用的 SAML 规范定义了 SP 和 IDP，SP 就是提供服务的，相当于你的系统，IDP 就是提供身份验证。
> 
> 当用户调用 SP 中受保护的资源时，SP 会发送一个认证请求到 IDP，然后用户在 IDP 那边完成用户认证后，IDP 将认证响应（SAML 响应，其中就包含必要的断言）
> 返回给 SP，SP 对响应进行验证和处理，获得用户信息，允许用户访问受保护资源。