# KeyStore

这其实是 Java 的一个安全类。

伴随它常见的还有 `.keytool、.jks` 格式的文件。

### keytool

keytool 其实是 JDK 的一个安全密钥管理工具，它并不是接口、类等代码，而是一个命令行工具。
在命令行中使用特定的命令就可以将密钥保存到其中，最后就会产生一个 `.keytool` 格式的文件。

对于 `.keytool` 文件而言，它很像一个用来存放密钥的库，在打开这个库时，我们可能需要它的密码才能打开，通常称为 keyStorePass。 

keytool 中可以存在多个密钥 / 密钥对，可以会只有公钥，没有私钥。也可能会有多个公钥，一把私钥等等的情况。
而打开私钥同样需要密码，这个密钥通常称为 keyPass。

### jks

`.jks` 格式的文件，其实和 `.keytool` 文件一样。

`.jks` 文件更多的用在代码中使用 KeyStore 来读取其中的密钥。

一般常用的方法有：

```java
// 创建新 keyStore，并加入 JKS，需要使用到 keyStorePass
// 注意 keyStorePass 尽量不要使用 String ，而是使用 char[]。
FileInputStream jksFile = new FileInputStream("mykeystore.jks");
char[] keyStorePass = {...};
KeyStore keyStore = KeyStore.getInstance("JKS");
// 这其实就是打开 keyStore
keyStore.load(jksFile, keyStorePass);


// 读取 X509 格式的公钥证书
CertificateFactory cf = CertificateFactory.getInstance("X509");
FileInputStream certFile = new FileInputStream("publicKeyCert.cert");
// 这里可以强转为 X509Certificate
Certificate cert = cf.generateCertificate(certFile);


// 读取 .key.der 文件格式的私钥证书，PKCS8，RSA
private static final BouncyCastleProvider BC = new BouncyCastleProvider();
FileInputStream fileInputStream = new FileInputStream("privateKeyCert.key.der");
byte[] keyBytes = new byte[fileInputStream.available()];
fileInputStream.read(keyBytes);
PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(keyBytes);
// 这里可以不使用 BC
KeyFactory keyFactory = KeyFactory.getInstance("RSA", BC);
// 可以强转为 RSAPrivateKey
return keyFactory.generatePrivate(privateKeySpec);


// 如果是读取 .key 文件格式的私钥明文证书，PKCS8，RSA
private static final BouncyCastleProvider BC = new BouncyCastleProvider();
FileInputStream fileInputStream = new FileInputStream("privateKeyCert.key");
byte[] keyBytes = new byte[fileInputStream.available()];
final String pvKey = new String(keyBytes);
// 需要先去掉一些标识，然后 Base64 解码后再生成 PrivateKey
final String base64 = pvKey.replaceAll("\r", "").replaceAll("\n", "")
            .replaceAll(FLAG_START, "").replaceAll(FLAG_END, "");
keyBytes = Base64.getDecoder().decode(base64);
PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(keyBytes);
// 这里一定要使用 BC，否则会报错，如果没有报错，当我没说
KeyFactory keyFactory = KeyFactory.getInstance("RSA", BC);
// 可以强转为 RSAPrivateKey
return keyFactory.generatePrivate(privateKeySpec);


// 将上面读取的公钥、私钥加入到 keyStore 中
char[] keyPass = {};
KeyStore.PasswordProtection pass = new KeyStore.PasswordProtection(keyPass);
// 上面读取的公钥
Certificate[] certificateChain = {cert};
// 上面读取的私钥
keyStore.setEntry(alias, new KeyStore.PrivateKeyEntry(privateKey, certificateChain), pass);


// 遍历 keyStore 中所有 aliases，这样可以找到 keyStore 中的私钥
final Enumeration<String> aliases = ks.aliases();
while (aliases.hasMoreElements()) {
    final String alias = aliases.nextElement();
    // 查看该 alias 是否为私钥
    if (ks.entryInstanceOf(alias, KeyStore.PrivateKeyEntry.class)) {
        // 这个key 就是私钥，所以可以在这里强转为 PrivateKey
        final Key key = ks.getKey(alias, keyPassword);
        Checker.checkState(key != null, "Can't get private key in keystore with the given key password.");
        return createJKSKeyManager(ks, Collections.singletonMap(alias, new String(keyPassword)), alias);
    }
}
```