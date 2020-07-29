# Spring Security SAML Token

在看这篇笔记前，请先看 [Spring Security JWT](Spring Security JWT.md) 。
因为两者的道理是一样的，同样是 Spring Security 为系统提供用户认证和授权功能，而用于认证和授权的信息载体，从 JWT 换成了 SAML token。
 
> 这里的 SAML token 其实是 SAML 响应 / SAML 断言，如果不清楚的可以先去了解一下 SAML 规范。
> 
> 这里简单说明一下，正常使用的 SAML 规范定义了 SP 和 IDP，SP 就是提供服务的，相当于你的系统，IDP 就是提供身份验证。
> 
> 当用户调用 SP 中受保护的资源时，SP 会发送一个认证请求到 IDP，然后用户在 IDP 那边完成用户认证后，IDP 将认证响应（SAML 响应，其中就包含必要的断言）
> 返回给 SP，SP 对响应进行验证和处理，获得用户信息，允许用户访问受保护资源。

所以，这里将主要说明 SAML token 验证，使用的是 OpenSAML API。Spring Security SAML 中底层也是使用该依赖库对 SAML 进行操作的。

## SAML 签名和验签

- 虽然 SAML 是 XML 标准的，但是似乎与正常的 XML 签名、验签过程有些不同。这里的 SAML 也是指定 SAML 2.0 而言的。
- 某些情况下，SAML 断言（`<Assertion>`）可能包含在 SAML 响应（`<Response>`）中，断言是响应的子元素。断言和响应都可以被签名，
其中签名需要使用到的算法、转换规则等信息，都被放到各自的 `<Reference>`  中，签名得到的值被放到各自的 `<SignatureValue>` 中。
那么我们接下来将 **只讨论 SAML 断言的相关**，但签名 / 验签的过程对于 SAML 响应同样适用。

**SAML 签名**

1. 对 `<Assertion>` 进行摘要（此时还未包含 `<Signature>` 元素），在 `<Assertion>` 中建立新元素 `<Signature>`，并将使用到的摘要算法、转换方法存放到 `<Signature>` 中对应的地方，将摘要值存放到 `<DigestValue>` 中。
2. 根据 `<Reference>` 元素中指定的 XML 数据的规范化方法对 `<SignedInfo>` 规范化，并使用私钥对 `<SignedInfo>` 进行签名，
   将最后得到的签名值存放到 `<SignatureValue>` 中。

**SAML 验签**

XML 数字签名的验证主要包括两个步骤：验证签名值、验证摘要值。
其中任何一步验证失败则表示整个 XML 验签失败。

1.验证签名值：需要根据 `<Reference>` 对 `<SignedInfo>` 进行转化（整个 `<SignedInfo>` 为待验签值），并从 `<KeyInfo>` 中获取到用于验签的公钥证书，从 `<SignatureValue>` 中得知签名值
  ，利用以上这些进行验签。

2. 验证摘要值：去掉 `<Assertion>` 的 `<Signature>`，需要根据 `<Reference>` 对 `<Assertion>` 进行转化，对整个 `<Assertion>` 计算根据 `<DigestMethod>` 计算出摘要值，将其与 `<DigestValue>` 进行比较，匹配则验证成功，否则失败。

上述是 SAML token 签名和验签的流程，但实际上 Spring Security SAML 中应该有对应 API 的封装，
所以在接下来就是研究如何将 SAML token 转换为 Spring Security 的 SAML token / OpenSAML 相应的对象，从而调用其封装好的验签 API。

但是，经过研究，Spring Security SAML 是一套完善且独立的依赖库，它虽然使用 OpenSAML API 对 SAML 进行操作，但是对其有一定的封装，
想要直接使用到 Spring Security SAML 中的 API 过于复杂，但是依然可以阅读 Spring Security SAML 中对 SAML 操作的代码，来学习是怎么验证的。
所以我们决定研究 OpenSAML API 的使用。

## SAML 验签实践 - [spring-security-saml-token-demo](https://github.com/a544793138/spring-security-saml-token-demo.git)

**！！经过研究和下面将会说明的验签实践，要求 SAML token XML 必须是紧凑的，即不能存在换行，空格（标签中的属性之间除外）等，否则验签失败！！**
> 在项目 demo 中，有针对 XML 的空格和换行的处理，但是没有使用 XML 的 API，是自己写的，可能存在 BUG，但一般应该没有问题。

- 手动验证 SAML 断言签名 - 参考项目 demo 的单元测试 `com.tjwoods.spring.security.saml.token.SamlTest#verifySignForSaml()`
- 使用 OpenSAML API 验签（没有使用信任引擎） - 参考项目 demo 的单元测试 `com.tjwoods.spring.security.saml.token.SamlTest#verifySignForSamlByOpenSAML()`

> 信任引擎（TrustEngine）是 OpenSAML 中的术语，由于 SAML 断言中可以找到用于验签的公钥证书，所以最基础的验签就是使用该公钥证书对自身的 SAML 断言进行验签，
> 但明显这只能证明 SAML 断言没有被篡改，但无法证明这个 SAML 断言是否可信，无法证明这个 SAML 来自预期的源，上面两个验签例子就是这种情况。
>
> 为了解决这个问题，就需要系统本身有信任的证书，使用这个信任的证书来验证 SAML 断言，而非直接使用其中包含的公钥证书。
> 那么，对于这个信任证书的来源，就对应有不同的 TrustEngine 类。
>
> 最经典的就是正常 SAML 流程中需要使用到的 IDP 元数据的来源途径，因为 IDP 元数据中也包含一个公钥证书
> （如果 SAML 断言不是伪造的，是从正常 SAML 和 IDP 交互得来的话，SAML 断言中包含的公钥证书和 IDP 元数据中的公钥证书应该是一致的，
> 最起码 SAML 断言中包含的公钥证书是被IDP 元数据中的公钥证书信任的 ），所以会使用 IDP 元数据中的公钥证书来验签 SAML 断言。
>
> 但我们这里明显不是正常的 SAML，而是使用 SAML 作为 token 而已，所以我们得另外找到对应的办法。

## 验证 SAML token 有效期

验证 SAML token 中的 Conditions 元素即可。从 Spring Security SAML 学到的。其中 getTimeSkew() 其实是一个允许的时间偏差，一个 int 数值，单位为秒。
```xml
<saml:Conditions NotBefore="2020-07-23T01:00:37Z" NotOnOrAfter="2020-07-23T09:01:37Z">
        ...
</saml:Conditions>
```

```java
/**
* 验证 SAML token 的 Conditions 中规定的时效，允许 {@code getTimeSkew()} 的时间偏差
*
* @param conditions SAML token 的 Conditions
* @throws SAMLException SAML token 失效
*/
void verifyTime(Conditions conditions) throws SAMLException {
    if (conditions.getNotBefore() != null) {
        // conditions 中 NotBefore 的时间，再往前推一个时间误差
        if (conditions.getNotBefore().minusSeconds(getTimeSkew()).isAfterNow()) {
            throw new SAMLException("Assertion is not yet valid, invalidated by condition notBefore " + conditions.getNotBefore());
        }
    }
    if (conditions.getNotOnOrAfter() != null) {
        // // conditions 中 NotOnOrAfter 的时间，再往后推一个时间误差
        if (conditions.getNotOnOrAfter().plusSeconds(getTimeSkew()).isBeforeNow()) {
            throw new SAMLException("Assertion is no longer valid, invalidated by condition notOnOrAfter " + conditions.getNotOnOrAfter());
        }
    }
}
```

## 验证 SAML token Issuer

验证 SAML token 中的 Issuer，通常来说是 SAML token 的颁发者的意思。

```java
/**
 * 验证 SAML token 的 Issuer 是否为指定的颁发者，验证失败时抛出异常
 *
 * @param issuer SAML token 的 Issuer
 * @throws SAMLException 验证失败，SAML token 中 Issuer 格式错误 / 颁发者不是预期
 */
void verifyIssuer(Issuer issuer) throws SAMLException {
    if (issuer.getFormat() != null && !issuer.getFormat().equals(NameIDType.ENTITY)) {
        throw new SAMLException("Issuer invalidated by issuer type " + issuer.getFormat());
    }
    if (!"exampleIssuer".equals(issuer.getDOM().getTextContent())) {
        throw new SAMLException("Issuer invalidated by issuer value " + issuer.getDOM().getTextContent() + " doesn't equal exampleIssuer");
    }
}
```