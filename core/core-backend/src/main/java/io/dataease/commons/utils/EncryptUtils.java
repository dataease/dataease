package io.dataease.commons.utils;

import io.dataease.utils.BeanUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.stream.Collectors;

public class EncryptUtils extends CodingUtil {

    private static final String OLD_SECRET_KEY = "www.fit2cloud.co";
    private static final String OLD_IV = "1234567890123456";

    private static volatile String secretKey;
    private static volatile String iv;
    private static volatile boolean initialized = false;

    public static void initKeys(String aesKey) {
        if (!initialized && StringUtils.isNotBlank(aesKey)) {
            secretKey = aesKey;
            iv = deriveIv(aesKey);
            initialized = true;
        }
    }

    private static String deriveIv(String key) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                hex.append(String.format("%02x", hash[i]));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object aesEncrypt(Object o) {
        if (o == null) {
            return null;
        }
        if (!initialized) {
            return aesEncrypt(o.toString(), OLD_SECRET_KEY, OLD_IV);
        }
        return aesEncrypt(o.toString(), secretKey, iv);
    }

    public static Object aesDecrypt(Object o) {
        if (o == null) {
            return null;
        }
        String str = o.toString();

        if (initialized) {
            String result = tryDecryptRaw(str, secretKey, iv);
            if (result != null) {
                return result;
            }
        }

        String result = tryDecryptRaw(str, OLD_SECRET_KEY, OLD_IV);
        if (result != null) {
            return result;
        }

        return str;
    }

    private static String tryDecryptRaw(String src, String key, String iv) {
        try {
            byte[] raw = key.getBytes(StandardCharsets.UTF_8);
            SecretKeySpec keySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] encrypted = Base64.decodeBase64(src);
            byte[] original = cipher.doFinal(encrypted);
            return new String(original, StandardCharsets.UTF_8);
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public static <T> Object aesDecrypt(List<T> o, String attrName) {
        if (o == null) {
            return null;
        }
        return o.stream()
                .filter(element -> BeanUtils.getFieldValueByName(attrName, element) != null)
                .peek(element -> BeanUtils.setFieldValueByName(element, attrName, aesDecrypt(BeanUtils.getFieldValueByName(attrName, element).toString()), String.class))
                .collect(Collectors.toList());
    }

    public static Object md5Encrypt(Object o) {
        if (o == null) {
            return null;
        }
        return md5(o.toString());
    }
}
