package io.dataease.datasource.provider;


import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.CellData;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dataease.api.dataset.dto.DatasetTableDTO;
import io.dataease.api.ds.vo.ExcelFileData;
import io.dataease.api.ds.vo.ExcelSheetData;
import io.dataease.api.ds.vo.TableField;
import io.dataease.datasource.request.DatasourceRequest;
import io.dataease.exception.DEException;
import io.dataease.utils.AuthUtils;
import io.dataease.utils.JsonUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class ExcelUtils {

    private static String path = "/opt/dataease/data/excel/";
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static List<DatasetTableDTO> getTables(DatasourceRequest datasourceRequest) throws Exception {
        List<DatasetTableDTO> tableDescs = new ArrayList<>();
        JsonNode rootNode = objectMapper.readTree(datasourceRequest.getDatasource().getConfiguration());
        for (int i = 0; i < rootNode.size(); i++) {
            DatasetTableDTO datasetTableDTO = new DatasetTableDTO();
            datasetTableDTO.setTableName(rootNode.get(i).get("tableName").asText());
            datasetTableDTO.setName(rootNode.get(i).get("tableName").asText());
            tableDescs.add(datasetTableDTO);
        }
        return tableDescs;
    }

    public static List<String[]> fetchDataList(DatasourceRequest datasourceRequest) throws Exception {
        List<String[]> dataList = new ArrayList<>();
        JsonNode rootNode = objectMapper.readTree(datasourceRequest.getDatasource().getConfiguration());
        for (int i = 0; i < rootNode.size(); i++) {
            if (rootNode.get(i).get("tableName").asText().equalsIgnoreCase(datasourceRequest.getTable())) {
                String suffix = rootNode.get(i).get("path").asText().substring(rootNode.get(i).get("path").asText().lastIndexOf(".") + 1);
                if (StringUtils.equalsIgnoreCase(suffix, "csv")) {
                    InputStream inputStream = new FileInputStream(rootNode.get(i).get("path").asText());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                    dataList = csvData(reader, false);
                }
            }
        }
        return dataList;
    }

    public static List<TableField> getTableFields(DatasourceRequest datasourceRequest) throws Exception {
        List<TableField> tableFields = new ArrayList<>();
        TypeReference<List<TableField>> listTypeReference = new TypeReference<List<TableField>>() {
        };
        JsonNode rootNode = objectMapper.readTree(datasourceRequest.getDatasource().getConfiguration());
        for (int i = 0; i < rootNode.size(); i++) {
            if (rootNode.get(i).get("tableName").asText().equalsIgnoreCase(datasourceRequest.getTable())) {
                tableFields = JsonUtil.parseList(rootNode.get(i).get("fields").toString(), listTypeReference);
            }
        }
        return tableFields;
    }

    public ExcelFileData excelSaveAndParse(MultipartFile file, long datasourceId) throws DEException {
        String filename = file.getOriginalFilename();
        List<ExcelSheetData> excelSheetDataList = null;
        try {
            excelSheetDataList = parseExcel(filename, file.getInputStream(), true);
        } catch (Exception e) {
            DEException.throwException(e);
        }
        List<ExcelSheetData> returnSheetDataList = new ArrayList<>();
//        if (datasourceId < 1) {
//        } else {
//            returnSheetDataList = excelSheetDataList;
//        }
        returnSheetDataList = excelSheetDataList;
        returnSheetDataList = returnSheetDataList.stream()
                .filter(excelSheetData -> !CollectionUtils.isEmpty(excelSheetData.getFields()))
                .collect(Collectors.toList());
        // save file
        String excelId = UUID.randomUUID().toString();
        String filePath = saveFile(file, excelId);

        filename = filename.substring(0, filename.lastIndexOf('.'));
        if (returnSheetDataList.size() == 1) {
            returnSheetDataList.get(0).setTableName(filename);
            returnSheetDataList.get(0).setPath(filePath);
            returnSheetDataList.get(0).setSheetId(UUID.randomUUID().toString());
            returnSheetDataList.get(0).setSheetExcelId(excelId);
        } else {
            for (ExcelSheetData excelSheetData : returnSheetDataList) {
                excelSheetData.setTableName(filename + "-" + excelSheetData.getExcelLabel());
                excelSheetData.setPath(filePath);
                excelSheetData.setSheetId(UUID.randomUUID().toString());
                excelSheetData.setSheetExcelId(excelId);
            }
        }
        ExcelFileData excelFileData = new ExcelFileData();
        excelFileData.setExcelLabel(filename);
        excelFileData.setId(excelId);
        excelFileData.setPath(filePath);
        excelFileData.setSheets(returnSheetDataList);
        return excelFileData;
    }

    private static String saveFile(MultipartFile file, String fileNameUUID) throws DEException {
        String filePath = null;
        try {
            String filename = file.getOriginalFilename();
            String suffix = filename.substring(filename.lastIndexOf(".") + 1);
            String dirPath = path + AuthUtils.getUser().getUserId() + "/";
            File p = new File(dirPath);
            if (!p.exists()) {
                p.mkdirs();
            }
            filePath = dirPath + fileNameUUID + "." + suffix;
            File f = new File(filePath);
            FileOutputStream fileOutputStream = new FileOutputStream(f);
            fileOutputStream.write(file.getBytes());
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e) {
            DEException.throwException(e);
        }
        return filePath;
    }

    public static List<String[]> csvData(BufferedReader reader, boolean isPreview) throws Exception {
        List<String[]> data = new ArrayList<>();
        int num = 1;
        String line;
        while ((line = reader.readLine()) != null) {
            if (isPreview && num > 1000) {
                break;
            }
            String str;
            line += ",";
            Pattern pCells = Pattern.compile("(\"[^\"]*(\"{2})*[^\"]*\")*[^,]*,");
            Matcher mCells = pCells.matcher(line);
            List<String> cells = new ArrayList();//每行记录一个list
            //读取每个单元格
            while (mCells.find()) {
                str = mCells.group();
                str = str.replaceAll("(?sm)\"?([^\"]*(\"{2})*[^\"]*)\"?.*,", "$1");
                str = str.replaceAll("(?sm)(\"(\"))", "$2");
                cells.add(str);
            }

            data.add(cells.toArray(new String[]{}));
            num++;
        }
        return data;
    }

    private String cellType(String value){
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.parse(value);
            return "DATETIME";
        }catch (Exception e1){
            try {
                Double d = Double.valueOf(value);
                double eps = 1e-10;
                if (d - Math.floor(d) < eps) {
                    return "LONG";
                } else {
                    return "DOUBLE";
                }
            } catch (Exception e2) {
                return "TEXT";
            }
        }
    }
    private void cellType(String value, int i, TableField tableFiled) {
        if (StringUtils.isEmpty(value)) {
            return;
        }
        if (i == 0) {
            tableFiled.setFieldType(cellType(value));
        }else {
            String type = cellType(value);
            if (type.equalsIgnoreCase("TEXT")) {
                tableFiled.setFieldType(type);
            }
            if (type.equalsIgnoreCase("DOUBLE") && tableFiled.getFieldType().equalsIgnoreCase("LONG")) {
                tableFiled.setFieldType(type);
            }
        }



    }

    @Data
    public class NoModleDataListener extends AnalysisEventListener<Map<Integer, String>> {
        private List<String[]> datas = new ArrayList<>();
        private List<String> header = new ArrayList<>();


        @Override
        public void invokeHead(Map<Integer, CellData> headMap, AnalysisContext context) {
            super.invokeHead(headMap, context);
            for (Integer key : headMap.keySet()) {
                CellData cellData = headMap.get(key);
                String value = cellData.getStringValue();
                if (StringUtils.isEmpty(value)) {
                    value = "none_" + key;
                }
                header.add(value);
            }
        }

        @Override
        public void invoke(Map<Integer, String> dataMap, AnalysisContext context) {
            List<String> line = new ArrayList<>();
            for (Integer key : dataMap.keySet()) {
                String value = dataMap.get(key);
                if (StringUtils.isEmpty(value)) {
                    value = "none";
                }
                line.add(value);
            }
            datas.add(line.toArray(new String[line.size()]));
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        }

        public void clear(){
            datas.clear();
            header.clear();
        }
    }

    public List<ExcelSheetData> parseExcel(String filename, InputStream inputStream, boolean isPreview) throws Exception {
        List<ExcelSheetData> excelSheetDataList = new ArrayList<>();
        try {
            String suffix = filename.substring(filename.lastIndexOf(".") + 1);
            if (StringUtils.equalsIgnoreCase(suffix, "xlsx")) {
                NoModleDataListener noModleDataListener = new NoModleDataListener();
                ExcelReader excelReader = EasyExcel.read(inputStream, noModleDataListener).build();
                List<ReadSheet> sheets = excelReader.excelExecutor().sheetList();
                for (ReadSheet readSheet : sheets) {
                    noModleDataListener.clear();
                    List<TableField> fields = new ArrayList<>();
                    excelReader.read(readSheet);
                    for (String s : noModleDataListener.getHeader()) {
                        TableField tableFiled = new TableField();
                        tableFiled.setFieldType("TEXT");
                        tableFiled.setName(s);
                        tableFiled.setOriginName(s);
                        fields.add(tableFiled);
                    }
                    List<String[]> data =  (isPreview && noModleDataListener.getDatas().size() > 100 ? new ArrayList<>(noModleDataListener.getDatas().subList(0, 100)) : noModleDataListener.getDatas());
                    if(isPreview){
                        for (String[] datum : data) {
                            for (int i = 0; i < datum.length; i++) {
                                if (i < fields.size()) {
                                    cellType(datum[i], i, fields.get(i));
                                }
                            }
                        }
                    }
                    ExcelSheetData excelSheetData = new ExcelSheetData();
                    excelSheetData.setFields(fields);
                    excelSheetData.setData(data);
                    excelSheetData.setExcelLabel(readSheet.getSheetName());
                    excelSheetDataList.add(excelSheetData);
                }
            }

            if (StringUtils.equalsIgnoreCase(suffix, "csv")) {
                List<TableField> fields = new ArrayList<>();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                String s = reader.readLine();// first line
                String[] split = s.split(",");
                for (String s1 : split) {
                    TableField tableFiled = new TableField();
                    tableFiled.setName(s1);
                    tableFiled.setOriginName(s1);
                    tableFiled.setFieldType("TEXT");
                    fields.add(tableFiled);
                }
                List<String[]> data = csvData(reader, isPreview);
                ExcelSheetData excelSheetData = new ExcelSheetData();
                String[] fieldArray = fields.stream().map(TableField::getName).toArray(String[]::new);
                excelSheetData.setFields(fields);
                excelSheetData.setData(data);
                excelSheetData.setExcelLabel(filename.substring(0, filename.lastIndexOf('.')));
                excelSheetDataList.add(excelSheetData);
            }
            inputStream.close();
            for (ExcelSheetData excelSheetData : excelSheetDataList) {
                List<String[]> data = excelSheetData.getData();
                String[] fieldArray = excelSheetData.getFields().stream().map(TableField::getName).toArray(String[]::new);

                List<Map<String, Object>> jsonArray = new ArrayList<>();
                if (data != null) {
                    jsonArray = data.stream().map(ele -> {
                        Map<String, Object> map = new HashMap<>();
                        for (int i = 0; i < fieldArray.length; i++) {
                            map.put(fieldArray[i], i < ele.length ? ele[i] : "");
                        }
                        return map;
                    }).collect(Collectors.toList());
                }
                excelSheetData.setJsonArray(jsonArray);
            };
        } catch (Exception e) {
            DEException.throwException(e);
        }
        return excelSheetDataList;
    }


}
