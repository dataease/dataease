package io.dataease.commons.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.*;

public class CompressUtils {

    /***
     * Zip压缩
     *
     * @param data 待压缩数据
     * @return 压缩后数据
     */
    public static Object zip(Object data) {
        if (!(data instanceof byte[])) {
            return data;
        }

        byte[] temp = (byte[]) data;
        byte[] b = (byte[]) data;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ZipOutputStream zip = new ZipOutputStream(bos);
            ZipEntry entry = new ZipEntry("zip");
            entry.setSize(temp.length);
            zip.putNextEntry(entry);
            zip.write(temp);
            zip.closeEntry();
            zip.close();
            b = bos.toByteArray();
            bos.close();
        } catch (Exception ex) {
            LogUtil.error(ex);
        }

        return b;
    }

    /***
     * Zip解压
     *
     * @param data 待解压数据
     * @return 解压后数据
     */
    public static Object unzip(Object data) {
        if (!(data instanceof byte[])) {
            return data;
        }
        byte[] temp = (byte[]) data;
        byte[] b = (byte[]) data;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(temp);
            ZipInputStream zip = new ZipInputStream(bis);
            while (zip.getNextEntry() != null) {
                byte[] buf = new byte[1024];
                int num;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                while ((num = zip.read(buf, 0, buf.length)) != -1) {
                    baos.write(buf, 0, num);
                }
                b = baos.toByteArray();
                baos.flush();
                baos.close();
            }
            zip.close();
            bis.close();
        } catch (Exception ex) {
            LogUtil.error(ex);
        }
        return b;
    }

    /**
     * GZip压缩
     *
     * @param data 待压缩数据
     * @return 压缩后数
     */
    public static Object compress(Object data) {
        if (!(data instanceof byte[])) {
            return data;
        }
        byte[] bytes = (byte[]) data;
        try (ByteArrayOutputStream obj = new ByteArrayOutputStream(); GZIPOutputStream gzip = new GZIPOutputStream(obj)) {
            gzip.write(bytes);
            gzip.flush();
            gzip.finish();
            return obj.toByteArray();
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            return data;
        }
    }

    /**
     * GZip解压
     *
     * @param data 待解压数据
     * @return 解压后数据
     */
    public static Object decompress(Object data) {
        if (!(data instanceof byte[])) {
            return data;
        }
        byte[] bytes = (byte[]) data;
        if (bytes.length == 0) {
            return bytes;
        }
        try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(bytes)); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = gis.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            return data;
        }
    }
}
