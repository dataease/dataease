package io.dataease;


import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.crypto.symmetric.AES;
import io.dataease.utils.LogUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Base64Utils;

import java.nio.charset.StandardCharsets;


@SpringBootTest
public class RsaTest {

    private static final String PK_SEPARATOR = "-pk_separator-";

    @Test
    public void test() {
        RSA rsa = new RSA();
        String privateKeyBase64 = rsa.getPrivateKeyBase64();
        String publicKeyBase64 = rsa.getPublicKeyBase64();
        LogUtil.info("private is {}", privateKeyBase64);
        LogUtil.info("public is {}", publicKeyBase64);
        String data = "my name is cyw";
        String s = rsa.encryptBase64(data, KeyType.PublicKey);
        String s1 = rsa.decryptStr(s, KeyType.PrivateKey);
        LogUtil.info(s1);
    }

    @Test
    public void test1() {
        String separator = Base64Utils.encodeToUrlSafeString(PK_SEPARATOR.getBytes(StandardCharsets.UTF_8));
        String a = Base64Utils.encodeToString(PK_SEPARATOR.getBytes(StandardCharsets.UTF_8));
        System.out.println(separator);
        System.out.println(a);
        String aesKey = "0251434c0eca47977c50ef044f278cb9";
        String pk = "8OT02q8aALvMjvsr2Ws2PobmKVVgg3UjNN9r1jUKVilJMtn3fYAfe1oOozM3TTqxV9JmRA1MhV6Q/FoUfYN3mBIL4cDSYbEPSO+IRGTq4FmLZdBHMvUZilliMpt5+0vxXF43K3dZT3WStsK5usLfTKCcqZLLMZs0Wqo98XbEpgMZBMP+bKrolcpUfY06dwVqvgX9CN4ujYd8aTVXEq4/iVjX3opBGsbT8aD3Fb11ThXfAiQaUghsuT17NIfoV/P0uATIITQsrfmOkERDSmCz3wE0MfJVC7HQn9n8GXNkuOY=";
        byte[] bytes = HexUtil.decodeHex(aesKey);
        AES aes = new AES(bytes);

        String s = aes.decryptStr(pk);

        System.out.println(s);


    }
}
