package io.dataease.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import io.dataease.model.excel.AutoAdaptWidthStyleStrategy;
import io.dataease.model.excel.ErrWriteHandler;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.IndexedColors;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

public class CommonExcelUtils {

    public static void writeExcel(HttpServletResponse response, List objects, Class clazz,List<Map<Integer, String>> errMsgList, String fileName) throws IOException {
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
        WriteCellStyle headCellStyle = new WriteCellStyle();
        headCellStyle.setFillForegroundColor(Short.valueOf(IndexedColors.WHITE.getIndex()));
        WriteFont headFont = new WriteFont();
        headFont.setFontHeightInPoints((short) 12);
        headCellStyle.setWriteFont(headFont);
        WriteCellStyle contentCellStyle = new WriteCellStyle();

        WriteFont contentFont = new WriteFont();
        contentFont.setFontHeightInPoints((short) 11);
        contentCellStyle.setWriteFont(contentFont);

        HorizontalCellStyleStrategy horizontalCellStyleStrategy = new HorizontalCellStyleStrategy(headCellStyle, contentCellStyle);
        try {
            ExcelWriterBuilder writerBuilder = EasyExcel.write(outputStream, clazz);
            if (CollectionUtils.isNotEmpty(errMsgList)) {
                writerBuilder.inMemory(Boolean.TRUE).registerWriteHandler(new ErrWriteHandler(errMsgList));
            }
            writerBuilder.registerWriteHandler(new AutoAdaptWidthStyleStrategy()).registerWriteHandler(horizontalCellStyleStrategy).sheet(sheetName).doWrite(objects);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void importExcel(InputStream inputStream, Class clazsz, AnalysisEventListener analysisEventListener) throws Exception {
        EasyExcel.read(inputStream, clazsz, analysisEventListener).sheet().headRowNumber(0).doRead();
    }

}
