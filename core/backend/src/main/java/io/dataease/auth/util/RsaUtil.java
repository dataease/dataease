package io.dataease.auth.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.ArrayUtils;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RsaUtil {

    /**
     * 私钥解密
     *
     * @param privateKeyText 私钥
     * @param text           待解密的文本
     * @return /
     * @throws Exception /
     */
    public static String decryptByPrivateKey(String privateKeyText, String text) throws Exception {
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec5 = new PKCS8EncodedKeySpec(Base64.decodeBase64(privateKeyText));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec5);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        // 下面该用分段加密
        byte[] result = null;
        byte[] b = Base64.decodeBase64(text);
        for (int i = 0; i < b.length; i += 64) {
            byte[] doFinal = cipher.doFinal(ArrayUtils.subarray(b, i, i + 64));
            result = ArrayUtils.addAll(result, doFinal);
        }
        return new String(result);
    }

    /**
     * 公钥加密
     *
     * @param publicKeyText 公钥
     * @param text          待加密的文本
     * @return /
     */
    public static String encryptByPublicKey(String publicKeyText, String text) throws Exception {
        X509EncodedKeySpec x509EncodedKeySpec2 = new X509EncodedKeySpec(Base64.decodeBase64(publicKeyText));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec2);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        // 下面该用分段加密
        byte[] result = null;
        byte[] b = text.getBytes("utf-8");
        for (int i = 0; i < b.length; i += 50) {
            byte[] doFinal = cipher.doFinal(ArrayUtils.subarray(b, i, i + 50));
            result = ArrayUtils.addAll(result, doFinal);
        }
        return Base64.encodeBase64String(result);
    }

}
