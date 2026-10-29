package io.dataease.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import static io.dataease.constant.StaticResourceConstants.*;

public class StaticResourceUtils {

    private final static String FILE_BASE_PATH = USER_HOME + FILE_SEPARATOR + UPLOAD_URL_PREFIX;

    private static final Pattern SAFE_RESOURCE_ID = Pattern.compile("[A-Za-z0-9_-]+");

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
        return getImgFileToBase64(Paths.get(FILE_BASE_PATH), imgFile);
    }

    public static String getImgFileToBase64(Path root, String imgFile) {
        try (InputStream input = openImgFile(root, imgFile)) {
            return input == null ? null : Base64.getEncoder().encodeToString(input.readAllBytes());
        } catch (IOException e) {
            LogUtil.error(e);
        }
        return null;
    }

    /** Opens an image from the existing export directory; the caller must close the stream. */
    public static InputStream openImgFile(String imgFile) {
        return openImgFile(Paths.get(FILE_BASE_PATH), imgFile);
    }

    /** Returns null for an unsupported, missing or unreadable resource. */
    public static InputStream openImgFile(Path root, String imgFile) {
        if (imgFile == null || !hasAllowedExtension(imgFile)) {
            return null;
        }
        try {
            Path targetPath = resolveResourcePath(root, imgFile);
            if (!Files.isRegularFile(targetPath, LinkOption.NOFOLLOW_LINKS)) {
                return null;
            }
            return Files.newInputStream(targetPath, StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS);
        } catch (IOException e) {
            LogUtil.error(e);
        }
        return null;
    }

    public static void validateResourceId(String fileId) throws IOException {
        if (fileId == null || !SAFE_RESOURCE_ID.matcher(fileId).matches()) {
            throw new IOException("Invalid static resource id");
        }
    }

    /** Validate legacy resource URLs before extracting their single file name. */
    public static String getResourceFileName(String reference) throws IOException {
        if (StringUtils.isBlank(reference) || reference.indexOf('\\') >= 0) {
            throw new IOException("Invalid static resource reference");
        }
        try {
            URI uri = URI.create(reference);
            String scheme = uri.getScheme();
            if (scheme != null && !scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https")) {
                throw new IOException("Invalid static resource reference");
            }
            // Never decode a second time or normalize away traversal supplied by the client.
            String path = uri.getRawPath();
            if (StringUtils.isBlank(path) || path.indexOf('%') >= 0) {
                throw new IOException("Invalid static resource reference");
            }
            for (String segment : path.split("/")) {
                if (segment.equals(".") || segment.equals("..")) {
                    throw new IOException("Invalid static resource reference");
                }
            }
            String prefix = "/" + UPLOAD_URL_PREFIX + "/";
            int prefixIndex = path.indexOf(prefix);
            String fileName;
            if (prefixIndex >= 0) {
                fileName = path.substring(prefixIndex + prefix.length());
            } else if (path.startsWith(UPLOAD_URL_PREFIX + "/")) {
                fileName = path.substring(UPLOAD_URL_PREFIX.length() + 1);
            } else if (path.indexOf('/') < 0) {
                fileName = path;
            } else {
                throw new IOException("Invalid static resource reference");
            }
            validateResourceFileName(fileName);
            return fileName;
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid static resource reference", e);
        }
    }

    /** The configured root may be a link, but individual resource files must not be. */
    public static Path resolveResourcePath(Path root, String fileName) throws IOException {
        validateResourceFileName(fileName);
        Path basePath = root.toRealPath();
        Path targetPath = basePath.resolve(fileName).normalize();
        if (!basePath.equals(targetPath.getParent()) || Files.isSymbolicLink(targetPath)) {
            throw new IOException("Invalid static resource path");
        }
        if (Files.exists(targetPath, LinkOption.NOFOLLOW_LINKS)
                && (!basePath.equals(targetPath.toRealPath().getParent())
                || !Files.isRegularFile(targetPath, LinkOption.NOFOLLOW_LINKS))) {
            throw new IOException("Invalid static resource path");
        }
        return targetPath;
    }

    private static void validateResourceFileName(String fileName) throws IOException {
        if (StringUtils.isBlank(fileName) || !SAFE_RESOURCE_FILE_NAME.matcher(fileName).matches()
                || fileName.contains("..") || fileName.startsWith("de-material-")
                || fileName.contains(".material-tmp")) {
            throw new IOException("Invalid static resource file name");
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
