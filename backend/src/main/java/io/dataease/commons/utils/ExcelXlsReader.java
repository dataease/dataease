package io.dataease.commons.utils;

import io.dataease.dto.dataset.ExcelSheetData;
import io.dataease.i18n.Translator;
import io.dataease.plugins.common.dto.datasource.TableField;
import org.apache.poi.hssf.eventusermodel.*;
import org.apache.poi.hssf.eventusermodel.dummyrecord.LastCellOfRowDummyRecord;
import org.apache.poi.hssf.eventusermodel.dummyrecord.MissingCellDummyRecord;
import org.apache.poi.hssf.record.*;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author y
 * @create 2018-01-19 14:18
 * @desc 用于解决.xls2003版本大数据量问题
 **/
public class ExcelXlsReader implements HSSFListener {

    private int minColumns = -1;

    public Integer getObtainedNum() {
        return obtainedNum;
    }

    public void setObtainedNum(Integer obtainedNum) {
        this.obtainedNum = obtainedNum;
    }

    private Integer obtainedNum = null;

    private POIFSFileSystem fs;

    /**
     * 总行数
     */
    private int totalRows = 0;

    /**
     * 上一行row的序号
     */
    private int lastRowNumber;

    /**
     * 上一单元格的序号
     */
    private int lastColumnNumber;

    /**
     * 是否输出formula，还是它对应的值
     */
    private boolean outputFormulaValues = true;

    /**
     * 用于转换formulas
     */
    private EventWorkbookBuilder.SheetRecordCollectingListener workbookBuildingListener;

    //excel2003工作簿
    private HSSFWorkbook stubWorkbook;

    private SSTRecord sstRecord;

    private FormatTrackingHSSFListener formatListener;

    private final HSSFDataFormatter formatter = new HSSFDataFormatter();

    /**
     * 文件的绝对路径
     */
    private String filePath = "";

    //表索引
    private int sheetIndex = 0;

    private BoundSheetRecord[] orderedBSRs;

    @SuppressWarnings("unchecked")
    private ArrayList boundSheetRecords = new ArrayList();

    private int nextRow;

    private int nextColumn;

    private boolean outputNextStringRecord;

    //当前行
    private int curRow = 0;

    //存储一行记录所有单元格的容器
    private List<String> cellList = new ArrayList<String>();

    /**
     * 判断整行是否为空行的标记
     */
    private boolean flag = false;

    @SuppressWarnings("unused")
    private String sheetName;

    public List<TableField> fields = new ArrayList<>();
    public List<List<String>> data = new ArrayList<>();
    public List<ExcelSheetData> totalSheets = new ArrayList<>();
    /**
     * 是否为日期
     */
    private boolean isDateFormat = false;


    public List<TableField> getFields() {
        return fields;
    }

    public void setFields(List<TableField> fields) {
        this.fields = fields;
    }

    public List<List<String>> getData() {
        return data;
    }

    public void setData(List<List<String>> data) {
        this.data = data;
    }


    /**
     * 遍历excel下所有的sheet
     *
     * @param inputStream
     * @throws Exception
     */
    public int process(InputStream inputStream) throws Exception {
        this.fs = new POIFSFileSystem(inputStream);
        MissingRecordAwareHSSFListener listener = new MissingRecordAwareHSSFListener(this);
        formatListener = new FormatTrackingHSSFListener(listener);
        HSSFEventFactory factory = new HSSFEventFactory();
        HSSFRequest request = new HSSFRequest();
        if (outputFormulaValues) {
            request.addListenerForAllRecords(formatListener);
        } else {
            workbookBuildingListener = new EventWorkbookBuilder.SheetRecordCollectingListener(formatListener);
            request.addListenerForAllRecords(workbookBuildingListener);
        }
        factory.processWorkbookEvents(request, fs);

        return totalRows; //返回该excel文件的总行数，不包括首列和空行
    }

    /**
     * HSSFListener 监听方法，处理Record
     * 处理每个单元格
     *
     * @param record
     */
    @SuppressWarnings("unchecked")
    public void processRecord(Record record) {
        int thisRow = -1;
        int thisColumn = -1;
        String thisStr = null;
        String value = null;
        switch (record.getSid()) {
            case BoundSheetRecord.sid:
                boundSheetRecords.add(record);
                break;
            case BOFRecord.sid: //开始处理每个sheet
                BOFRecord br = (BOFRecord) record;
                if (br.getType() == BOFRecord.TYPE_WORKSHEET) {
                    //如果有需要，则建立子工作簿
                    if (workbookBuildingListener != null && stubWorkbook == null) {
                        stubWorkbook = workbookBuildingListener.getStubHSSFWorkbook();
                    }

                    if (orderedBSRs == null) {
                        orderedBSRs = BoundSheetRecord.orderByBofPosition(boundSheetRecords);
                    }
                    sheetName = orderedBSRs[sheetIndex].getSheetname();
                    sheetIndex++;
                }
                break;
            case MergeCellsRecord.sid:
                throw new RuntimeException(Translator.get("i18n_excel_have_merge_region"));
            case SSTRecord.sid:
                sstRecord = (SSTRecord) record;
                break;
            case BlankRecord.sid: //单元格为空白
                BlankRecord brec = (BlankRecord) record;
                thisRow = brec.getRow();
                thisColumn = brec.getColumn();
                thisStr = "";
                cellList.add(thisColumn, thisStr);
                break;
            case BoolErrRecord.sid: //单元格为布尔类型
                BoolErrRecord berec = (BoolErrRecord) record;
                thisRow = berec.getRow();
                thisColumn = berec.getColumn();
                thisStr = berec.getBooleanValue() + "";
                cellList.add(thisColumn, thisStr);

                checkRowIsNull(thisStr);  //如果里面某个单元格含有值，则标识该行不为空行
                break;
            case FormulaRecord.sid://单元格为公式类型
                FormulaRecord frec = (FormulaRecord) record;
                thisRow = frec.getRow();
                thisColumn = frec.getColumn();
                thisStr = String.valueOf(frec.getValue());
                String fieldType = checkType(thisStr, thisColumn);
                if (fieldType != null && fieldType.equalsIgnoreCase("LONG") && thisStr.endsWith(".0")) {
                    thisStr = thisStr.substring(0, thisStr.length() - 2);
                }
                cellList.add(thisColumn, thisStr);
                checkRowIsNull(thisStr);  //如果里面某个单元格含有值，则标识该行不为空行
                break;
            case StringRecord.sid: //单元格中公式的字符串
                if (outputNextStringRecord) {
                    StringRecord srec = (StringRecord) record;
                    thisStr = srec.getString();
                    thisRow = nextRow;
                    thisColumn = nextColumn;
                    outputNextStringRecord = false;
                }
                break;
            case LabelRecord.sid:
                LabelRecord lrec = (LabelRecord) record;
                curRow = thisRow = lrec.getRow();
                thisColumn = lrec.getColumn();
                value = lrec.getValue().trim();
                value = value.equals("") ? "" : value;
                cellList.add(thisColumn, value);
                checkRowIsNull(value);  //如果里面某个单元格含有值，则标识该行不为空行
                break;
            case LabelSSTRecord.sid: //单元格为字符串类型
                LabelSSTRecord lsrec = (LabelSSTRecord) record;
                curRow = thisRow = lsrec.getRow();
                thisColumn = lsrec.getColumn();
                if (sstRecord == null) {
                    cellList.add(thisColumn, "");
                } else {
                    value = sstRecord.getString(lsrec.getSSTIndex()).toString().trim();
                    value = value.equals("") ? "" : value;
                    cellList.add(thisColumn, value);
                    checkType(value, thisColumn);
                    checkRowIsNull(value);  //如果里面某个单元格含有值，则标识该行不为空行
                }
                break;
            case NumberRecord.sid: //单元格为数字类型
                NumberRecord numrec = (NumberRecord) record;
                curRow = thisRow = numrec.getRow();
                thisColumn = numrec.getColumn();
                //第一种方式
                //value = formatListener.formatNumberDateCell(numrec).trim();//这个被写死，采用的m/d/yy h:mm格式，不符合要求
                //第二种方式，参照formatNumberDateCell里面的实现方法编写
                Double valueDouble = ((NumberRecord) numrec).getValue();
                String formatString = formatListener.getFormatString(numrec);
                if (formatString.contains("m/d/yy")) {
                    formatString = "yyyy-MM-dd hh:mm:ss";
                }
                int formatIndex = formatListener.getFormatIndex(numrec);

                value = formatter.formatRawCellContents(valueDouble, formatIndex, formatString).trim();
                value = value.equals("") ? "" : value;
                //向容器加入列值
                cellList.add(thisColumn, value);
                if (formatIndex == 59 || formatIndex == 14) {
                    totalSheets.get(totalSheets.size() - 1).getFields().get(thisColumn).setFieldType("DATETIME");
                } else {
                    checkType(value, thisColumn);
                }
                checkRowIsNull(value);  //如果里面某个单元格含有值，则标识该行不为空行
                break;
            default:
                break;
        }

        //遇到新行的操作
        if (thisRow != -1 && thisRow != lastRowNumber) {
            lastColumnNumber = -1;
        }

        //空值的操作
        if (record instanceof MissingCellDummyRecord) {
            MissingCellDummyRecord mc = (MissingCellDummyRecord) record;
            curRow = thisRow = mc.getRow();
            thisColumn = mc.getColumn();
            cellList.add(thisColumn, "");
        }

        //更新行和列的值
        if (thisRow > -1)
            lastRowNumber = thisRow;
        if (thisColumn > -1)
            lastColumnNumber = thisColumn;

        //行结束时的操作
        if (record instanceof LastCellOfRowDummyRecord) {
            if (minColumns > 0) {
                //列值重新置空
                if (lastColumnNumber == -1) {
                    lastColumnNumber = 0;
                }
            }
            lastColumnNumber = -1;

            if (!totalSheets.stream().map(ExcelSheetData::getExcelLabel).collect(Collectors.toList()).contains(sheetName)) {
                ExcelSheetData excelSheetData = new ExcelSheetData();
                excelSheetData.setExcelLabel(sheetName);
                excelSheetData.setData(new ArrayList<>());
                excelSheetData.setFields(new ArrayList<>());
                totalSheets.add(excelSheetData);
            }

            if (curRow == 0) {
                for (String s : cellList) {
                    TableField tableField = new TableField();
                    tableField.setFieldType("TEXT");
                    tableField.setFieldSize(65533);
                    tableField.setFieldName(s);
                    tableField.setRemarks(s);
                    this.fields.add(tableField);
                    totalSheets.get(totalSheets.size() - 1).getFields().add(tableField);
                }
            }


            if (flag && curRow != 0) { //该行不为空行且该行不是第一行，发送（第一行为列名，不需要）
                if (!totalSheets.stream().map(ExcelSheetData::getExcelLabel).collect(Collectors.toList()).contains(sheetName)) {
                    ExcelSheetData excelSheetData = new ExcelSheetData();
                    excelSheetData.setData(new ArrayList<>(data));
                    excelSheetData.setExcelLabel(sheetName);
                    excelSheetData.setFields(new ArrayList<>(fields));
                    List<String> tmp = new ArrayList<>(cellList);
                    excelSheetData.getData().add(tmp);
                    totalRows++;
                    totalSheets.add(excelSheetData);
                } else {
                    List<String> tmp = new ArrayList<>(cellList);
                    if (obtainedNum != null && totalSheets.stream().filter(s -> s.getExcelLabel().equalsIgnoreCase(sheetName)).collect(Collectors.toList()).get(0).getData().size() < obtainedNum) {
                        totalSheets.stream().filter(s -> s.getExcelLabel().equalsIgnoreCase(sheetName)).collect(Collectors.toList()).get(0).getData().add(tmp);
                    }
                    if (obtainedNum == null) {
                        totalSheets.stream().filter(s -> s.getExcelLabel().equalsIgnoreCase(sheetName)).collect(Collectors.toList()).get(0).getData().add(tmp);
                    }
                    totalRows++;
                }
            }

            //清空容器
            cellList.clear();
            flag = false;
        }
    }

    /**
     * 如果里面某个单元格含有值，则标识该行不为空行
     *
     * @param value
     */
    public void checkRowIsNull(String value) {
        if (value != null && !"".equals(value)) {
            flag = true;
        }
    }


    private String checkType(String str, int thisColumn) {
        String type = null;
        try {
            double d = Double.valueOf(str);
            try {
                Double value = new Double(d);
                double eps = 1e-10;
                if (value - Math.floor(value) < eps) {
                    type = "LONG";
                } else {
                    type = "DOUBLE";
                }
            } catch (Exception e) {
            }
        } catch (Exception e) {
        }

        if (curRow == 1) {
            totalSheets.get(totalSheets.size() - 1).getFields().get(thisColumn).setFieldType(type == null ? "TEXT" : type);
        }
        if (curRow > 1 && type != null) {
            String oldType = totalSheets.get(totalSheets.size() - 1).getFields().get(thisColumn).getFieldType();
            if (type.equalsIgnoreCase("TEXT")) {
                totalSheets.get(totalSheets.size() - 1).getFields().get(thisColumn).setFieldType(type);
            }
            if (type.equalsIgnoreCase("DOUBLE") && oldType.equalsIgnoreCase("LONG")) {
                totalSheets.get(totalSheets.size() - 1).getFields().get(thisColumn).setFieldType(type);
            }
        }
        return type;
    }

}
