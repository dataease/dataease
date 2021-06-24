package io.dataease.excel.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import io.dataease.commons.utils.LogUtil;
import io.dataease.exception.DataEaseException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

public class EasyExcelExporter {

    private Class clazz;

    public EasyExcelExporter(Class clazz) {
        this.clazz = clazz;
    }

    public void export(HttpServletResponse response, List data, String fileName, String sheetName) {
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
        contentWriteCellStyle.setWrapped(true);
        try {
            HorizontalCellStyleStrategy horizontalCellStyleStrategy = new HorizontalCellStyleStrategy(null, contentWriteCellStyle);
            response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8") + ".xlsx");
            EasyExcel.write(response.getOutputStream(), this.clazz).registerWriteHandler(horizontalCellStyleStrategy).sheet(sheetName).doWrite(data);
        } catch (UnsupportedEncodingException e) {
            LogUtil.error(e.getMessage(), e);
            DataEaseException.throwException("Utf-8 encoding is not supported");
        } catch (IOException e) {
            LogUtil.error(e.getMessage(), e);
            DataEaseException.throwException("IO exception");
        }
    }

}
