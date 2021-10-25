package io.dataease.commons.utils;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RsaUtil {

    public static final String CHARSET = "UTF-8";
    public static final String RSA_ALGORITHM = "RSA";


    /**
     * 创建RSA 公钥-私钥
     */
    public static RsaKey createKeys() throws NoSuchAlgorithmException {
        return createKeys(1024);
    }

    /**
     * 创建RSA 公钥-私钥
     */
    public static RsaKey createKeys(int keySize) throws NoSuchAlgorithmException {
        //为RSA算法创建一个KeyPairGenerator对象
        KeyPairGenerator kpg = KeyPairGenerator.getInstance(RSA_ALGORITHM);

        //初始化KeyPairGenerator对象,密钥长度
        kpg.initialize(keySize);
        //生成密匙对
        KeyPair keyPair = kpg.generateKeyPair();

        //得到公钥
        Key publicKey = keyPair.getPublic();

        String publicKeyStr = new String(Base64.encodeBase64(publicKey.getEncoded()));

        //得到私钥
        Key privateKey = keyPair.getPrivate();
        String privateKeyStr = new String(Base64.encodeBase64(privateKey.getEncoded()));

        RsaKey rsaKey = new RsaKey();
        rsaKey.setPublicKey(publicKeyStr);
        rsaKey.setPrivateKey(privateKeyStr);

        return rsaKey;
    }


    /**
     * 公钥加密
     *
     * @param originalText 原文
     * @param publicKey    公钥
     */
    public static String publicEncrypt(String originalText, String publicKey) throws NoSuchAlgorithmException {
        RSAPublicKey rsaPublicKey = getPublicKey(publicKey);
        return publicEncrypt(originalText, rsaPublicKey);
    }

    /**
     * 公钥解密
     *
     * @param cipherText 密文
     * @param publicKey  公钥
     */
    public static String publicDecrypt(String cipherText, String publicKey) throws NoSuchAlgorithmException {
        RSAPublicKey rsaPublicKey = getPublicKey(publicKey);
        return publicDecrypt(cipherText, rsaPublicKey);
    }

    /**
     * 私钥加密
     *
     * @param originalText 原文
     * @param privateKey   私钥
     */
    public static String privateEncrypt(String originalText, String privateKey) throws NoSuchAlgorithmException {
        RSAPrivateKey rsaPrivateKey = getPrivateKey(privateKey);
        return privateEncrypt(originalText, rsaPrivateKey);
    }


    /**
     * 私钥解密
     *
     * @param cipherText 密文
     * @param privateKey 私钥
     */
    public static String privateDecrypt(String cipherText, String privateKey) throws NoSuchAlgorithmException {
        RSAPrivateKey rsaPrivateKey = getPrivateKey(privateKey);
        return privateDecrypt(cipherText, rsaPrivateKey);
    }


    /**
     * 得到公钥
     *
     * @param publicKey 密钥字符串（经过base64编码）
     */
    private static RSAPublicKey getPublicKey(String publicKey) throws NoSuchAlgorithmException {
        //通过X509编码的Key指令获得公钥对象
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);

        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(Base64.decodeBase64(publicKey));
        RSAPublicKey key = null;
        try {
            key = (RSAPublicKey) keyFactory.generatePublic(x509KeySpec);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return key;
    }

    /**
     * 公钥加密
     *
     * @param originalText 原文
     * @param publicKey    公钥
     */
    private static String publicEncrypt(String originalText, RSAPublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return Base64.encodeBase64URLSafeString(rsaSplitCodec(cipher, Cipher.ENCRYPT_MODE, originalText.getBytes(CHARSET), publicKey.getModulus().bitLength()));
        } catch (Exception e) {
            throw new RuntimeException("加密字符串[" + originalText + "]时遇到异常", e);
        }
    }

    /**
     * 得到私钥
     *
     * @param privateKey 密钥字符串（经过base64编码）
     */
    private static RSAPrivateKey getPrivateKey(String privateKey) throws NoSuchAlgorithmException {
        //通过PKCS#8编码的Key指令获得私钥对象
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);

        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(privateKey));
        RSAPrivateKey key = null;
        try {
            key = (RSAPrivateKey) keyFactory.generatePrivate(pkcs8KeySpec);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return key;
    }


    /**
     * 私钥解密
     *
     * @param cipherText 密文
     * @param privateKey 私钥
     */

    private static String privateDecrypt(String cipherText, RSAPrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return new String(rsaSplitCodec(cipher, Cipher.DECRYPT_MODE, Base64.decodeBase64(cipherText), privateKey.getModulus().bitLength()), CHARSET);
        } catch (Exception e) {
            throw new RuntimeException("解密字符串[" + cipherText + "]时遇到异常", e);
        }
    }

    private static String privateEncrypt(String originalText, RSAPrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            return Base64.encodeBase64URLSafeString(rsaSplitCodec(cipher, Cipher.ENCRYPT_MODE, originalText.getBytes(CHARSET), privateKey.getModulus().bitLength()));
        } catch (Exception e) {
            throw new RuntimeException("加密字符串[" + originalText + "]时遇到异常", e);
        }
    }


    private static String publicDecrypt(String cipherText, RSAPublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            return new String(rsaSplitCodec(cipher, Cipher.DECRYPT_MODE, Base64.decodeBase64(cipherText), publicKey.getModulus().bitLength()), CHARSET);
        } catch (Exception e) {
            throw new RuntimeException("解密字符串[" + cipherText + "]时遇到异常", e);
        }
    }

    private static byte[] rsaSplitCodec(Cipher cipher, int opmode, byte[] datas, int keySize) {
        int maxBlock;
        if (opmode == Cipher.DECRYPT_MODE) {
            maxBlock = keySize / 8;
        } else {
            maxBlock = keySize / 8 - 11;
        }
        int offSet = 0;
        byte[] buff;
        int i = 0;
        try (
                ByteArrayOutputStream out = new ByteArrayOutputStream()
        ) {
            while (datas.length > offSet) {
                if (datas.length - offSet > maxBlock) {
                    buff = cipher.doFinal(datas, offSet, maxBlock);
                } else {
                    buff = cipher.doFinal(datas, offSet, datas.length - offSet);
                }
                out.write(buff, 0, buff.length);
                i++;
                offSet = i * maxBlock;
            }
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("加解密阀值为[" + maxBlock + "]的数据时发生异常", e);
        }
    }

}