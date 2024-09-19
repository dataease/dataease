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
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/staticResource")
public class StaticResourceServer implements StaticResourceApi {

    private final Path staticDir = Paths.get("/opt/dataease2.0/data/static-resource/");

    @Override
    public void upload(String fileId, MultipartFile file) {
        // check if the path is valid (not outside staticDir)
        Assert.notNull(file, "Multipart file must not be null");
        try {
            if (!isImage(file)) {
                DEException.throwException("Multipart file must be image");
            }
            String originName = file.getOriginalFilename();
            String newFileName = fileId + originName.substring(originName.lastIndexOf("."), originName.length());
            Path basePath = Paths.get(staticDir.toString());
            // create dir is absent
            FileUtils.createIfAbsent(basePath);
            Path uploadPath = basePath.resolve(newFileName);
            Files.createFile(uploadPath);
            file.transferTo(uploadPath);
        } catch (IOException e) {
            LogUtil.error("文件上传失败", e);
            DEException.throwException("文件上传失败");
        } catch (Exception e) {
            DEException.throwException(e);
        }
    }

    private boolean isImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        String mimeType = file.getContentType();
        if (StringUtils.isEmpty(mimeType)) {
            return false;
        }
        // 判断是否为图片或SVG
        return (mimeType != null && mimeType.startsWith("image/")) || isValidSVG(file);
    }

    public void saveFilesToServe(String staticResource) {
        if (StringUtils.isNotEmpty(staticResource)) {
            Map<String, String> resource = JsonUtil.parse(staticResource, Map.class);
            for (Map.Entry<String, String> entry : resource.entrySet()) {
                String path = entry.getKey();
                String fileName = path.substring(path.lastIndexOf("/") + 1, path.length());
                saveSingleFileToServe(fileName, entry.getValue());
            }
        }
    }

    public void saveSingleFileToServe(String fileName, String content) {
        Path basePath = Paths.get(staticDir.toString());
        Path uploadPath = basePath.resolve(fileName);
        try {
            if (Files.exists(uploadPath)) {
                LogUtil.info("file exists");
            } else {
                if (StringUtils.isNotEmpty(content)) {
                    Files.createFile(uploadPath);
                    FileCopyUtils.copy(Base64.getDecoder().decode(content), Files.newOutputStream(uploadPath));
                }
            }
        } catch (Exception e) {
            LogUtil.error("template static resource save error" + e.getMessage());
        }
    }

    @Override
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

    @Override
    public Map<String, String> urlTest() {
        return null;
    }

    private static boolean isValidSVG(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try (InputStream inputStream = file.getInputStream()) {
            // 禁用外部实体解析以防止XXE攻击
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(inputStream);

            // 检查根元素是否是<svg>
            if ("svg".equals(doc.getDocumentElement().getNodeName())) {
                return true;
            } else {
                return false;
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            // 如果出现任何解析错误，说明该文件不是合法的SVG
            return false;
        }
    }
}
