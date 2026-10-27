package io.dataease.visualization.server;

import io.dataease.api.visualization.StaticResourceApi;
import io.dataease.api.visualization.request.StaticResourceRequest;
import io.dataease.exception.DEException;
import io.dataease.utils.FileUtils;
import io.dataease.utils.JsonUtil;
import io.dataease.utils.LogUtil;
import io.dataease.utils.StaticResourceUtils;
import io.dataease.visualization.utils.SvgResourceValidator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

@RestController
@RequestMapping("/staticResource")
public class StaticResourceServer implements StaticResourceApi {

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(
            ".gif", ".svg", ".png", ".jpeg", ".jpg"
    );

    private static final Set<String> ALLOWED_SVG_MIME_TYPES = Set.of(
            "image/svg+xml",
            "image/svg-xml",
            "application/svg+xml"
    );

    @Value("${dataease.path.static-resource:/opt/dataease2.0/data/static-resource/}")
    private String staticDir;

    @Override
    public void upload(String fileId, MultipartFile file) {
        Assert.notNull(file, "Multipart file must not be null");
        try {
            FileUtils.validateUploadFilename(fileId);
            String originName = file.getOriginalFilename();
            validateImageFilename(originName);
            String newFileName = fileId + originName.substring(originName.lastIndexOf("."), originName.length());
            byte[] fileBytes = file.getBytes();
            validateImageContent(newFileName, fileBytes, file.getContentType(), true);
            writeFileIfAbsent(newFileName, fileBytes);
        } catch (IOException e) {
            LogUtil.error("文件上传失败", e);
            DEException.throwException("文件上传失败");
        } catch (Exception e) {
            DEException.throwException(e);
        }
    }

    public void saveFilesToServe(String staticResource) {
        if (StringUtils.isNotEmpty(staticResource)) {
            Map<String, String> resource = JsonUtil.parse(staticResource, Map.class);
            if (resource == null || resource.isEmpty()) {
                return;
            }
            for (Map.Entry<String, String> entry : resource.entrySet()) {
                String path = entry.getKey();
                String fileName = extractFileName(path);
                try {
                    saveSingleFileToServe(fileName, entry.getValue());
                } catch (Exception e) {
                    LogUtil.error(e);
                }
            }
        }
    }

    public void saveSingleFileToServe(String fileName, String content) {
        try {
            validateImageFilename(fileName);
            if (StringUtils.isEmpty(content)) {
                DEException.throwException("静态资源内容不能为空");
            }
            byte[] fileBytes = decodeBase64Content(content);
            validateImageContent(fileName, fileBytes, null, false);
            writeFileIfAbsent(fileName, fileBytes);
        } catch (DEException e) {
            throw e;
        } catch (Exception e) {
            LogUtil.error("template static resource save error", e);
            DEException.throwException(e);
        }
    }

    private void validateImageFilename(String filename) {
        if (StringUtils.isEmpty(filename)) {
            DEException.throwException("静态资源文件名不能为空");
        }
        FileUtils.validateUploadFilename(filename);
        if (!hasValidImageExtension(filename)) {
            DEException.throwException("静态资源必须是图片");
        }
    }

    private boolean hasValidImageExtension(String filename) {
        String lowerFilename = filename.toLowerCase(Locale.ROOT);
        for (String ext : ALLOWED_IMAGE_EXTENSIONS) {
            if (lowerFilename.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    private byte[] decodeBase64Content(String content) {
        Path basePath = Paths.get(staticDir.toString());
        try {
            FileUtils.createIfAbsent(basePath);
            return Base64.getDecoder().decode(content);
        } catch (IllegalArgumentException e) {
            DEException.throwException("静态资源Base64内容无效");
            return null;
        } catch (Exception e) {
            DEException.throwException(e);
            return null;
        }
    }

    private void writeFileIfAbsent(String fileName, byte[] content) throws IOException {
        Path basePath = Paths.get(staticDir.toString()).normalize();
        FileUtils.createIfAbsent(basePath);
        Path uploadPath = basePath.resolve(fileName).normalize();
        if (!uploadPath.startsWith(basePath)) {
            DEException.throwException("非法文件路径");
        }
        if (Files.exists(uploadPath)) {
            LogUtil.info("file exists");
            return;
        }
        Files.write(uploadPath, content, StandardOpenOption.CREATE_NEW);
    }

    private void validateImageContent(String fileName, byte[] content, String mimeType, boolean checkSvgMimeType) {
        if (content == null || content.length == 0) {
            DEException.throwException("静态资源内容不能为空");
        }
        if (isSvgFile(fileName)) {
            validateSvgContent(content, mimeType, checkSvgMimeType);
            return;
        }
        if (!isImageOther(content)) {
            DEException.throwException("静态资源必须是图片");
        }
    }

    private boolean isSvgFile(String fileName) {
        return StringUtils.endsWithIgnoreCase(fileName, ".svg");
    }

    private boolean isImageOther(byte[] content) {
        try (InputStream input = new ByteArrayInputStream(content)) {
            BufferedImage image = ImageIO.read(input);
            return image != null && image.getWidth() > 0 && image.getHeight() > 0;
        } catch (IOException e) {
            LogUtil.error(e.getMessage(), e);
            return false;
        }
    }

    private void validateSvgContent(byte[] content, String mimeType, boolean checkMimeType) {
        if (checkMimeType && !isValidSvgMimeType(mimeType)) {
            DEException.throwException("无效的SVG文件MIME类型");
        }
        SvgResourceValidator.validate(content);
    }

    @Override
    public Map<String, String> findResourceAsBase64(StaticResourceRequest resourceRequest) {
        Map<String, String> result = new HashMap<>();
        if (CollectionUtils.isNotEmpty(resourceRequest.getResourcePathList())) {
            for (String path : resourceRequest.getResourcePathList()) {
                String value = StaticResourceUtils.getImgFileToBase64(extractFileName(path));
                result.put(path, value);
            }
        }
        return result;
    }

    private String extractFileName(String path) {
        return StringUtils.substringAfterLast(StringUtils.replace(path, "\\", "/"), "/");
    }

    private boolean isValidSvgMimeType(String contentType) {
        if (contentType == null) {
            return false;
        }
        return ALLOWED_SVG_MIME_TYPES.contains(contentType.toLowerCase(Locale.ROOT));
    }

    public static FileType getFileType(InputStream is) throws IOException {
        byte[] src = new byte[28];
        is.read(src, 0, 28);
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v).toUpperCase();
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        FileType[] fileTypes = FileType.values();
        for (FileType fileType : fileTypes) {
            if (stringBuilder.toString().startsWith(fileType.getValue())) {
                return fileType;
            }
        }
        return null;
    }

    private static Boolean isImageCheckType(MultipartFile file) {
        try {
            return getFileType(file.getInputStream()) != null;
        } catch (Exception e) {
            LogUtil.error(e.getMessage());
            return false;
        }
    }

    public static String getImageType(InputStream fileInputStream) {
        byte[] b = new byte[10];
        int l = -1;
        try {
            l = fileInputStream.read(b);
            fileInputStream.close();
        } catch (Exception e) {
            return null;
        }
        if (l == 10) {
            byte b0 = b[0];
            byte b1 = b[1];
            byte b2 = b[2];
            byte b3 = b[3];
            byte b6 = b[6];
            byte b7 = b[7];
            byte b8 = b[8];
            byte b9 = b[9];
            if (b0 == (byte) 'G' && b1 == (byte) 'I' && b2 == (byte) 'F') {
                return "gif";
            } else if (b1 == (byte) 'P' && b2 == (byte) 'N' && b3 == (byte) 'G') {
                return "png";
            } else if (b6 == (byte) 'J' && b7 == (byte) 'F' && b8 == (byte) 'I' && b9 == (byte) 'F') {
                return "jpg";
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
