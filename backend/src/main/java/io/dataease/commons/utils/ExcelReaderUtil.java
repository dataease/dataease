package io.dataease.commons.utils;
import com.google.gson.Gson;
import io.dataease.datasource.dto.TableFiled;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

public class ExcelReaderUtil {
    //excel2003扩展名
    public static final String EXCEL03_EXTENSION = ".xls";
    //excel2007扩展名
    public static final String EXCEL07_EXTENSION = ".xlsx";

    public static void sendRows(String filePath, String sheetName, int sheetIndex, int curRow, List<String> cellList) {
        StringBuffer oneLineSb = new StringBuffer();
        oneLineSb.append(filePath);
        oneLineSb.append("--");
        oneLineSb.append("sheet" + sheetIndex);
        oneLineSb.append("::" + sheetName);//加上sheet名
        oneLineSb.append("--");
        oneLineSb.append("row" + curRow);
        oneLineSb.append("::");

        // map.put(cellList.get(9),cellList.get(0)) ;

        for (String cell : cellList) {
            oneLineSb.append(cell.trim());
            oneLineSb.append("|");
        }
        String oneLine = oneLineSb.toString();
        if (oneLine.endsWith("|")) {
            oneLine = oneLine.substring(0, oneLine.lastIndexOf("|"));
        }// 去除最后一个分隔符

        System.out.println(oneLine);
    }

    /**
     * 读取excel文件路径
     * @param fileName 文件路径
     * @throws Exception
     */
    public static void readExcel(String fileName, InputStream inputStream) throws Exception {
        if (fileName.endsWith(EXCEL03_EXTENSION)) { //处理excel2003文件
            ExcelXlsReader excelXls=new ExcelXlsReader();
            excelXls.process(inputStream);
            System.out.println(excelXls.totalSheets.size());
            System.out.println(excelXls.totalSheets.get(0).getSheetName());
            for (TableFiled field : excelXls.totalSheets.get(0).getFields()) {
                System.out.println(new Gson().toJson(field));
            }
            System.out.println(excelXls.totalSheets.get(0).getData().get(0));

        } else if (fileName.endsWith(EXCEL07_EXTENSION)) {//处理excel2007文件
            ExcelXlsxReader excelXlsxReader = new ExcelXlsxReader();
            excelXlsxReader.process(inputStream);
            System.out.println(excelXlsxReader.totalSheets.size());
            System.out.println(excelXlsxReader.totalSheets.get(0).getSheetName());
            for (TableFiled field : excelXlsxReader.totalSheets.get(0).getFields()) {
                System.out.println(new Gson().toJson(field));
            }
            System.out.println(excelXlsxReader.totalSheets.get(0).getData().get(0));

        } else {
            throw new Exception("文件格式错误，fileName的扩展名只能是xls或xlsx。");
        }
    }

    public static void main(String[] args) throws Exception {
        String file ="全国现有确诊趋势.xlsx";
        ExcelReaderUtil.readExcel(file, new FileInputStream("/Users/taojinlong/Desktop/" + file));
    }
}
