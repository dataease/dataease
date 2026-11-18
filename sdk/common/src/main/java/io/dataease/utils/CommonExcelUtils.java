package io.dataease.utils;

import io.dataease.excel.ExcelIO;
import io.dataease.excel.ExcelModelHandler;
import io.dataease.excel.ExcelReadOptions;
import io.dataease.excel.ExcelWriteOptions;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

public class CommonExcelUtils {

    public static void writeExcel(HttpServletResponse response, List objects, Class clazz, List<Map<Integer, String>> errMsgList, String fileName) throws IOException {
        String sheetName = fileName;
        writeExcel(response, objects, clazz, errMsgList, fileName, sheetName);
    }

    public static void writeExcel(HttpServletResponse response, List objects, Class clazz, List<Map<Integer, String>> errMsgList, String fileName, String sheetName) throws IOException {
        response.addHeader("responseType", "blob");
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition", "attachment:filename=" + URLEncoder.encode(fileName + "." + "xlsx", "UTF-8"));
        ServletOutputStream outputStream = response.getOutputStream();
        writeOutputStream(outputStream, clazz, objects, errMsgList, sheetName);
        try {
            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeOutputStream(OutputStream outputStream, Class clazz, List objects, List<Map<Integer, String>> errMsgList, String sheetName) {
        ExcelWriteOptions options = new ExcelWriteOptions()
                .setSheetName(sheetName)
                .setErrMsgList(errMsgList == null ? java.util.Collections.emptyList() : errMsgList);
        ExcelIO.writeModel(outputStream, clazz, objects, options);
    }

    public static void importExcel(InputStream inputStream, Class clazsz, ExcelModelHandler handler) throws Exception {
        ExcelIO.readModel(inputStream, clazsz, new ExcelReadOptions().setHeadRowNumber(0), handler);
    }

    /**
     * 清理 Excel 读取缓存在当前线程内的日期/数字格式判断结果。
     * Excel 自定义格式 ID 由每个文件独立分配，线程复用时残留的缓存会导致日期被误判为普通数值。
     */
    public static void clearThreadLocalCache() {
        ExcelIO.clearThreadLocalCache();
    }

}
