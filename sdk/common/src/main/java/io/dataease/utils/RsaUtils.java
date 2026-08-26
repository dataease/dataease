package io.dataease.utils;


import io.dataease.exception.DEException;
import io.dataease.model.RSAModel;
import io.dataease.rsa.dao.entity.CoreRsa;
import io.dataease.rsa.manage.RsaManage;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
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


    private static final int MAX_ENCRYPT_BLOCK = 245;

    private static final int MAX_DECRYPT_BLOCK = 256;

    private static final String PK_SEPARATOR = "-pk_separator-";

    private static RsaManage rsaManage;

    private static String staticAesKey;

    public static void initAesKey(String aesKey) {
        RsaUtils.staticAesKey = aesKey;
    }

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
        generator.initialize(2048);
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
            CoreRsa coreRsa = rsaManage.query();
            return encrypt(data, getPublicKey(coreRsa.getPublicKey()));
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

    private static String generateAesKey() {
        return RandomStringUtils.randomAlphanumeric(16);
    }

    private static byte[] deriveIv(String key) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(key.getBytes(StandardCharsets.UTF_8));
            byte[] iv = new byte[16];
            System.arraycopy(hash, 0, iv, 0, 16);
            return iv;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String ascEncrypt(String message, String key) {
        try {
            byte[] baseKey = key.getBytes(StandardCharsets.UTF_8);
            byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);

            byte[] ivBytes = deriveIv(key);
            IvParameterSpec ivps = new IvParameterSpec(ivBytes);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            SecretKey keySpec = new SecretKeySpec(baseKey, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivps);
            byte[] data = cipher.doFinal(messageBytes);

            return Base64.getEncoder().encodeToString(data);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }

    }


    private static final String ALGORITHM = "AES";

    public static String generateSymmetricKey() {
        return Base64.getEncoder().encodeToString(staticAesKey.getBytes(StandardCharsets.UTF_8));
    }

    public static String symmetricEncrypt(String data) {
        try {
            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec secretKeySpec = new SecretKeySpec(staticAesKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] ciphertext = cipher.doFinal(data.getBytes("UTF-8"));

            byte[] ivAndCipher = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, ivAndCipher, 0, iv.length);
            System.arraycopy(ciphertext, 0, ivAndCipher, iv.length, ciphertext.length);

            return Base64.getEncoder().encodeToString(ivAndCipher);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String symmetricDecrypt(String data) {
        try {
            byte[] decoded = Base64.getDecoder().decode(data);

            byte[] ivBytes = new byte[16];
            System.arraycopy(decoded, 0, ivBytes, 0, 16);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(ivBytes);

            byte[] ciphertext = new byte[decoded.length - 16];
            System.arraycopy(decoded, 16, ciphertext, 0, ciphertext.length);

            SecretKeySpec secretKeySpec = new SecretKeySpec(staticAesKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] decryptedText = cipher.doFinal(ciphertext);
            return new String(decryptedText, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
