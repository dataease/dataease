package io.dataease.plugins.common.util;


import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class GlobalFileUtil {


    private static String root;

    @Value("${dataease.busiFilePath:/opt/dataease/data/business/}")
    public void setRoot(String root) {
        GlobalFileUtil.root = root;
    }

    private static final String SPLITOR = "-de-";

    public static String upload(byte[] bytes, String suffix) {
        String dateStr = null;
        try {
            dateStr = GlobalDateUtils.getDateString(new Date());
            String formatRoot = formatRoot();
            String dirPath = formatRoot + dateStr;
            File dirFile = new File(dirPath);
            if (!dirFile.exists()) {
                dirFile.mkdirs();
            }
            String fileId = fileIdByDate(dateStr);
            String filePath = formatRoot + dateStr + "/" + fileId + "." + suffix;
            File file = new File(filePath);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytes);
            fos.close();
            return fileId;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ResponseEntity<byte[]> showPicture(String fileId) {
        String filePath = filePathById(fileId, null);
        byte[] bytes = FileUtil.readBytes(filePath);
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    public static ResponseEntity<ByteArrayResource> down(String fileId, String fileName) throws Exception{
        String suffix = FileUtil.getSuffix(fileName);
        String filePath = filePathById(fileId, suffix);
        byte[] bytes = FileUtil.readBytes(filePath);
        ByteArrayResource bar = new ByteArrayResource(bytes);
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        ContentDisposition contentDisposition = ContentDisposition.parse("attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));
        headers.setContentDisposition(contentDisposition);
        return new ResponseEntity<>(bar, headers, HttpStatus.OK);
    }

    private static String formatRoot() {
        if (!StringUtils.endsWith(root, "/")) {
            return root + "/";
        }
        return root;
    }

    private static String dateByFileId(String fileId) {
        return fileId.substring(0, 10);
    }

    private static String fileIdByDate(String dateStr) {
        dateStr = StringUtils.isBlank(dateStr) ? GlobalDateUtils.formatDate(new Date()) : dateStr;

        String uuid = UUID.randomUUID().toString().replace("-", "");
        return dateStr + SPLITOR + uuid;
    }



    public static String filePathById(String fileId, String suffix) {
        String dateStr = dateByFileId(fileId);
        String dirPath = formatRoot() + dateStr;
        if (!FileUtil.exist(dirPath)) {
            throw new RuntimeException("dir path is not exist [" + dirPath + "]");
        }
        if (StringUtils.isNotBlank(suffix)) {
            String filePath = dirPath + "/" + fileId + "." + suffix;
            if (!FileUtil.exist(filePath)) {
                throw new RuntimeException("file path is not exist [" + filePath + "]");
            }
            return filePath;
        }
        List<String> fileNames = FileUtil.listFileNames(dirPath);
        assert fileNames != null;
        for (String fileName : fileNames) {
            String prefix = FileUtil.getPrefix(fileName);
            if (StringUtils.equals(fileId, prefix)) {
                return dirPath + "/" + fileName;
            }
        }
        return null;
    }


}
