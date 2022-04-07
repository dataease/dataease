package io.dataease.commons.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.Arrays;

public class DeFileUtils {



    /**
     * Java文件操作 获取不带扩展名的文件名
     */
    public static String getFileNameNoEx(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }

    /**
     * 获取文件扩展名，不带 .
     */
    public static String getExtensionName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return filename;
    }

    public static void validateExist(String path) {
        File dir = new File(path);
        if (dir.exists()) return ;
        dir.mkdirs();
    }
    /**
     * 将文件名解析成文件的上传路径
     */
    public static File upload(MultipartFile file, String filePath) {
        String name = getFileNameNoEx(file.getOriginalFilename());
        String suffix = getExtensionName(file.getOriginalFilename());
        try {
            validateExist(filePath);
            String fileName = name  + "." + suffix;
            String path = filePath + fileName;
            // getCanonicalFile 可解析正确各种路径
            File dest = new File(path).getCanonicalFile();

            // 文件写入
            FileOutputStream fileOutputStream = new FileOutputStream(dest);
            fileOutputStream.write(file.getBytes());
            fileOutputStream.flush();
            fileOutputStream.close();
            return dest;
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
        return null;
    }

    public static String copy(File source, String targetDir) throws IOException{
        String name = source.getName();
        String destPath = null;
        if (targetDir.endsWith("/") || targetDir.endsWith("\\")){
            destPath = targetDir + name;
        }else{
            destPath = targetDir + "/" + name;
        }
        File DestFile = new File(destPath);
        if (!DestFile.getParentFile().exists()) {
            DestFile.getParentFile().mkdirs();
        }
        copyFileUsingFileChannels(source, DestFile);
        return destPath;
    }

    private static void copyFileUsingFileChannels(File source, File dest) throws IOException {
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        try {
            inputChannel = new FileInputStream(source).getChannel();
            outputChannel = new FileOutputStream(dest).getChannel();
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        } finally {
            inputChannel.close();
            outputChannel.close();
        }
    }

    public static String readJson(File file) {
        String str = null;
        try {
            FileReader fileReader = new FileReader(file);
            Reader reader = new InputStreamReader(new FileInputStream(file), "utf-8");
            int ch=0;
            StringBuffer sb = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            fileReader.close();
            reader.close();
            str = sb.toString();
            return str;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void deleteFile(String path) {
        File file = new File(path);
        if (file.exists()){
            if (file.isDirectory()) {
                Arrays.stream(file.listFiles()).forEach(item -> deleteFile(item.getAbsolutePath()));
            }
            file.delete();
        }
    }
}
