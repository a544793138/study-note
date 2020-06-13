## 登录流程（单点登录 / SSO）

1. 启动项目后， 打开页面 http://localhost:8080，该页面不受 SP 保护，不需要登录
2. 在页面中点击 `Here` 后，跳转到 `localhost:8080/saml/login`，demo 会向 IDP 发送认证请求，跳转到 OKTA（作为 IDP） 进行登录
3. 在 OKTA 登录成功后，IDP 会发送断言到 SP，会跳转到系统的 `localhost:8080/saml/SSO` 消费断言
4. SP 验证断言成功后，会触发 `/home` 接口，跳转到 `localhost:8080/home` 页面

## 登出流程

分为**本地登出**和**单点登出（SLO）**

1. 在 `localhost:8080/home` 页面中：
- 点击**本地登出**，则跳转到 http://localhost:8080/saml/logout?local=true ，直接在本地登出。
- 点击**单点登出**，则跳转到 http://localhost:8080/saml/logout ，经过系统会跳转到 http://localhost:8080/saml/SingleLogout，到 IDP 登出。
2. 登出后将会返回 http://localhost:8080/

> **本地登出**：直接退出本地的 session，但是没有在 IDP 那边退出，所以当再次登录时，在 IDP 无需再次输入用户、密码，直接进行登录。
>
> **单点登出**：在 IDP 那边退出，所以当再次登录时，在 IDP 那边需要输入用户、密码，才能进行登录。

## OKTA IDP 设置流程

> 本 demo 不给予 IDP 的帐号、密码，请自行搭建，很简单

1. OKTA 可以免费申请到开发者帐号，[链接](https://developer.okta.com/)。
2. 通过 [OKTA 的帮助文档](https://help.okta.com/en/prod/Content/Topics/Apps/Apps_App_Integration_Wizard_SAML.htm?cshid=ext_Apps_App_Integration_Wizard-saml) 完成 SAML IDP 的搭建。（搭建好后，记得将应用分配给用户（可以是自己），这样才能进行使用）

3. 最终配置好的样子

> KEY 分别是什么意思？直接翻译一下就出来了，都是 OKTA 配置时 / 配置后的项，完成配置后，在 `General` 一页可以看到以下相关的 KEY

| KEY                            | VALUE                                                        |
| ------------------------------ | ------------------------------------------------------------ |
| **Single Sign On URL**         | http://localhost:8080/saml/SSO                               |
| **Recipient URL**              | http://localhost:8080/saml/SSO                               |
| **Destination URL**            | http://localhost:8080/saml/SSO                               |
| **Audience Restriction**       | xxx（SP 的唯一标识，就是一个字符串，通常有几部分组成，使用 `:` 分隔） |
| **Default Relay State**        | http://localhost:8080/home                                   |
| **Name ID Format**             | Unspecified                                                  |
| **Response**                   | Signed                                                       |
| **Assertion Signature**        | Signed                                                       |
| **Signature Algorithm**        | RSA_SHA256                                                   |
| **Digest Algorithm**           | SHA256                                                       |
| **Assertion Encryption**       | Unencrypted                                                  |
| **SAML Single Logout**         | Enabled                                                      |
| **Signature Certificate**      | xxx（用于签名的证书，公钥）                                  |
| **authnContextClassRef**       | PasswordProtectedTransport                                   |
| **Honor Force Authentication** | Yes                                                          |
| **SAML Issuer ID**             | （默认即可）                                                 |
| **SP Issuer**                  | xxx（SP 的唯一标识，就是一个字符串，通常有几部分组成，使用 `:` 分隔） |

4. 完成配置后，在 `Sign On` 中可以找到 `View Setup Instructions`，点击它。打开的页面中就是刚刚新搭建的 IDP 的一些数据。其中：
   - **Identity Provider Single Sign-On URL** - 当你启动 SP（即本系统）后，直接在浏览器中打开该地址，可以用来测试单点登录的功能（也就是会触发单点登录的功能）。
   - **Identity Provider Single Logout URL** - 与上一点同理，但这是单点登出的功能。
   - **Identity Provider Issuer** - IDP 的标识，暂时不知道有啥用，只知道 IDP 元数据中包含它，可以用它来判断元数据是否属于对应的 IDP。
   - **X.509 Certificate** - 证书，好像是签名用的证书，不是你上传那张，是 OKTA 自己提供的
   - **Provide the following IDP metadata to your SP provider** - 这个就是 IDP 的元数据了，这个内容就是系统中 `idp-okta.xml` 中的内容，如果搭建了自己的 IDP ，则将该内容复制到 `idp-okta.xml` 中进行替换，系统就会知道往你搭建的 IDP 发送认证请求等内容了。