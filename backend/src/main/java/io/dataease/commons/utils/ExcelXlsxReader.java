package io.dataease.commons.utils;
import io.dataease.datasource.dto.TableFiled;
import io.dataease.dto.dataset.ExcelSheetData;
import io.dataease.i18n.Translator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.InputStream;
import java.util.*;

/**
 * @author y
 * @create 2018-01-18 14:28
 * @desc POI读取excel有两种模式，一种是用户模式，一种是事件驱动模式
 * 采用SAX事件驱动模式解决XLSX文件，可以有效解决用户模式内存溢出的问题，
 * 该模式是POI官方推荐的读取大数据的模式，
 * 在用户模式下，数据量较大，Sheet较多，或者是有很多无用的空行的情况下，容易出现内存溢出
 * <p>
 * 用于解决.xlsx2007版本大数据量问题
 **/
public class ExcelXlsxReader extends DefaultHandler {

    /**
     * 自定义获取表格某些信息
     */
    public Map map = new TreeMap<String,String>();
    /**
     * 单元格中的数据可能的数据类型
     */
    enum CellDataType {
        BOOL, ERROR, FORMULA, INLINESTR, SSTINDEX, NUMBER, DATE, NULL
    }

    /**
     * 共享字符串表
     */
    private SharedStringsTable sst;

    /**
     * 上一次的索引值
     */
    private String lastIndex;

    /**
     * 总行数
     */
    private int totalRows=0;

    /**
     * 一行内cell集合
     */
    private List<String> cellList = new ArrayList<String>();

    /**
     * 判断整行是否为空行的标记
     */
    private boolean flag = false;

    /**
     * 当前行
     */
    private int curRow = 1;

    /**
     * 当前列
     */
    private int curCol = 0;

    /**
     * T元素标识
     */
    private boolean isTElement;

    /**
     * 单元格数据类型，默认为字符串类型
     */
    private CellDataType nextDataType = CellDataType.SSTINDEX;

    private final DataFormatter formatter = new DataFormatter();

    /**
     * 单元格日期格式的索引
     */
    private short formatIndex;

    /**
     * 日期格式字符串
     */
    private String formatString;

    //定义前一个元素和当前元素的位置，用来计算其中空的单元格数量，如A6和A8等
    private String preRef = null, ref = null;

    //定义该文档一行最大的单元格数，用来补全一行最后可能缺失的单元格
    private String maxRef = null;

    /**
     * 单元格
     */
    private StylesTable stylesTable;

    public List<TableFiled> fields = new ArrayList<>();
    public List<List<String>> data = new ArrayList<>();
    public List<ExcelSheetData> totalSheets = new ArrayList<>();
    /**
     * 是否为日期
     */
    private boolean isDateFormat = false;


    public List<TableFiled> getFields() {
        return fields;
    }

    public void setFields(List<TableFiled> fields) {
        this.fields = fields;
    }

    public List<List<String>> getData() {
        return data;
    }

    public void setData(List<List<String>>  data) {
        this.data = data;
    }

    public int process(InputStream inputStream) throws Exception {
        OPCPackage pkg = OPCPackage.open(inputStream);
        XSSFReader xssfReader = new XSSFReader(pkg);
        stylesTable = xssfReader.getStylesTable();
        SharedStringsTable sst = xssfReader.getSharedStringsTable();
        XMLReader parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
        this.sst = sst;
        parser.setContentHandler(this);
        XSSFReader.SheetIterator sheets = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
        while (sheets.hasNext()) { //遍历sheet
            curRow = 1; //标记初始行为第一行
            fields.clear();
            data.clear();
            InputStream sheet = sheets.next(); //sheets.next()和sheets.getSheetName()不能换位置，否则sheetName报错
            InputSource sheetSource = new InputSource(sheet);
            parser.parse(sheetSource); //解析excel的每条记录，在这个过程中startElement()、characters()、endElement()这三个函数会依次执行

            ExcelSheetData excelSheetData = new ExcelSheetData();
            excelSheetData.setData(new ArrayList<>(data));
            excelSheetData.setSheetName(sheets.getSheetName());
            excelSheetData.setFields(new ArrayList<>(fields));
            totalSheets.add(excelSheetData);

            sheet.close();
        }
        return totalRows; //返回该excel文件的总行数，不包括首列和空行
    }

    /**
     * 第一个执行
     *
     * @param uri
     * @param localName
     * @param name
     * @param attributes
     * @throws SAXException
     */
    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
        if(curRow>101){
            return;
        }

        if(name.equalsIgnoreCase("mergeCell")){
            throw new RuntimeException(Translator.get("i18n_excel_have_merge_region"));
        }
        //c => 单元格
        if ("c".equals(name)) {
            //当前单元格的位置
            ref = attributes.getValue("r");
            //设定单元格类型
            this.setNextDataType(attributes);
        }

        //当元素为t时
        if ("t".equals(name)) {
            isTElement = true;
        } else {
            isTElement = false;
        }

        //置空
        lastIndex = "";
    }

    /**
     * 第二个执行
     * 得到单元格对应的索引值或是内容值
     * 如果单元格类型是字符串、INLINESTR、数字、日期，lastIndex则是索引值
     * 如果单元格类型是布尔值、错误、公式，lastIndex则是内容值
     * @param ch
     * @param start
     * @param length
     * @throws SAXException
     */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if(curRow>101){
            return;
        }
        lastIndex += new String(ch, start, length);
    }

    /**
     * 第三个执行
     *
     * @param uri
     * @param localName
     * @param name
     * @throws SAXException
     */
    @Override
    public void endElement(String uri, String localName, String name) throws SAXException {
        if(curRow>101){
            return;
        }
        //t元素也包含字符串
        if (isTElement) {//这个程序没经过
            //将单元格内容加入rowlist中，在这之前先去掉字符串前后的空白符
            String value = lastIndex.trim();
            cellList.add(curCol, value);
            curCol++;
            isTElement = false;
            //如果里面某个单元格含有值，则标识该行不为空行
            if (value != null && !"".equals(value)) {
                flag = true;
            }
        } else if ("v".equals(name)) {
            //v => 单元格的值，如果单元格是字符串，则v标签的值为该字符串在SST中的索引
            String value = this.getDataValue(lastIndex.trim(), "");//根据索引值获取对应的单元格值

            if (preRef == null) {
                preRef = ref;
            }
            //补全单元格之间的空单元格
            if (!"A".equals(preRef.substring(0, 1)) && curRow==1 && preRef.equalsIgnoreCase(ref)) {
                throw new RuntimeException(Translator.get("i18n_excel_empty_column"));
            }else if (!ref.equals(preRef)) {
                int len = countNullCell(ref, preRef);
                for (int i = 0; i < len; i++) {
                    cellList.add(curCol, "");
                    curCol++;
                }
            }

            cellList.add(curCol, value);
            curCol++;
            //如果里面某个单元格含有值，则标识该行不为空行
            if (value != null && !"".equals(value)) {
                flag = true;
            }
            preRef = ref;
        } else {
            //如果标签名称为row，这说明已到行尾
            if ("row".equals(name)) {
                //默认第一行为表头，以该行单元格数目为最大数目
                if (curRow == 1) {
                    maxRef = ref;
                }
                //补全一行尾部可能缺失的单元格
                if (maxRef != null) {
                    int len = countNullCell(maxRef, ref);
                    for (int i = 0; i <= len; i++) {
                        cellList.add(curCol, "");
                        curCol++;
                    }
                }
                if(curRow>1){
                    List<String> tmp = new ArrayList<>(cellList);
                    this.getData().add(tmp);
                }
                totalRows++;
                cellList.clear();
                curRow++;
                curCol = 0;
                preRef = null;
                ref = null;
                flag=false;
            }
        }
    }

    /**
     * 处理数据类型
     *
     * @param attributes
     */
    public void setNextDataType(Attributes attributes) {
        nextDataType = CellDataType.NUMBER; //cellType为空，则表示该单元格类型为数字
        formatIndex = -1;
        formatString = null;
        String cellType = attributes.getValue("t"); //单元格类型
        if ("b".equals(cellType)) { //处理布尔值
            nextDataType = CellDataType.BOOL;
        } else if ("e".equals(cellType)) {  //处理错误
            nextDataType = CellDataType.ERROR;
        } else if ("inlineStr".equals(cellType)) {
            nextDataType = CellDataType.INLINESTR;
        } else if ("s".equals(cellType)) { //处理字符串
            nextDataType = CellDataType.SSTINDEX;
        } else if ("str".equals(cellType)) {
            nextDataType = CellDataType.SSTINDEX;
        }

        String cellStyleStr = attributes.getValue("s"); //
        if (cellStyleStr != null) {
            int styleIndex = Integer.parseInt(cellStyleStr);
            XSSFCellStyle style = this.stylesTable.getStyleAt(styleIndex);
            formatIndex = style.getDataFormat();
            formatString = style.getDataFormatString();
            short format = this.formatIndex;
            if (format == 14 || format == 31 || format == 57 ||format == 59||
                    format == 58 || (176 < format && format < 178)
                    || (182 <= format && format <= 196) ||
                    (210 <= format && format <= 213) || (208 == format))
            { // 日期
                isDateFormat = true;
            }
        }

    }

    /**
     * 对解析出来的数据进行类型处理
     * @param value   单元格的值，
     *                value代表解析：BOOL的为0或1， ERROR的为内容值，FORMULA的为内容值，INLINESTR的为索引值需转换为内容值，
     *                SSTINDEX的为索引值需转换为内容值， NUMBER为内容值，DATE为内容值
     * @param thisStr 一个空字符串
     * @return
     */
    @SuppressWarnings("deprecation")
    public String getDataValue(String value, String thisStr) {
        String type = "TEXT";
        switch (nextDataType) {
            // 这几个的顺序不能随便交换，交换了很可能会导致数据错误
            case BOOL: //布尔值
                char first = value.charAt(0);
                thisStr = first == '0' ? "FALSE" : "TRUE";
                type = "LONG";
                break;
            case ERROR: //错误
                thisStr = "\"ERROR:" + value.toString() + '"';
                break;
            case FORMULA: //公式
                thisStr = '"' + value.toString() + '"';
                type = getType(thisStr);
                break;
            case INLINESTR:
                XSSFRichTextString rtsi = new XSSFRichTextString(value.toString());
                thisStr = rtsi.toString();
                rtsi = null;
                break;
            case SSTINDEX: //字符串
                String sstIndex = value.toString();
                try {
                    int idx = Integer.parseInt(sstIndex);
                    XSSFRichTextString rtss = new XSSFRichTextString(sst.getEntryAt(idx));//根据idx索引值获取内容值
                    thisStr = rtss.toString();
                    rtss = null;
                } catch (NumberFormatException ex) {
                    thisStr = value.toString();
                }

                break;
            case NUMBER: //数字
                if (formatString != null && isDateFormat) {
                    thisStr = formatter.formatRawCellContents(Double.parseDouble(value), formatIndex, formatString).trim();
                } else {
                    thisStr = value;
                }
                thisStr = thisStr.replace("_", "").trim();
                if(isDateFormat){
                    type = "DATETIME";isDateFormat = false;
                }else {
                    type = getType(thisStr);
                }
                break;
            case DATE: //日期
                thisStr = formatter.formatRawCellContents(Double.parseDouble(value), formatIndex, formatString);
                // 对日期字符串作特殊处理，去掉T
                thisStr = thisStr.replace("T", " ");
                type = "DATETIME";
                break;
            default:
                thisStr = " ";
                break;
        }
        if(curRow==1){
            TableFiled tableFiled = new TableFiled();
            tableFiled.setFieldType("TEXT");
            tableFiled.setFieldSize(65533);
            tableFiled.setFieldName(thisStr);
            tableFiled.setRemarks(thisStr);
            this.fields.add(tableFiled);
        }else {
            if(CollectionUtils.isEmpty(this.getFields())){
                throw new RuntimeException(Translator.get("i18n_excel_header_empty"));
            }
            if(curRow==2){
                this.getFields().get(curCol).setFieldType(type);
            }else {
                if(type.equalsIgnoreCase("TEXT")){
                    this.getFields().get(curCol).setFieldType(type);
                }
                if(type.equalsIgnoreCase("DOUBLE") && this.getFields().get(curCol).getFieldType().equalsIgnoreCase("LONG")){
                    this.getFields().get(curCol).setFieldType(type);
                }
                if(type.equalsIgnoreCase("DATETIME")){
                    this.getFields().get(curCol).setFieldType(type);
                }
            }
        }
        return thisStr;
    }

    private String getType(String thisStr){
        if(totalRows==0){
            return "TEXT";
        }
        try{
            if(thisStr.endsWith("%")){
                thisStr = thisStr.substring(0, thisStr.length()-1);
                thisStr = String.valueOf(Double.valueOf(thisStr)/100);
            }
            Long.valueOf(thisStr);
            return "LONG";
        }catch (Exception e){
            try {
                Double.valueOf(thisStr);
                return "DOUBLE";
            }catch (Exception ignore){ }
        }
        return "TEXT";
    }

    public int countNullCell(String ref, String preRef) {
        //excel2007最大行数是1048576，最大列数是16384，最后一列列名是XFD
        String xfd = ref.replaceAll("\\d+", "");
        String xfd_1 = preRef.replaceAll("\\d+", "");

        xfd = fillChar(xfd, 3, '@', true);
        xfd_1 = fillChar(xfd_1, 3, '@', true);

        char[] letter = xfd.toCharArray();
        char[] letter_1 = xfd_1.toCharArray();
        int res = (letter[0] - letter_1[0]) * 26 * 26 + (letter[1] - letter_1[1]) * 26 + (letter[2] - letter_1[2]);
        return res - 1;
    }

    public String fillChar(String str, int len, char let, boolean isPre) {
        int len_1 = str.length();
        if (len_1 < len) {
            if (isPre) {
                for (int i = 0; i < (len - len_1); i++) {
                    str = let + str;
                }
            } else {
                for (int i = 0; i < (len - len_1); i++) {
                    str = str + let;
                }
            }
        }
        return str;
    }

}
