package io.dataease.plugins.common.util;

import io.dataease.plugins.common.exception.DataEaseException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FileUtil {

    public static byte[] readBytes(String path) {
        File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            DataEaseException.throwException("文件不存在");
        }
        byte[] bytes = null;
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
            bytes = bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bytes;
    }

    public static String getSuffix(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    public static void main(String[] args) {
        String name = "test.text";
        System.out.println(getSuffix(name));
        System.out.println(getPrefix(name));

        String dirPath = "/opt/dataease/plugins/default";
        List<String> strings = listFileNames(dirPath);
        assert strings != null;
        strings.forEach(System.out::println);
    }

    public static boolean exist(String path) {
        return new File(path).exists();
    }

    public static List<String> listFileNames(String dirPath){
        File file = new File(dirPath);
        if (!file.exists()) return null;
        File[] files = file.listFiles();
        assert files != null;
        return Arrays.stream(files).map(File::getName).collect(Collectors.toList());
    }

    public static String getPrefix(String fileName) {
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    public static void del(File file) {
        if (!file.exists()) return;
        file.delete();
    }

    public static void del(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }

    public static boolean isFile(File file) {
        return file.isFile();
    }

    public static void move(File file, File target, boolean replace) {
        if (!file.exists()) return;
        try {
            Files.move(file.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
