package io.dataease.datasource.provider;


import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dataease.api.ds.vo.ExcelFileData;
import io.dataease.api.ds.vo.ExcelSheetData;
import io.dataease.api.ds.vo.RemoteExcelRequest;
import io.dataease.commons.utils.EncryptUtils;
import io.dataease.datasource.dao.auto.entity.CoreDatasource;
import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.dto.DatasetTableDTO;
import io.dataease.extensions.datasource.dto.DatasourceDTO;
import io.dataease.extensions.datasource.dto.DatasourceRequest;
import io.dataease.extensions.datasource.dto.TableField;
import io.dataease.api.ds.vo.ExcelConfiguration;
import io.dataease.i18n.Translator;
import io.dataease.utils.*;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class ExcelUtils {
    public static final String UFEFF = "\uFEFF";
    private static String path = getExcelPath();
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static String getExcelPath() {
        if (ModelUtils.isDesktop()) {
            return ConfigUtils.getConfig("dataease.path.excel", "/opt/dataease2.0/data/excel/");
        } else {
            return "/opt/dataease2.0/data/excel/";
        }
    }

    private static TypeReference<List<TableField>> TableFieldListTypeReference = new TypeReference<List<TableField>>() {
    };

    private static TypeReference<List<ExcelSheetData>> sheets = new TypeReference<List<ExcelSheetData>>() {
    };

    public static void mergeSheets(CoreDatasource requestDatasource, DatasourceDTO sourceData) {
        if (requestDatasource.getType().equalsIgnoreCase("Excel")) {
            List<ExcelSheetData> newSheets = JsonUtil.parseList(requestDatasource.getConfiguration(), sheets);
            List<String> tableNames = newSheets.stream().map(ExcelSheetData::getDeTableName).collect(Collectors.toList());
            List<ExcelSheetData> oldSheets = JsonUtil.parseList(sourceData.getConfiguration(), sheets);
            for (ExcelSheetData oldSheet : oldSheets) {
                if (!tableNames.contains(oldSheet.getDeTableName())) {
                    newSheets.add(oldSheet);
                }
            }
            requestDatasource.setConfiguration(JsonUtil.toJSONString(newSheets).toString());
        } else {
            ExcelConfiguration excelConfiguration = JsonUtil.parseObject(requestDatasource.getConfiguration(), ExcelConfiguration.class);
            List<ExcelSheetData> newSheets = excelConfiguration.getSheets();
            List<String> tableNames = newSheets.stream().map(ExcelSheetData::getDeTableName).collect(Collectors.toList());
            List<ExcelSheetData> oldSheets = JsonUtil.parseObject(sourceData.getConfiguration(), ExcelConfiguration.class).getSheets();
            for (ExcelSheetData oldSheet : oldSheets) {
                if (!tableNames.contains(oldSheet.getDeTableName())) {
                    newSheets.add(oldSheet);
                }
            }
            excelConfiguration.setSheets(newSheets);
            requestDatasource.setConfiguration(JsonUtil.toJSONString(excelConfiguration).toString());
        }

    }

    public static List<DatasetTableDTO> getTables(DatasourceRequest datasourceRequest) throws DEException {
        List<DatasetTableDTO> tableDescs = new ArrayList<>();
        try {
            String sheets = "";
            if (datasourceRequest.getDatasource().getType().equalsIgnoreCase("Excel")) {
                sheets = datasourceRequest.getDatasource().getConfiguration();
            } else {
                sheets = objectMapper.readTree(datasourceRequest.getDatasource().getConfiguration()).get("sheets").toString();
            }
            JsonNode rootNode = objectMapper.readTree(sheets);
            for (int i = 0; i < rootNode.size(); i++) {
                DatasetTableDTO datasetTableDTO = new DatasetTableDTO();
                datasetTableDTO.setTableName(rootNode.get(i).get("deTableName").asText());
                datasetTableDTO.setName(rootNode.get(i).get("deTableName").asText());
                datasetTableDTO.setDatasourceId(datasourceRequest.getDatasource().getId());
                datasetTableDTO.setLastUpdateTime(rootNode.get(i).get("lastUpdateTime") == null ? datasourceRequest.getDatasource().getCreateTime() : rootNode.get(i).get("lastUpdateTime").asLong(0L));
                tableDescs.add(datasetTableDTO);
            }
        } catch (Exception e) {
            DEException.throwException(e);
        }

        return tableDescs;
    }

    public static Map<String, String> getTableNamesMap(String type, String configuration) throws DEException {
        Map<String, String> result = new HashMap<>();
        JsonNode rootNode = null;
        // 兼容历史未加密信息
        String sheets = configuration;
        try {
            if (type.equalsIgnoreCase("ExcelRemote")) {
                sheets = objectMapper.readTree(configuration).get("sheets").toString();
            }
            rootNode = objectMapper.readTree((String) EncryptUtils.aesDecrypt(sheets));
        } catch (Exception e) {
            try {
                rootNode = objectMapper.readTree(sheets);
            } catch (Exception ex) {
                DEException.throwException(ex);
            }
        }
        if (rootNode != null) {
            for (int i = 0; i < rootNode.size(); i++) {
                result.put(rootNode.get(i).get("tableName").asText(), rootNode.get(i).get("deTableName").asText());
            }
        }
        return result;
    }

    public static String getFileName(CoreDatasource datasource) throws DEException {
        if (datasource.getType().equalsIgnoreCase("ExcelRemote")) {
            ExcelConfiguration excelConfiguration = JsonUtil.parseObject(datasource.getConfiguration(), ExcelConfiguration.class);
            for (ExcelSheetData sheet : excelConfiguration.getSheets()) {
                return sheet.getFileName();
            }
        }
        JsonNode rootNode = null;
        try {
            rootNode = objectMapper.readTree((String) EncryptUtils.aesDecrypt(datasource.getConfiguration()));
        } catch (Exception e) {
            try {
                rootNode = objectMapper.readTree(datasource.getConfiguration());
            } catch (Exception ex) {
                DEException.throwException(ex);
            }
        }
        if (rootNode != null) {
            for (int i = 0; i < rootNode.size(); i++) {
                return rootNode.get(i).get("fileName").asText();
            }
        }
        return "";
    }

    public static String getSize(CoreDatasource datasource) throws DEException {
        if (datasource.getType().equalsIgnoreCase("ExcelRemote")) {
            ExcelConfiguration excelConfiguration = JsonUtil.parseObject(datasource.getConfiguration(), ExcelConfiguration.class);
            for (ExcelSheetData sheet : excelConfiguration.getSheets()) {
                return sheet.getSize();
            }
        }
        try {
            JsonNode rootNode = objectMapper.readTree(datasource.getConfiguration());
            for (int i = 0; i < rootNode.size(); i++) {
                return rootNode.get(i).get("size").asText();
            }
        } catch (Exception e) {
            DEException.throwException(e);
        }
        return "0 B";
    }

    public List<String[]> fetchDataList(DatasourceRequest datasourceRequest) throws DEException, IOException {
        List<String[]> dataList = new ArrayList<>();
        if (datasourceRequest.getDatasource().getType().equalsIgnoreCase("ExcelRemote")) {
            ExcelConfiguration excelConfiguration = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), ExcelConfiguration.class);
            Map<String, String> fileNames = downLoadRemoteExcel(excelConfiguration);
            for (ExcelSheetData sheet : excelConfiguration.getSheets()) {
                if (sheet.getDeTableName().equalsIgnoreCase(datasourceRequest.getTable())) {
                    List<TableField> tableFields = sheet.getFields();
                    String suffix = fileNames.get("fileName").substring(fileNames.get("fileName").lastIndexOf(".") + 1);
                    InputStream inputStream = new FileInputStream(path + fileNames.get("tranName"));
                    if (StringUtils.equalsIgnoreCase(suffix, "csv")) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                        reader.readLine();//去掉表头
                        dataList = csvData(reader, false, tableFields.size());
                    } else {
                        dataList = fetchExcelDataList(sheet.getTableName(), inputStream);
                    }
                }
            }
            if (StringUtils.isNotEmpty(fileNames.get("tranName"))) {
                FileUtils.deleteFile(path + fileNames.get("tranName"));
            }
        } else {
            try {
                JsonNode rootNode = objectMapper.readTree(datasourceRequest.getDatasource().getConfiguration());
                for (int i = 0; i < rootNode.size(); i++) {
                    if (rootNode.get(i).get("deTableName").asText().equalsIgnoreCase(datasourceRequest.getTable())) {
                        List<TableField> tableFields = JsonUtil.parseList(rootNode.get(i).get("fields").toString(), TableFieldListTypeReference);
                        String suffix = rootNode.get(i).get("path").asText().substring(rootNode.get(i).get("path").asText().lastIndexOf(".") + 1);
                        InputStream inputStream = new FileInputStream(rootNode.get(i).get("path").asText());
                        if (StringUtils.equalsIgnoreCase(suffix, "csv")) {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                            reader.readLine();//去掉表头
                            dataList = csvData(reader, false, tableFields.size());
                        } else {
                            dataList = fetchExcelDataList(rootNode.get(i).get("tableName").asText(), inputStream);
                        }
                    }
                }
            } catch (Exception e) {
                DEException.throwException(e);
            }
        }
        return dataList;
    }

    private List<String[]> fetchExcelDataList(String sheetName, InputStream inputStream) {
        NoModelDataListener noModelDataListener = new NoModelDataListener();
        ExcelReader excelReader = EasyExcel.read(inputStream, noModelDataListener).build();
        List<ReadSheet> sheets = excelReader.excelExecutor().sheetList();
        for (ReadSheet readSheet : sheets) {
            if (!sheetName.equalsIgnoreCase(readSheet.getSheetName())) {
                continue;
            }
            noModelDataListener.clear();
            List<TableField> fields = new ArrayList<>();
            excelReader.read(readSheet);
            for (String s : noModelDataListener.getHeader()) {
                TableField tableFiled = new TableField();
                tableFiled.setFieldType("TEXT");
                tableFiled.setName(s);
                tableFiled.setOriginName(s);
                fields.add(tableFiled);
            }
        }
        return noModelDataListener.getData();
    }

    public static List<TableField> getTableFields(DatasourceRequest datasourceRequest) throws DEException {
        List<TableField> tableFields = new ArrayList<>();
        TypeReference<List<TableField>> listTypeReference = new TypeReference<List<TableField>>() {
        };
        try {
            String sheets = "";
            if (datasourceRequest.getDatasource().getType().equalsIgnoreCase("Excel")) {
                sheets = datasourceRequest.getDatasource().getConfiguration();
            } else {
                sheets = objectMapper.readTree(datasourceRequest.getDatasource().getConfiguration()).get("sheets").toString();
            }
            JsonNode rootNode = objectMapper.readTree(sheets);
            for (int i = 0; i < rootNode.size(); i++) {
                if (rootNode.get(i).get("deTableName").asText().equalsIgnoreCase(datasourceRequest.getTable())) {
                    tableFields = JsonUtil.parseList(rootNode.get(i).get("fields").toString(), listTypeReference);
                }
            }
        } catch (Exception e) {
            DEException.throwException(e);
        }
        return tableFields;
    }

    public ExcelFileData excelSaveAndParse(MultipartFile file, String createBy) throws DEException {
        String filename = file.getOriginalFilename();
        List<ExcelSheetData> excelSheetDataList = null;
        try {
            excelSheetDataList = parseExcel(filename, file.getInputStream(), true, filename);
        } catch (Exception e) {
            DEException.throwException(e);
        }
        List<ExcelSheetData> returnSheetDataList = new ArrayList<>();
        returnSheetDataList = excelSheetDataList;
        returnSheetDataList = returnSheetDataList.stream().filter(excelSheetData -> !CollectionUtils.isEmpty(excelSheetData.getFields())).collect(Collectors.toList());
        // save file
        String excelId = UUID.randomUUID().toString();
        String filePath = saveFile(file, excelId);

        for (ExcelSheetData excelSheetData : returnSheetDataList) {
            excelSheetData.setLastUpdateTime(System.currentTimeMillis());
            excelSheetData.setTableName(excelSheetData.getExcelLabel());
            excelSheetData.setDeTableName("excel_" + excelSheetData.getExcelLabel() + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10));
            excelSheetData.setPath(filePath);
            excelSheetData.setSheetId(UUID.randomUUID().toString());
            excelSheetData.setSheetExcelId(excelId);
            excelSheetData.setFileName(filename);
            /**
             * dataease字段类型：0-文本，1-时间，2-整型数值，3-浮点数值，4-布尔，5-地理位置，6-二进制
             */
            for (TableField field : excelSheetData.getFields()) {
                //TEXT LONG DATETIME DOUBLE
                if (field.getFieldType().equalsIgnoreCase("TEXT")) {
                    field.setDeType(0);
                    field.setDeExtractType(0);
                }
                if (field.getFieldType().equalsIgnoreCase("DATETIME")) {
                    field.setDeType(1);
                    field.setDeExtractType(1);
                }
                if (field.getFieldType().equalsIgnoreCase("LONG")) {
                    field.setDeType(2);
                    field.setDeExtractType(2);
                }
                if (field.getFieldType().equalsIgnoreCase("DOUBLE")) {
                    field.setDeType(3);
                    field.setDeExtractType(3);
                }

            }
            long size = 0;
            String unit = "B";
            if (file.getSize() / 1024 == 0) {
                size = file.getSize();
            }
            if (0 < file.getSize() / 1024 && file.getSize() / 1024 < 1024) {
                size = file.getSize() / 1024;
                unit = "KB";
            }
            if (1024 <= file.getSize() / 1024) {
                size = file.getSize() / 1024 / 1024;
                unit = "MB";
            }
            excelSheetData.setSize(size + " " + unit);
        }
        ExcelFileData excelFileData = new ExcelFileData();
        excelFileData.setExcelLabel(filename.substring(0, filename.lastIndexOf('.')));
        excelFileData.setId(excelId);
        excelFileData.setPath(filePath);
        excelFileData.setSheets(returnSheetDataList);
        return excelFileData;
    }

    public static String checkStatus(DatasourceRequest datasourceRequest) throws FileNotFoundException {
        ExcelConfiguration excelConfiguration = JsonUtil.parseObject(datasourceRequest.getDatasource().getConfiguration(), ExcelConfiguration.class);
        Map<String, String> fileNames = new HashMap<>();
        try {
            fileNames = downLoadRemoteExcel(excelConfiguration);
            return "Success";
        } catch (Exception e) {
            throw e;
        } finally {
            if (StringUtils.isNotEmpty(fileNames.get("tranName"))) {
                FileUtils.deleteFile(path + fileNames.get("tranName"));
            }
        }
    }

    public ExcelFileData parseRemoteExcel(RemoteExcelRequest remoteExcelRequest) throws DEException, FileNotFoundException {
        Map<String, String> fileNames = downLoadRemoteExcel(remoteExcelRequest);
        FileInputStream fileInputStream = new FileInputStream(path + fileNames.get("tranName"));
        List<ExcelSheetData> returnSheetDataList = new ArrayList<>();
        try {
            returnSheetDataList = parseExcel(fileNames.get("tranName"), fileInputStream, true, fileNames.get("fileName")).stream().filter(excelSheetData -> !CollectionUtils.isEmpty(excelSheetData.getFields())).collect(Collectors.toList());
        } catch (Exception e) {
            DEException.throwException(e);
        }
        for (ExcelSheetData excelSheetData : returnSheetDataList) {
            excelSheetData.setLastUpdateTime(System.currentTimeMillis());
            excelSheetData.setTableName(excelSheetData.getExcelLabel());
            excelSheetData.setDeTableName("excel_" + excelSheetData.getExcelLabel() + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10));
            excelSheetData.setPath(path + fileNames.get("tranName"));
            excelSheetData.setSheetId(UUID.randomUUID().toString());
            excelSheetData.setSheetExcelId(fileNames.get("tranName").split("\\.")[0]);
            excelSheetData.setFileName(fileNames.get("fileName"));
            /**
             * dataease字段类型：0-文本，1-时间，2-整型数值，3-浮点数值，4-布尔，5-地理位置，6-二进制
             */
            for (TableField field : excelSheetData.getFields()) {
                //TEXT LONG DATETIME DOUBLE
                if (field.getFieldType().equalsIgnoreCase("TEXT")) {
                    field.setDeType(0);
                    field.setDeExtractType(0);
                }
                if (field.getFieldType().equalsIgnoreCase("DATETIME")) {
                    field.setDeType(1);
                    field.setDeExtractType(1);
                }
                if (field.getFieldType().equalsIgnoreCase("LONG")) {
                    field.setDeType(2);
                    field.setDeExtractType(2);
                }
                if (field.getFieldType().equalsIgnoreCase("DOUBLE")) {
                    field.setDeType(3);
                    field.setDeExtractType(3);
                }
            }
            long size = 0;
            File file = new File(path + fileNames.get("tranName"));
            String unit = "B";
            if (file.length() / 1024 == 0) {
                size = file.length();
            }
            if (0 < file.length() / 1024 && file.length() / 1024 < 1024) {
                size = file.length() / 1024;
                unit = "KB";
            }
            if (1024 <= file.length() / 1024) {
                size = file.length() / 1024 / 1024;
                unit = "MB";
            }
            excelSheetData.setSize(size + " " + unit);
        }

        ExcelFileData excelFileData = new ExcelFileData();
        excelFileData.setExcelLabel(fileNames.get("fileName").split("\\.")[0]);
        excelFileData.setId(fileNames.get("tranName").split("\\.")[0]);
        excelFileData.setPath(path + fileNames.get("tranName"));
        excelFileData.setSheets(returnSheetDataList);
        if (StringUtils.isNotEmpty(fileNames.get("tranName"))) {
            FileUtils.deleteFile(path + fileNames.get("tranName"));
        }
        return excelFileData;
    }

    private static Map<String, String> downLoadRemoteExcel(ExcelConfiguration remoteExcelRequest) throws DEException, FileNotFoundException {
        Map<String, String> fileNames = new HashMap<>();
        if (remoteExcelRequest.getUrl().trim().startsWith("http")) {
            HttpClientConfig httpClientConfig = new HttpClientConfig();
            if (StringUtils.isNotEmpty(remoteExcelRequest.getUserName()) && StringUtils.isNotEmpty(remoteExcelRequest.getPasswd())) {
                String authValue = "Basic " + Base64.getUrlEncoder().encodeToString((remoteExcelRequest.getUserName() + ":" + remoteExcelRequest.getPasswd()).getBytes());
                httpClientConfig.addHeader("Authorization", authValue);
            }
            File p = new File(path);
            if (!p.exists()) {
                p.mkdirs();
            }
            fileNames = HttpClientUtil.downloadFile(remoteExcelRequest.getUrl(), httpClientConfig, path);
        } else if (remoteExcelRequest.getUrl().trim().startsWith("ftp")) {
            fileNames = downLoadFromFtp(remoteExcelRequest);
        } else {
            DEException.throwException(Translator.get("i18n_unsupported_protocol"));
        }
        return fileNames;
    }

    private static String saveFile(MultipartFile file, String fileNameUUID) throws DEException {
        String filePath = null;
        try {
            String filename = file.getOriginalFilename();
            String suffix = filename.substring(filename.lastIndexOf(".") + 1);
            File p = new File(path);
            if (!p.exists()) {
                p.mkdirs();
            }
            filePath = path + fileNameUUID + "." + suffix;
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

    private static boolean isEmpty(List<String> cells) {
        if (CollectionUtils.isEmpty(cells)) {
            return true;
        }
        boolean isEmpty = true;
        for (int i = 0; i < cells.size(); i++) {
            if (isEmpty && StringUtils.isEmpty(cells.get(i))) {
                isEmpty = true;
            } else {
                isEmpty = false;
            }
        }
        return isEmpty;
    }

    public static List<String[]> csvData(BufferedReader reader, boolean isPreview, int size) throws DEException {
        List<String[]> data = new ArrayList<>();
        try {
            int num = 1;
            String line;
            while ((line = reader.readLine()) != null) {
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
                if (!isEmpty(cells)) {
                    if (cells.size() > size) {
                        cells = cells.subList(0, size);
                    }
                    data.add(cells.toArray(new String[]{}));
                    num++;
                }
            }
        } catch (Exception e) {
            DEException.throwException(e);
        }
        return data;
    }

    private String cellType(String value) {
        if (StringUtils.isEmpty(value) || value.length() > 19) {
            return "TEXT";
        }
        String regex = "^-?\\d+(\\.\\d+)?$";
        if (!value.matches(regex)) {
            return "TEXT";
        }
        try {
            Double d = Double.valueOf(value);
            double eps = 1e-10;
            if (d - Math.floor(d) < eps) {
                if (value.contains(".")) {
                    return "DOUBLE";
                }
                return "LONG";
            } else {
                return "DOUBLE";
            }
        } catch (Exception e2) {
            return "TEXT";
        }
    }

    private void cellType(String value, int i, TableField tableFiled) {
        if (StringUtils.isEmpty(value)) {
            return;
        }
        if (i == 0) {
            tableFiled.setFieldType(cellType(value));
        } else {
            String type = cellType(value);
            if (tableFiled.getFieldType() == null) {
                tableFiled.setFieldType(type);
            } else {
                if (type.equalsIgnoreCase("TEXT")) {
                    tableFiled.setFieldType(type);
                }
                if (type.equalsIgnoreCase("DOUBLE") && tableFiled.getFieldType().equalsIgnoreCase("LONG")) {
                    tableFiled.setFieldType(type);
                }
            }
        }

    }

    @Data
    public class NoModelDataListener extends AnalysisEventListener<Map<Integer, String>> {
        private List<String[]> data = new ArrayList<>();
        private List<String> header = new ArrayList<>();
        private List<Integer> headerKey = new ArrayList<>();

        @Override
        public void invokeHead(Map<Integer, ReadCellData<?>> headMap, AnalysisContext context) {
            super.invokeHead(headMap, context);
            for (Integer key : headMap.keySet()) {
                ReadCellData<?> cellData = headMap.get(key);
                String value = null;
                if (cellData.getType().equals(CellDataTypeEnum.STRING)) {
                    value = cellData.getStringValue();
                }
                if (cellData.getType().equals(CellDataTypeEnum.NUMBER)) {
                    value = cellData.getNumberValue().toString();
                }
                if (StringUtils.isEmpty(value)) {
                    continue;
                }
                headerKey.add(key);
                header.add(value);
            }
        }

        @Override
        public void invoke(Map<Integer, String> dataMap, AnalysisContext context) {
            List<String> line = new ArrayList<>();
            for (Integer key : dataMap.keySet()) {
                String value = dataMap.get(key);
                if (StringUtils.isEmpty(value)) {
                    value = null;
                }
                if (headerKey.contains(key)) {
                    line.add(value);
                }
            }
            int size = line.size();
            if (size < header.size()) {
                for (int i = 0; i < header.size() - size; i++) {
                    line.add(null);
                }
            }
            data.add(line.toArray(new String[line.size()]));
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        }

        public void clear() {
            data.clear();
            header.clear();
        }
    }


    private List<ExcelSheetData> parseExcel(String filename, InputStream inputStream, boolean isPreview, String originFilename) throws IOException {
        List<ExcelSheetData> excelSheetDataList = new ArrayList<>();
        String suffix = filename.substring(filename.lastIndexOf(".") + 1);
        if (StringUtils.equalsIgnoreCase(suffix, "xlsx") || StringUtils.equalsIgnoreCase(suffix, "xls")) {
            NoModelDataListener noModelDataListener = new NoModelDataListener();
            ExcelReader excelReader = EasyExcel.read(inputStream, noModelDataListener).build();
            List<ReadSheet> sheets = excelReader.excelExecutor().sheetList();
            for (ReadSheet readSheet : sheets) {
                noModelDataListener.clear();
                List<TableField> fields = new ArrayList<>();
                excelReader.read(readSheet);
                if (CollectionUtils.isEmpty(noModelDataListener.getHeader())) {
                    DEException.throwException(readSheet.getSheetName() + "首行不能为空！");
                }
                for (String s : noModelDataListener.getHeader()) {
                    TableField tableFiled = new TableField();
                    tableFiled.setFieldType(null);
                    tableFiled.setName(s);
                    tableFiled.setOriginName(s);
                    tableFiled.setChecked(true);
                    fields.add(tableFiled);
                }
                List<String[]> data = new ArrayList<>(noModelDataListener.getData());
                if (isPreview) {
                    for (int i = 0; i < data.size(); i++) {
                        for (int j = 0; j < data.get(i).length; j++) {
                            if (j < fields.size()) {
                                cellType(data.get(i)[j], i, fields.get(j));
                            }
                        }
                    }
                    if (data.size() > 100) {
                        data = data.subList(0, 100);
                    }
                }

                for (int i = 0; i < fields.size(); i++) {
                    if (StringUtils.isEmpty(fields.get(i).getFieldType())) {
                        fields.get(i).setFieldType("TEXT");
                    }
                }

                ExcelSheetData excelSheetData = new ExcelSheetData();
                excelSheetData.setFields(fields);
                excelSheetData.setData(data);
                excelSheetData.setFileName(filename);
                excelSheetData.setExcelLabel(readSheet.getSheetName());
                excelSheetDataList.add(excelSheetData);
            }
        }

        if (StringUtils.equalsIgnoreCase(suffix, "csv")) {
            List<TableField> fields = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String s = reader.readLine();// first line
            if (StringUtils.isNotEmpty(s)) {
                String[] split = s.split(",");
                for (int i = 0; i < split.length; i++) {
                    String filedName = split[i];
                    if (StringUtils.isEmpty(filedName)) {
                        DEException.throwException(Translator.get("i18n_excel_error_first_row"));
                    }
                    if (filedName.startsWith(UFEFF)) {
                        filedName = filedName.replace(UFEFF, "");
                    }
                    TableField tableFiled = new TableField();
                    tableFiled.setName(filedName);
                    tableFiled.setOriginName(filedName);
                    tableFiled.setFieldType(null);
                    tableFiled.setChecked(true);
                    fields.add(tableFiled);
                }
            }

            List<String[]> data = csvData(reader, isPreview, fields.size());
            if (isPreview) {
                for (int i = 0; i < data.size(); i++) {
                    for (int j = 0; j < data.get(i).length; j++) {
                        if (j < fields.size()) {
                            cellType(data.get(i)[j], i, fields.get(j));
                        }
                    }
                }
                if (data.size() > 100) {
                    data = data.subList(0, 100);
                }
            }
            for (int i = 0; i < fields.size(); i++) {
                if (StringUtils.isEmpty(fields.get(i).getFieldType())) {
                    fields.get(i).setFieldType("TEXT");
                }
            }

            ExcelSheetData excelSheetData = new ExcelSheetData();
            String[] fieldArray = fields.stream().map(TableField::getName).toArray(String[]::new);
            excelSheetData.setFields(fields);
            excelSheetData.setData(data);
            excelSheetData.setFileName(filename);
            excelSheetData.setExcelLabel(originFilename.substring(0, originFilename.lastIndexOf('.')));
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
        }

        return excelSheetDataList;
    }

    public static Map<String, String> downLoadFromFtp(ExcelConfiguration remoteExcelRequest) {
        Map<String, String> fileNames = new HashMap<>();
        String username = "";
        String password = "";
        String serverAddress = "";
        String filePath = "";
        if (remoteExcelRequest.getUrl().contains("@")) {
            String regex = "ftp://([^:]+):([^@]+)@([^/]+)(.*)";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(remoteExcelRequest.getUrl());
            if (matcher.find()) {
                username = matcher.group(1);
                password = matcher.group(2);
                serverAddress = matcher.group(3);
                filePath = matcher.group(4);
            } else {
                DEException.throwException(Translator.get("i18n_invalid_address"));
            }
        } else {
            String regex = "ftp://([^/]+)(.*)";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(remoteExcelRequest.getUrl());
            if (matcher.find()) {
                serverAddress = matcher.group(1);
                filePath = matcher.group(2);
            } else {
                DEException.throwException(Translator.get("i18n_invalid_address"));
            }
        }
        if (StringUtils.isNotEmpty(remoteExcelRequest.getUserName())) {
            username = remoteExcelRequest.getUserName();
        }
        if (StringUtils.isNotEmpty(remoteExcelRequest.getPasswd())) {
            password = remoteExcelRequest.getPasswd();
        }
        filePath = filePath.startsWith("/") ? filePath.substring(1) : filePath;
        String suffix = filePath.substring(filePath.lastIndexOf(".") + 1);
        if (!Arrays.asList("csv", "xlsx", "xls").contains(suffix)) {
            DEException.throwException(Translator.get("i18n_unsupported_file_format"));
        }
        String tranName = UUID.randomUUID().toString() + "." + suffix;
        String localFilePath = path + tranName;
        fileNames.put("fileName", filePath);
        fileNames.put("tranName", tranName);

        try {
            URL url;
            if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
                url = new URL("ftp://" + username + ":" + URLEncoder.encode(password, StandardCharsets.UTF_8) + "@" + serverAddress + "/" + filePath);
            } else {
                url = new URL("ftp://" + serverAddress + "/" + filePath);
            }

            URLConnection conn = url.openConnection();
            InputStream inputStream = conn.getInputStream();
            FileOutputStream outputStream = new FileOutputStream(localFilePath);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();
            outputStream.close();

        } catch (IOException e) {
            DEException.throwException(Translator.get("i18n_file_download_failed") + ", " + e.getMessage());
        }
        return fileNames;
    }

}
