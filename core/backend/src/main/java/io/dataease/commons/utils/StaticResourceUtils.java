package io.dataease.commons.utils;

import io.dataease.plugins.common.exception.DataEaseException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.Base64Utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static io.dataease.commons.constants.StaticResourceConstants.*;

/**
 * Author: wangjiahao
 * Date: 2022/4/24
 * Description:
 */
public class StaticResourceUtils {

    private final static String FILE_BASE_PATH = USER_HOME + FILE_SEPARATOR + UPLOAD_URL_PREFIX;

    private static final String FILE_NAME_REGEX_PATTERN = "^[A-Za-z0-9.-]{1,255}$";

    public static String ensureBoth(@NonNull String string, @NonNull String bothfix) {
        return ensureBoth(string, bothfix, bothfix);
    }

    public static String ensureBoth(@NonNull String string, @NonNull String prefix,
                                    @NonNull String suffix) {
        return ensureSuffix(ensurePrefix(string, prefix), suffix);
    }

    /**
     * Ensures the string contain prefix.
     *
     * @param string string must not be blank
     * @param prefix prefix must not be blank
     * @return string contain prefix specified
     */
    public static String ensurePrefix(@NonNull String string, @NonNull String prefix) {
        Assert.hasText(string, "String must not be blank");
        Assert.hasText(prefix, "Prefix must not be blank");

        return prefix + StringUtils.removeStart(string, prefix);
    }


    /**
     * Ensures the string contain suffix.
     *
     * @param string string must not be blank
     * @param suffix suffix must not be blank
     * @return string contain suffix specified
     */
    public static String ensureSuffix(@NonNull String string, @NonNull String suffix) {
        Assert.hasText(string, "String must not be blank");
        Assert.hasText(suffix, "Suffix must not be blank");

        return StringUtils.removeEnd(string, suffix) + suffix;
    }

    public static boolean validateStringFilenameUsingRegex(String filename) {
        if (filename == null) {
            return false;
        }
        return filename.matches(FILE_NAME_REGEX_PATTERN);
    }

    /**
     * @param imgFile local storage path
     * @return
     */
    public static String getImgFileToBase64(String imgFile) {
        if (!validateStringFilenameUsingRegex(imgFile)) {
            DataEaseException.throwException("Illegal File Name");
        }
        //Convert the picture file into byte array  and encode it with Base64
        InputStream inputStream = null;
        byte[] buffer = null;
        //Read picture byte array
        try {
            inputStream = new FileInputStream(FILE_BASE_PATH + FILE_SEPARATOR + imgFile);
            int count = 0;
            while (count == 0) {
                count = inputStream.available();
            }
            buffer = new byte[count];
            inputStream.read(buffer);
        } catch (IOException e) {
            LogUtil.error(e);
        } catch (Exception e) {
            LogUtil.error(e);
        } finally {
            if (inputStream != null) {
                try {
                    // Close InputStream
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        // Encode byte array as Base64
        if (buffer != null) {
            return Base64Utils.encodeToString(buffer);
        } else {
            return null;
        }
    }

}
