# RSA 密钥相关

- 将不包含 OID 的 RSA 公钥字符串，拆分出 modulus 和 expoent 值

```java
    @Test
    public void test_pk() throws IOException {
        System.out.println(Hex.toHexString(Base64.getDecoder().decode("MIIBCgKCAQEApDvNDZ4aOAwevPjsXeVJUtxeh3r9BBZXIAQkcfHSaEg4wdHMaR8ix0vbOM7tt5MPCemP8mT40U9tJwdS7056Mn0B+ZHxrkWe+W+tMgYqJaOVEs2IIeJXIh+9pE86+XvDI1p0Ls0cbU76J/D2H1lk+e+mTugtF9e8EffapISmUwtIbAanx+M+o6Z1BZixllQAkZKg6o9YNjukEaAL9lAkqCcGcsmUfEs20UFAEVpng8v2z6MMsiTNEUUH/Gd8s7KG6Mmi7rbnUSiu+ildqWob3dPhMEol73zVFamVV1o295mV6Jh7zoICZWp8+zCUjJgDjTYDPQ85KA7YqYpy36RwrQIDAQAB")));

        final ASN1Sequence sequence = (ASN1Sequence) DERSequence.fromByteArray(Base64.getDecoder().decode("MIIBCgKCAQEApDvNDZ4aOAwevPjsXeVJUtxeh3r9BBZXIAQkcfHSaEg4wdHMaR8ix0vbOM7tt5MPCemP8mT40U9tJwdS7056Mn0B+ZHxrkWe+W+tMgYqJaOVEs2IIeJXIh+9pE86+XvDI1p0Ls0cbU76J/D2H1lk+e+mTugtF9e8EffapISmUwtIbAanx+M+o6Z1BZixllQAkZKg6o9YNjukEaAL9lAkqCcGcsmUfEs20UFAEVpng8v2z6MMsiTNEUUH/Gd8s7KG6Mmi7rbnUSiu+ildqWob3dPhMEol73zVFamVV1o295mV6Jh7zoICZWp8+zCUjJgDjTYDPQ85KA7YqYpy36RwrQIDAQAB")).toASN1Primitive();
        final ASN1Encodable modulus = sequence.getObjectAt(0);
        System.out.println(Hex.toHexString(((ASN1Integer)modulus.toASN1Primitive()).getValue().toByteArray()));
        System.out.println(Base64.getEncoder().encodeToString(((ASN1Integer)modulus.toASN1Primitive()).getValue().toByteArray()));
        final ASN1Encodable expoent = sequence.getObjectAt(1);
        System.out.println(Hex.toHexString(((ASN1Integer)expoent.toASN1Primitive()).getValue().toByteArray()));
    }
```

- 根据证书，获取其中的 RSA 公钥密钥（ECC 密钥也是类似的）
```java
        try {
            final CertificateFactory factory = CertificateFactory.getInstance("X.509");
            final ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(certificate.replace("-----BEGIN CERTIFICATE-----", "")
                    .replace("-----END CERTIFICATE-----", "").replace("\r", "").replace("\n", "")));
            // 生成了公钥，如果证书是 RSA 密钥，那么这里 publicKey 就可以强转为 RSAPublicKey
            final PublicKey publicKey = factory.generateCertificate(inputStream).getPublicKey();
            return Optional.ofNullable(publicKey).map(function).orElseThrow(() -> new BadRequestException("Parse certificate failed: [%s]", certificate));
        } catch (Exception e) {
            throw new BadRequestException("Parse certificate failed.", e);
        }

        key = publicKey
        if (key instanceof RSAPublicKey) {
            return getRSAPublicKey(((RSAPublicKey) key).getModulus().toByteArray(), ((RSAPublicKey) key).getPublicExponent().toByteArray());
        }

    // 这个方法是用来生成不包含 OID 的 RSA 公钥值
    public byte[] getRSAPublicKey(byte[] m, byte[] e) {
        ASN1EncodableVector vector = new ASN1EncodableVector();
        vector.add(new ASN1Integer(new BigInteger(1, m)));
        vector.add(new ASN1Integer(new BigInteger(1, e)));
        DERSequence sequence = new DERSequence(vector);
        try {
            return sequence.getEncoded();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to assemble RSA public key using m and e.", ex);
        }
    }
```

其中，`new BigInteger(1, m)` 是为了确保 m 为正整数。