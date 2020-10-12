# RSA

## 在Java中使用RSA加密

我使用hutool工具类

### maven 导包

```xml
<dependency>
    <groupId>cn.hutool</groupId>
    <artifactId>hutool-all</artifactId>
    <version>5.3.9</version>
</dependency>
```

### 生成公钥和私钥

- 公钥下发
- 私钥保存在服务器

> 规定未公钥加密，私钥解密

```java
RSA rsa = new RSA();

//获得私钥
rsa.getPrivateKey();
rsa.getPrivateKeyBase64();
//获得公钥
rsa.getPublicKey();
rsa.getPublicKeyBase64();
```

出来一下公钥，下发到客户端

```text
-----BEGIN RSA PUBLIC KEY-----
MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCia0sSMFbNnZUTMoH515h8N2GMwLojGcGgLdoq/
nFeNRkqwPZLdTfLseRA4v1b0VOUHUri4nl9atIzAobCu565tCpBxrN9SkwmmuG2SuxlmF9jwf6Cvg
KKuff8HzdkYY2bv7i5aLNNhAKnvStX5Pu5sojTui4cB2pdENpRma5U9wIDAQAB
-----END RSA PUBLIC KEY-----
```

私钥保存在服务器

```text
-----BEGIN RSA PRIVATE KEY-----
MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAKJrSxIwVs2dlRMygfnXmHw3YYzAuiMZwaAt2ir+
cV41GSrA9kt1N8ux5EDi/VvRU5QdSuLieX1q0jMChsK7nrm0KkHGs31KTCaa4bZK7GWYX2PB/oK+Aoq59/wfN2Rh
jZu/uLlos02EAqe9K1fk+7myiNO6LhwHal0Q2lGZrlT3AgMBAAECgYA8t8eUwSVyMJruVk9oy0RiC4IYUGRFgvdz
njCVKhHPmiISbZillOiK6bHe3/y/On34PQslzmyEik6SaztdyS4IvQRpCY+3vA9Wo0yxuCssfWYNajTpbLL4iaCw
NGmwmCHIZ2Safk3BXBcBk7193NnmDsrEf2PW5Oq2XO/ALTPSYQJBAOOT2RCsYSkfsiNegiX6Usc3+tP8fXYgPsZa
XAK6vh55dyFZwzGpLj8KANK/f17856jhk2nELbU9/DLt1KbD6/0CQQC2tDAj/CVTtvA9IW6w/pBISZyA0AlcJp6+
OsJJ8OpJaCWxa1r/Enwe5DJGwhiHwNk4Kd7y6a1cux5087+6kyUDAkEA4GWPOkaOw0ryjreIo0PxzWggVlh3qTtg
SpsccMCL6Gailer5cgU8iYImj6etQw8iqb5LaZW78CM/g0RJU2qKTQJACaYIaM4XFo2xsDjExLoc2oRwrGjQJbqw
ZXFq80ayyL+kRfNaceADCAqbERuM+hZYIlwrtv5aNmx3VkvNE0hUCwJBALZH8Kr56skQ6oTYJZ50pnDplyAqPa7z
4ND0ToQ4M2hl6szKB2VGWh2Pogx7/LhmeTM8fBBNwkZoFNttoNFTHds=
-----END RSA PRIVATE KEY-----
```

> 注意，这里我的公钥和私钥是我自己处理过的，生成的是一句不带BEGIN和end以及回车的

### 加密解密

```java
// 处理私钥
String rider_rsa_private = readFile("rider_rsa_private.pem");
rider_rsa_private = rider_rsa_private.replaceAll("(-----BEGIN RSA PRIVATE KEY-----)|(-----END RSA PRIVATE KEY-----)|([\n\r]*)","");
// 处理公钥
String pu = readFile("rider_ras_public.pem");
pu = pu.replaceAll("(-----BEGIN RSA PUBLIC KEY-----)|(-----END RSA PUBLIC KEY-----)|([\n\r]*)","");
// 私钥加密
RSA rsa = new RSA(null, pu);
byte[] encrypt = rsa.encrypt("我是CS嗷嗷".getBytes(), KeyType.PublicKey);
// 转为base64
String mw = Base64.encodeBase64String(encrypt);

// 私钥解密
// base64字符串转为byte数组
byte[] jm = Base64.decodeBase64(mw);
// 私钥解密
RSA rsa1 = new RSA(rider_rsa_private, null);
byte[] decrypt = rsa1.decrypt(jm, KeyType.PrivateKey);
// 输出
System.out.println(new String(decrypt));
```

上面代码输出为```我是CS嗷嗷```
