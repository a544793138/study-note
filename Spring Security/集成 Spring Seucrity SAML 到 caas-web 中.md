# 集成 Spring Seucrity SAML 到应用中

原有应用路径及跳转：

- `http://localhost:8080/#/login` 为原有登录页
- `http://localhost:8080/#/index` 为原有首页，即登录后的页面

```
http://localhost:8080 => http://localhost:8080/#/login
http://localhost:8080/ => http://localhost:8080/#/login
http://localhost:8080/index => 404
http://localhost:8080/index.html => http://localhost:8080/index.html#/login
http://localhost:8080/login => 404
http://localhost:8080/login.html => 404
http://localhost:8080/# => http://localhost:8080/#/login
http://localhost:8080/#/login => http://localhost:8080/#/login
http://localhost:8080/#/login.html => http://localhost:8080/#/login
http://localhost:8080/#/index => http://localhost:8080/#/login
http://localhost:8080/#/index.html => http://localhost:8080/#/login
```

现有的应用路径及跳转：

**登录流程**

1. 在 `http://localhost:8080/#/login` 打开登录页（不受 SP 保护，只有一个登录按钮）。

2. 点击登录按钮，跳转到 `http://localhost:8080/saml/login`，应用发送认证请求，会跳转到 IDP 进行登录。

3. IDP 输入用户、密码后，点击登录后，跳转到应用的`http://localhost:8080/saml/SSO`，在 应用中消费断言。

4. 断言验证通过后，应用 触发 `/login` 接口，在接口中为响应添加一个 Cooike 用于帮助前端知道已经登录，然后重定向到应用首页。

5. 进入首页后，前端通过 `GET /web/user` 获取通过 SAML 登录的用户信息。

   同时，后台也可以从中获取到同样的用户信息，并将其保存，用于角色权限校验。

```json
用户信息：
{
    "userId":...
}
userId - 用户登录 IDP 时输入的帐号名（邮箱，用户名），不需要扩展返回数据即可获取
```

> 修复 BUG
>
> 情况：应用正常到达首页后，重启后台实例，重启后刷新当前页面，前端无法通过 /web/user 获取到用户信息，然后在页面上显示 `undefiled`，其余页面均没法正常获取数据。
>
> 原因：重启实例后，session 不一致，或者说当前页面的 session 已经无效。应用和 IDP 无法通过这个 session 建立联系，所以可以看到后台只有 应用 的认证请求日志，没有看到 IDP 的断言响应。
>
> 解决：在 Spring Security 中配置关于 seesion 无效后的跳转地址，设置为 `/#/`，重新跳转到 应用 的登录页。

**登出流程**

- 登出

  考虑：是否需要将本地登出与单点登出都做？

  答：做本地登出，登出后跳回到 应用 首页

  1. 在 应用 首页中，点击登出（本地登出），则跳转到 `http://localhost:8080/saml/logout?local=true` ，直接在本地登出。
  2. 登出后将会返回 `http://localhost:8080/`

- 切换帐号

考虑：当前已经将登出做成本地登出，但如此一来，用户在登录实例并退出后，将无法切换帐号，因为 IDP 一直没有真正退出。

答：做一个 “切换帐号” 的按钮，用来做单点登出

1. 在 应用 首页中，点击切换帐号（单点登出），跳转到 `http://localhost:8080/saml/logout` ，经过 应用 会跳转到 `http://localhost:8080/saml/SingleLogout`，到 IDP 登出。
2. 登出后将返回 `http://localhost:8080/`

**角色权限校验**

1. 在 Spring Security 中，角色权限的校验和控制是需要提前设定好的，比如：

   ```java
   http.authorizeRequests()
       .antMatchers("/web/xxx", ...).hasRole("WEBER")
   ```

   以上代码就表示 `/web/xxx"` 接口需要用户拥有 `WEBER` 这个角色才能进行访问。

   > 以上仅是对后台接口的限制，无法控制前端页面的跳转

2. 在用户通过 IDP 登录后，后台可以获取到 IDP 返回的用户信息，其中一定包含的用户登录时使用的用户名，所以，我们可以利用这个用户名，通过查询数据库，获得它与角色的关系，并将它拥有的角色授予这个用户。

3. 当用户被赋予有它拥有的角色后，用户再进行其他的访问，Spring Security 将自动为用户进行角色权限的校验和控制。

