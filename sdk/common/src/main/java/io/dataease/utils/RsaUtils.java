package io.dataease.utils;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.crypto.symmetric.AES;
import io.dataease.model.RSAModel;
import io.dataease.rsa.dao.entity.CoreRsa;
import io.dataease.rsa.manage.RsaManage;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;

import java.nio.charset.StandardCharsets;

@Component
public class RsaUtils {

    private static final String PK_SEPARATOR = "-pk_separator-";

    private static RsaManage rsaManage;

    @Resource
    public void setRsaManage(RsaManage rsaManage) {
        RsaUtils.rsaManage = rsaManage;
    }

    public static RSAModel generate() {
        RSA rsa = new RSA();
        String privateKeyBase64 = rsa.getPrivateKeyBase64();
        String publicKeyBase64 = rsa.getPublicKeyBase64();
        RSAModel rsaModel = new RSAModel();
        rsaModel.setPrivateKey(privateKeyBase64);
        rsaModel.setPublicKey(publicKeyBase64);
        rsaModel.setAesKey(generateAesKey());
        return rsaModel;
    }

    public static String decryptStr(String data, String privateKey) {
        RSA rsa = new RSA(privateKey, null);
        return rsa.decryptStr(data, KeyType.PrivateKey);
    }

    public static String decryptStr(String data) {
        return decryptStr(data, privateKey());
    }

    public static String privateKey() {
        CoreRsa coreRsa = rsaManage.query();
        return coreRsa.getPrivateKey();
    }

    public static String publicKey() {
        CoreRsa coreRsa = rsaManage.query();
        String publicKey = coreRsa.getPublicKey();
        String aesKey = coreRsa.getAesKey();
        // Security.addProvider(new BouncyCastleProvider());
        String pk = ascEncrypt(publicKey, aesKey);
        String separator = Base64Utils.encodeToUrlSafeString(PK_SEPARATOR.getBytes(StandardCharsets.UTF_8));
        return pk + separator + aesKey;
    }

    private static final String IV_KEY = "0000000000000000";
    private static String generateAesKey() {
        return RandomUtil.randomString(16);
    }
    private static String ascEncrypt(String message, String key) {
        byte[] baseKey = key.getBytes(StandardCharsets.UTF_8);
        byte[] ivBytes = IV_KEY.getBytes(StandardCharsets.UTF_8);
        AES aes = new AES("CBC", "PKCS7Padding", baseKey, ivBytes);
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        return Base64Utils.encodeToString(aes.encrypt(messageBytes));
    }


}
