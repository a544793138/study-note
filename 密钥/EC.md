# EC 密钥

这是一个基于曲线的密钥，所以会出现有关曲线上点（Point）以及一个点的 X、Y 值的参数。

- 将仅包含 X、Y 的 EC 公钥变为 PKCS8 格式（包含 OID）
```java
    private final static Provider PROVIDER = new BouncyCastleProvider();

    private byte[] becomeECPublicKey(byte[] publicKey, String curveName) {
        try {
            final ECParameterSpec ecParameterSpec = ECNamedCurveTable.getParameterSpec(curveName);
            // 04 表示未压缩
            byte[] uncompressed = Hex.decode("04");
            byte[] uncompressedKeyValue = new byte[publicKey.length + uncompressed.length];
            System.arraycopy(uncompressed, 0, uncompressedKeyValue, 0, uncompressed.length);
            System.arraycopy(publicKey, 0, uncompressedKeyValue, uncompressed.length, publicKey.length);
            ECPoint ecPoint = ecParameterSpec.getCurve().decodePoint(uncompressedKeyValue);
            KeyFactory keyFactory = KeyFactory.getInstance("EC", PROVIDER);
            return keyFactory.generatePublic(new ECPublicKeySpec(ecPoint, ecParameterSpec)).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("Get EC publicKey Failed." + e);
        }
    }
```

- 从 PKCS8 格式的 EC 公钥中获取纯公钥（仅包含 X、Y）
```java
    KeyFactory keyFactory = KeyFactory.getInstance("EC");
    // value 是 PKCS8 格式的 EC 公钥的byte[]
    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(value);
    final PublicKey publicKey = keyFactory.generatePublic(keySpec);
    getECPublicKey(((ECPublicKey) publicKey).getW())

    public byte[] getECPublicKey(ECPoint w) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(w.getAffineX().toByteArray());
            outputStream.write(w.getAffineY().toByteArray());
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to assemble EC public key.", e);
        }
    }
```