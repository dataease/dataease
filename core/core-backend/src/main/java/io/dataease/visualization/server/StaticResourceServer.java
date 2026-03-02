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
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/staticResource")
public class StaticResourceServer implements StaticResourceApi {

    @Value("${dataease.path.static-resource:/opt/dataease2.0/data/static-resource/}")
    private String staticDir;

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
        if (!hasValidImageExtension(file.getOriginalFilename())) {
            return false;
        }
        // 判断是否为图片或SVG
        return (isImageOther(file)) || isValidSVG(file);
    }

    private boolean hasValidImageExtension(String filename) {
        if (StringUtils.isEmpty(filename)) {
            return false;
        }
        // 转换为小写进行比较
        String lowerFilename = filename.toLowerCase();
        // 允许的图片后缀名列表
        Set<String> allowedExtensions = Set.of(
                ".gif", ".svg", ".png", ".jpeg", ".jpg"
        );

        for (String ext : allowedExtensions) {
            if (lowerFilename.endsWith(ext)) {
                return true;
            }
        }

        return false;
    }

    private boolean isImageOther(MultipartFile file) {
        BufferedImage image = null;
        try (InputStream input = file.getInputStream()) {
            image = ImageIO.read(input);
        } catch (IOException e) {
            LogUtil.error(e.getMessage(), e);
            return false;
        }
        if (image == null || image.getWidth() <= 0 || image.getHeight() <= 0) {
            return false;
        }
        return true;
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

    private static boolean isValidSVG(MultipartFile file){
        if (file == null || file.isEmpty()) {
            return false;
        }

        // MIME类型预检查
        if (!isValidSvgMimeType(file)) {
            DEException.throwException("无效的SVG文件MIME类型");
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

            // 启用安全解析设置
            dbf.setXIncludeAware(false);
            dbf.setExpandEntityReferences(false);

            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(inputStream);

            // 检查根元素是否是<svg>
            if (!"svg".equals(doc.getDocumentElement().getNodeName())) {
                DEException.throwException("根元素必须是svg");
                return false;
            }

            // 安全检查：如果发现任何危险内容，直接返回false
            if (containsDangerousContent(doc)) {
                DEException.throwException("SVG包含不允许的脚本或事件处理器");
                return false;
            }

            return true;

        } catch (ParserConfigurationException | SAXException | IOException e) {
            // 如果出现任何解析错误，说明该文件不是合法的SVG
            if(e.getMessage() != null && e.getMessage().contains("DOCTYPE")){
                DEException.throwException("svg 内容禁止使用 DOCTYPE");
            } else {
                DEException.throwException("SVG解析失败: " + e.getMessage());
            }
        }
        return false;
    }

    /**
     * MIME类型检查
     */
    private static boolean isValidSvgMimeType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }

        // 允许的SVG MIME类型
        Set<String> allowedMimeTypes = new HashSet<>(Arrays.asList(
                "image/svg+xml",
                "image/svg-xml",
                "application/svg+xml"
        ));

        return allowedMimeTypes.contains(contentType.toLowerCase());
    }

    /**
     * 检查SVG是否包含危险内容
     * @return true 包含危险内容，false 安全
     */
    private static boolean containsDangerousContent(Document doc) {
        // 危险的事件处理器属性
        Set<String> dangerousAttributes = new HashSet<>(Arrays.asList(
                "onload", "onerror", "onclick", "ondblclick", "onmousedown", "onmouseup",
                "onmouseover", "onmousemove", "onmouseout", "onfocus", "onblur",
                "onkeydown", "onkeypress", "onkeyup", "onsubmit", "onreset",
                "onchange", "onselect", "onabort", "onunload", "onresize",
                "onscroll", "oninput", "onactivate", "onbeforeactivate", "onbeforedeactivate",
                "ondeactivate", "onbegin", "onend", "onrepeat", "onloadstart",
                "onprogress", "onloadend", "oncanplay", "oncanplaythrough", "onwaiting",
                "onseeking", "onseeked", "ontimeupdate", "onplaying", "onpause",
                "onratechange", "ondurationchange", "onvolumechange"
        ));

        // 危险标签
        Set<String> dangerousTags = new HashSet<>(Arrays.asList(
                "script", "style", "object", "embed", "applet", "iframe",
                "frame", "frameset", "link", "meta", "base", "form"
        ));

        // 遍历所有元素
        NodeList elements = doc.getElementsByTagName("*");
        for (int i = 0; i < elements.getLength(); i++) {
            Element element = (Element) elements.item(i);
            String tagName = element.getTagName().toLowerCase();

            // 检查危险标签
            if (dangerousTags.contains(tagName)) {
                return true; // 发现危险标签，返回true表示包含危险内容
            }

            // 检查属性
            NamedNodeMap attributes = element.getAttributes();
            for (int j = 0; j < attributes.getLength(); j++) {
                Attr attr = (Attr) attributes.item(j);
                String attrName = attr.getName().toLowerCase();
                String attrValue = attr.getValue().toLowerCase();

                // 检查事件处理器属性
                if (dangerousAttributes.contains(attrName)) {
                    return true;
                }

                // 检查属性名是否以"on"开头（事件处理器）
                if (attrName.startsWith("on") && attrName.length() > 2) {
                    return true;
                }

                // 检查属性值是否包含JavaScript相关代码
                if (containsJavaScript(attrValue)) {
                    return true;
                }
            }
        }

        // 检查文本节点
        if (containsScriptInText(doc)) {
            return true;
        }

        return false;
    }

    /**
     * 检查字符串是否包含JavaScript代码
     */
    private static boolean containsJavaScript(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }

        String lowerValue = value.toLowerCase();

        // JavaScript相关模式
        String[] jsPatterns = {
                "javascript:", "vbscript:", "data:", "expression(",
                "eval(", "alert(", "confirm(", "prompt(",
                "document.", "window.", "location.", "cookie.",
                "function(", "new function", "settimeout(", "setinterval(",
                "innerhtml", "outerhtml", "insertadjacenthtml",
                "<script", "</script", "&#", "\\u"
        };

        for (String pattern : jsPatterns) {
            if (lowerValue.contains(pattern)) {
                return true;
            }
        }

        // 检查base64编码的潜在脚本
        if (lowerValue.contains("base64")) {
            // 简单的base64解码检查，这里可以根据需要实现更复杂的检测
            return lowerValue.matches(".*base64\\s*,\\s*[a-z0-9+/=]{20,}.*");
        }

        return false;
    }

    /**
     * 检查文本节点是否包含脚本
     */
    private static boolean containsScriptInText(Document doc) {
        NodeList allNodes = doc.getElementsByTagName("*");
        for (int i = 0; i < allNodes.getLength(); i++) {
            Node node = allNodes.item(i);

            // 检查元素的文本内容
            String textContent = node.getTextContent();
            if (textContent != null && !textContent.trim().isEmpty()) {
                String lowerText = textContent.toLowerCase();

                // 检查文本中是否包含HTML/XML标签
                if (lowerText.contains("<script") ||
                        lowerText.contains("</script>") ||
                        lowerText.contains("<?") ||
                        lowerText.contains("<!")) {
                    return true;
                }

                // 检查是否包含JavaScript代码
                if (containsJavaScript(lowerText)) {
                    return true;
                }
            }

            // 检查CDATA节点
            NodeList childNodes = node.getChildNodes();
            for (int j = 0; j < childNodes.getLength(); j++) {
                Node child = childNodes.item(j);
                if (child.getNodeType() == Node.CDATA_SECTION_NODE) {
                    String cdataContent = child.getTextContent().toLowerCase();
                    if (containsJavaScript(cdataContent) ||
                            cdataContent.contains("<script") ||
                            cdataContent.contains("</script")) {
                        return true;
                    }
                }
            }
        }

        return false;
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
