package io.dataease.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import static io.dataease.constant.StaticResourceConstants.*;

public class StaticResourceUtils {

    private final static String FILE_BASE_PATH = USER_HOME + FILE_SEPARATOR + UPLOAD_URL_PREFIX;

    private static final Pattern SAFE_RESOURCE_FILE_NAME = Pattern.compile("^[A-Za-z0-9._-]+$");

    private static final Set<String> ALLOWED_RESOURCE_EXTENSIONS = Set.of(
            ".gif", ".svg", ".png", ".jpeg", ".jpg"
    );

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

    /**
     * @param imgFile local storage path
     * @return
     */
    public static String getImgFileToBase64(String imgFile) {
        if (StringUtils.isBlank(imgFile) || !SAFE_RESOURCE_FILE_NAME.matcher(imgFile).matches()) {
            LogUtil.warn("Reject illegal static resource file name: " + imgFile);
            return null;
        }
        if (!hasAllowedExtension(imgFile)) {
            LogUtil.warn("Reject static resource with disallowed extension: " + imgFile);
            return null;
        }
        Path targetPath = resolveSafeResourcePath(imgFile);
        if (targetPath == null) {
            return null;
        }
        if (!Files.isRegularFile(targetPath)) {
            LogUtil.warn("Reject static resource that is not a regular file: " + imgFile);
            return null;
        }
        //Convert the picture file into byte array  and encode it with Base64
        InputStream inputStream = null;
        byte[] buffer = null;
        //Read picture byte array
        try {
            inputStream = Files.newInputStream(targetPath);
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
                    LogUtil.error(e);
                }
            }
        }
        // Encode byte array as Base64
        if (buffer != null) {
            return Base64.getEncoder().encodeToString(buffer);
        } else {
            return null;
        }
    }

    private static Path resolveSafeResourcePath(String fileName) {
        try {
            Path basePath = Paths.get(FILE_BASE_PATH).toAbsolutePath().normalize();
            Path targetPath = basePath.resolve(fileName).normalize();
            if (!targetPath.startsWith(basePath)) {
                LogUtil.warn("Reject static resource path outside base directory: " + fileName);
                return null;
            }
            return targetPath;
        } catch (Exception e) {
            LogUtil.error(e);
            return null;
        }
    }

    private static boolean hasAllowedExtension(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        for (String ext : ALLOWED_RESOURCE_EXTENSIONS) {
            if (lower.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }
}
