package io.dataease.utils;


import io.dataease.exception.DEException;
import io.dataease.model.RSAModel;
import io.dataease.rsa.dao.entity.CoreRsa;
import io.dataease.rsa.manage.RsaManage;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class RsaUtils {

    static {
        if (ObjectUtils.isNotEmpty(Security.getProvider("BC"))) {
            Security.removeProvider("BC");
        }
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }


    private static final int MAX_ENCRYPT_BLOCK = 117;

    private static final int MAX_DECRYPT_BLOCK = 128;

    private static final String PK_SEPARATOR = "-pk_separator-";

    private static RsaManage rsaManage;

    @Resource
    public void setRsaManage(RsaManage rsaManage) {
        RsaUtils.rsaManage = rsaManage;
    }

    private static KeyPair getKeyPair() {
        KeyPairGenerator generator = null;
        try {
            generator = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            LogUtil.error(e.getMessage(), e);
            DEException.throwException(e);
        }
        generator.initialize(1024);
        return generator.generateKeyPair();
    }

    private static PrivateKey getPrivateKey(String privateKey) {
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
            byte[] decodedKey = Base64.getDecoder().decode(privateKey.getBytes());
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private static PublicKey getPublicKey(String publicKey) {
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
            byte[] decodedKey = Base64.getDecoder().decode(publicKey.getBytes());
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private static String encrypt(String data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        int inputLen = data.getBytes().length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offset = 0;
        byte[] cache;
        int i = 0;
        while (inputLen - offset > 0) {
            if (inputLen - offset > MAX_ENCRYPT_BLOCK) {
                cache = cipher.doFinal(data.getBytes(), offset, MAX_ENCRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(data.getBytes(), offset, inputLen - offset);
            }
            out.write(cache, 0, cache.length);
            i++;
            offset = i * MAX_ENCRYPT_BLOCK;
        }
        byte[] encryptedData = out.toByteArray();
        out.close();
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    private static String decrypt(String data, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] dataBytes = Base64.getDecoder().decode(data);
        int inputLen = dataBytes.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offset = 0;
        byte[] cache;
        int i = 0;
        while (inputLen - offset > 0) {
            if (inputLen - offset > MAX_DECRYPT_BLOCK) {
                cache = cipher.doFinal(dataBytes, offset, MAX_DECRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(dataBytes, offset, inputLen - offset);
            }
            out.write(cache, 0, cache.length);
            i++;
            offset = i * MAX_DECRYPT_BLOCK;
        }
        out.close();
        return out.toString(StandardCharsets.UTF_8);
    }

    public static RSAModel generate() {
        KeyPair keyPair = getKeyPair();
        String privateKey = new String(Base64.getEncoder().encode(keyPair.getPrivate().getEncoded()));
        String publicKey = new String(Base64.getEncoder().encode(keyPair.getPublic().getEncoded()));
        RSAModel rsaModel = new RSAModel();
        rsaModel.setPrivateKey(privateKey);
        rsaModel.setPublicKey(publicKey);
        rsaModel.setAesKey(generateAesKey());
        return rsaModel;
    }

    public static String decryptStr(String data, String privateKey) {
        try {
            return decrypt(data, getPrivateKey(privateKey));
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public static String decryptStr(String data) {
        return decryptStr(data, privateKey());
    }

    public static String encryptStr(String data) {
        try {
            return encrypt(data, getPublicKey(publicKey()));
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public static String privateKey() {
        CoreRsa coreRsa = rsaManage.query();
        return coreRsa.getPrivateKey();
    }

    public static String publicKey() {
        CoreRsa coreRsa = rsaManage.query();
        String publicKey = coreRsa.getPublicKey();
        String aesKey = coreRsa.getAesKey();
        String pk = ascEncrypt(publicKey, aesKey).replaceAll("[\\s*\t\n\r]", "");
        String separator = Base64.getUrlEncoder().encodeToString(PK_SEPARATOR.getBytes(StandardCharsets.UTF_8));
        return pk + separator + aesKey;
    }

    private static final String IV_KEY = "0000000000000000";

    private static String generateAesKey() {
        return RandomStringUtils.randomAlphanumeric(16);
    }

    private static String ascEncrypt(String message, String key) {
        Cipher cipher = null;
        try {
            byte[] baseKey = key.getBytes(StandardCharsets.UTF_8);
            byte[] ivBytes = IV_KEY.getBytes(StandardCharsets.UTF_8);
            byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
            cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            SecretKey keySpec = new SecretKeySpec(baseKey, "AES");
            IvParameterSpec ivps = new IvParameterSpec(ivBytes);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivps);
            byte[] data = cipher.doFinal(messageBytes);
            return Base64.getEncoder().encodeToString(data);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }

    }
}
