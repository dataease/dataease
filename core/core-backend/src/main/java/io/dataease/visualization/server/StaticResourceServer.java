package io.dataease.visualization.server;

import io.dataease.api.visualization.StaticResourceApi;
import io.dataease.api.visualization.request.StaticResourceRequest;
import io.dataease.exception.DEException;
import io.dataease.utils.FileUtils;
import io.dataease.utils.JsonUtil;
import io.dataease.utils.LogUtil;
import io.dataease.utils.StaticResourceUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/staticResource")
public class StaticResourceServer implements StaticResourceApi {

    @Value("${dataease.path.static-resource:/opt/dataease3.0/data/static-resource/}")
    private String staticDir;

    @Override
    public void upload(String fileId, MultipartFile file) {
        // check if the path is valid (not outside staticDir)
        Assert.notNull(file, "Multipart file must not be null");
        try {
            FileUtils.validateUploadFilename(fileId);
            String originName = file.getOriginalFilename();
            FileUtils.validateUploadFilename(originName);
            String newFileName = fileId + originName.substring(originName.lastIndexOf('.'));
            byte[] content;
            try (InputStream input = file.getInputStream()) {
                content = input.readNBytes(StaticResourceContentGuard.MAX_BYTES + 1);
            }
            StaticResourceContentGuard.validate(newFileName, content);
            Path basePath = Paths.get(staticDir.toString());
            // create dir is absent
            FileUtils.createIfAbsent(basePath);
            Path uploadPath = basePath.resolve(newFileName);
            Files.write(uploadPath, content, java.nio.file.StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            LogUtil.error("文件上传失败", e);
            DEException.throwException(io.dataease.i18n.Translator.get("i18n_static_resource_invalid"));
        } catch (Exception e) {
            DEException.throwException(io.dataease.i18n.Translator.get("i18n_static_resource_invalid"));
        }
    }

    public void saveFilesToServe(String staticResource) {
        if (StringUtils.isNotEmpty(staticResource)) {
            Map<String, String> resource = JsonUtil.parse(staticResource, Map.class);
            for (Map.Entry<String, String> entry : resource.entrySet()) {
                String path = entry.getKey();
                String fileName = extractFileName(path);
                saveSingleFileToServe(fileName, entry.getValue());
            }
        }
    }

    public void saveSingleFileToServe(String fileName, String content) {
        try {
            FileUtils.validateUploadFilename(fileName);
            if (content == null || content.length() > ((StaticResourceContentGuard.MAX_BYTES + 2L) / 3) * 4) {
                throw new IOException("Invalid static resource size");
            }
            byte[] decoded = Base64.getDecoder().decode(content);
            StaticResourceContentGuard.validate(fileName, decoded);
            Path basePath = Paths.get(staticDir).toAbsolutePath().normalize();
            FileUtils.createIfAbsent(basePath);
            Path uploadPath = basePath.resolve(fileName);
            if (Files.isSymbolicLink(uploadPath)) {
                throw new IOException("Invalid static resource path");
            }
            if (!Files.exists(uploadPath)) {
                Files.write(uploadPath, decoded, java.nio.file.StandardOpenOption.CREATE_NEW);
            }
        } catch (Exception e) {
            LogUtil.error("template static resource save error", e);
            DEException.throwException(io.dataease.i18n.Translator.get("i18n_static_resource_invalid"));
        }
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
