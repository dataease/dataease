package io.dataease.service.staticResource;

import com.google.gson.Gson;
import io.dataease.commons.utils.FileUtils;
import io.dataease.commons.utils.LogUtil;
import io.dataease.commons.utils.StaticResourceUtils;
import io.dataease.controller.request.resource.StaticResourceRequest;
import io.dataease.plugins.common.exception.DataEaseException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.Base64Utils;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: wangjiahao
 * Date: 2022/4/24
 * Description:
 */
@Service
public class StaticResourceService {

    private final Path staticDir = Paths.get("/opt/dataease/data/static-resource/");

    public void upload(String fileId, MultipartFile file) {
        // check if the path is valid (not outside staticDir)
        Assert.notNull(file, "Multipart file must not be null");
        try {
            if (!isImage(file)) {
                DataEaseException.throwException("Multipart file must be image");
            }
            String originName = file.getOriginalFilename();
            String newFileName = fileId + originName.substring(originName.lastIndexOf("."), originName.length());
            Path uploadPath = Paths.get(staticDir.toString(), newFileName);
            // create dir is absent
            FileUtils.createIfAbsent(Paths.get(staticDir.toString()));
            Files.createFile(uploadPath);
            file.transferTo(uploadPath);
        } catch (IOException e) {
            LogUtil.error("文件上传失败", e);
            DataEaseException.throwException("文件上传失败");
        } catch (Exception e) {
            DataEaseException.throwException(e);
        }
    }

    private boolean isImage(MultipartFile file) {
        try (InputStream input = file.getInputStream()) {
            ImageIO.read(input);
        } catch (IOException e) {
            LogUtil.error(e.getMessage(), e);
            return false;
        }
        Pattern pattern = Pattern.compile("\\.(png|jpg|jpeg|gif|svg)$");
        Matcher matcher = pattern.matcher(file.getOriginalFilename().toLowerCase());
        if (!matcher.find()) {
            return false;
        }

        return true;
    }

    public void saveFilesToServe(String staticResource) {
        Gson gson = new Gson();
        if (StringUtils.isNotEmpty(staticResource)) {
            Map<String, String> resource = gson.fromJson(staticResource, Map.class);
            for (Map.Entry<String, String> entry : resource.entrySet()) {
                String path = entry.getKey();
                String fileName = path.substring(path.lastIndexOf("/") + 1, path.length());
                saveSingleFileToServe(fileName, entry.getValue());
            }
        }
    }

    public void saveSingleFileToServe(String fileName, String content) {
        Path uploadPath = Paths.get(staticDir.toString(), fileName);
        try {
            if (uploadPath.toFile().exists()) {
                LogUtil.info("file exists");
            } else {
                if (StringUtils.isNotEmpty(content)) {
                    Files.createFile(uploadPath);
                    FileCopyUtils.copy(Base64Utils.decodeFromString(content), Files.newOutputStream(uploadPath));
                }
            }
        } catch (Exception e) {
            LogUtil.error("template static resource save error" + e.getMessage());
        }
    }

    public Map<String, String> findResourceAsBase64(StaticResourceRequest resourceRequest) {
        Map<String, String> result = new HashMap<>();
        if (CollectionUtils.isNotEmpty(resourceRequest.getResourcePathList())) {
            for (String path : resourceRequest.getResourcePathList()) {
                String value = StaticResourceUtils.getImgFileToBase64(path.substring(path.lastIndexOf("/") + 1, path.length()));
                result.put(path, value);
            }
        }
        return result;
    }
}
