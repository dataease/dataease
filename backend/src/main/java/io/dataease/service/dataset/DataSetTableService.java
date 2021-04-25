package io.dataease.service.dataset;


import com.google.gson.Gson;
import io.dataease.base.domain.*;
import io.dataease.base.mapper.DatasetTableIncrementalConfigMapper;
import io.dataease.base.mapper.DatasetTableMapper;
import io.dataease.base.mapper.DatasourceMapper;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.controller.request.dataset.DataSetTableRequest;
import io.dataease.datasource.constants.DatasourceTypes;
import io.dataease.datasource.dto.TableFiled;
import io.dataease.datasource.provider.DatasourceProvider;
import io.dataease.datasource.provider.ProviderFactory;
import io.dataease.datasource.request.DatasourceRequest;
import io.dataease.dto.dataset.DataTableInfoDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author gin
 * @Date 2021/2/23 2:54 下午
 */
@Service
public class DataSetTableService {
    @Resource
    private DatasetTableMapper datasetTableMapper;
    @Resource
    private DatasourceMapper datasourceMapper;
    @Resource
    private DataSetTableFieldsService dataSetTableFieldsService;
    @Resource
    private DataSetTableTaskService dataSetTableTaskService;
    @Resource
    private DatasetTableIncrementalConfigMapper datasetTableIncrementalConfigMapper;
    @Value("${upload.file.path}")
    private String path;

    public void batchInsert(List<DatasetTable> datasetTable) throws Exception {
        for (DatasetTable table : datasetTable) {
            save(table);
        }
    }

    public DatasetTable save(DatasetTable datasetTable) throws Exception {
        checkName(datasetTable);
        if (StringUtils.isEmpty(datasetTable.getId())) {
            datasetTable.setId(UUID.randomUUID().toString());
            datasetTable.setCreateBy(AuthUtils.getUser().getUsername());
            datasetTable.setCreateTime(System.currentTimeMillis());
            DataTableInfoDTO dataTableInfoDTO = new DataTableInfoDTO();
            if (StringUtils.equalsIgnoreCase("db", datasetTable.getType())) {
                dataTableInfoDTO.setTable(datasetTable.getName());
                datasetTable.setInfo(new Gson().toJson(dataTableInfoDTO));
            }
            int insert = datasetTableMapper.insert(datasetTable);
            // 添加表成功后，获取当前表字段和类型，抽象到dataease数据库
            if (insert == 1) {
                saveTableField(datasetTable);
            }
        } else {
            datasetTableMapper.updateByPrimaryKeySelective(datasetTable);
        }
        return datasetTable;
    }

    public void delete(String id) {
        datasetTableMapper.deleteByPrimaryKey(id);
        dataSetTableFieldsService.deleteByTableId(id);
        // 删除同步任务
        dataSetTableTaskService.deleteByTableId(id);
    }

    public List<DatasetTable> list(DataSetTableRequest dataSetTableRequest) {
        DatasetTableExample datasetTableExample = new DatasetTableExample();
        DatasetTableExample.Criteria criteria = datasetTableExample.createCriteria();
        criteria.andCreateByEqualTo(AuthUtils.getUser().getUsername());
        if (StringUtils.isNotEmpty(dataSetTableRequest.getSceneId())) {
            criteria.andSceneIdEqualTo(dataSetTableRequest.getSceneId());
        }
        if (StringUtils.isNotEmpty(dataSetTableRequest.getSort())) {
            datasetTableExample.setOrderByClause(dataSetTableRequest.getSort());
        }
        return datasetTableMapper.selectByExampleWithBLOBs(datasetTableExample);
    }

    public DatasetTable get(String id) {
        return datasetTableMapper.selectByPrimaryKey(id);
    }

    public List<TableFiled> getFields(DataSetTableRequest dataSetTableRequest) throws Exception {
        Datasource ds = datasourceMapper.selectByPrimaryKey(dataSetTableRequest.getDataSourceId());
        DatasourceProvider datasourceProvider = ProviderFactory.getProvider(ds.getType());
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(ds);
        datasourceRequest.setTable(new Gson().fromJson(dataSetTableRequest.getInfo(), DataTableInfoDTO.class).getTable());
        return datasourceProvider.getTableFileds(datasourceRequest);
    }

    public Map<String, List<DatasetTableField>> getFieldsFromDE(DataSetTableRequest dataSetTableRequest) throws Exception {
        DatasetTableField datasetTableField = DatasetTableField.builder().build();
        datasetTableField.setTableId(dataSetTableRequest.getId());
        datasetTableField.setChecked(Boolean.TRUE);
        List<DatasetTableField> fields = dataSetTableFieldsService.list(datasetTableField);

        List<DatasetTableField> dimension = new ArrayList<>();
        List<DatasetTableField> quota = new ArrayList<>();

        fields.forEach(field -> {
            if (field.getDeType() == 2 || field.getDeType() == 3) {
                quota.add(field);
            } else {
                dimension.add(field);
            }
        });
        // quota add count
        DatasetTableField count = DatasetTableField.builder()
                .id("count")
                .tableId(dataSetTableRequest.getId())
                .originName("*")
                .name("记录数*")
                .type("INT")
                .checked(true)
                .columnIndex(999)
                .deType(2)
                .build();
        quota.add(count);

        Map<String, List<DatasetTableField>> map = new HashMap<>();
        map.put("dimension", dimension);
        map.put("quota", quota);

        return map;
    }

    public List<String[]> getData(DataSetTableRequest dataSetTableRequest) throws Exception {
        Datasource ds = datasourceMapper.selectByPrimaryKey(dataSetTableRequest.getDataSourceId());
        DatasourceProvider datasourceProvider = ProviderFactory.getProvider(ds.getType());
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(ds);
        String table = new Gson().fromJson(dataSetTableRequest.getInfo(), DataTableInfoDTO.class).getTable();

        DatasetTableField datasetTableField = DatasetTableField.builder().build();
        datasetTableField.setTableId(dataSetTableRequest.getId());
        datasetTableField.setChecked(Boolean.TRUE);
        List<DatasetTableField> fields = dataSetTableFieldsService.list(datasetTableField);
        String[] fieldArray = fields.stream().map(DatasetTableField::getOriginName).toArray(String[]::new);
        datasourceRequest.setQuery(createQuerySQL(ds.getType(), table, fieldArray));

        return datasourceProvider.getData(datasourceRequest);
    }

    public Map<String, Object> getPreviewData(DataSetTableRequest dataSetTableRequest) throws Exception {
        DatasetTableField datasetTableField = DatasetTableField.builder().build();
        datasetTableField.setTableId(dataSetTableRequest.getId());
        datasetTableField.setChecked(Boolean.TRUE);
        List<DatasetTableField> fields = dataSetTableFieldsService.list(datasetTableField);
        String[] fieldArray = fields.stream().map(DatasetTableField::getOriginName).toArray(String[]::new);

        DataTableInfoDTO dataTableInfoDTO = new Gson().fromJson(dataSetTableRequest.getInfo(), DataTableInfoDTO.class);
        DatasetTable datasetTable = datasetTableMapper.selectByPrimaryKey(dataSetTableRequest.getId());

        List<String[]> data = new ArrayList<>();
        if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "db")) {
            Datasource ds = datasourceMapper.selectByPrimaryKey(dataSetTableRequest.getDataSourceId());
            DatasourceProvider datasourceProvider = ProviderFactory.getProvider(ds.getType());
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(ds);

            String table = dataTableInfoDTO.getTable();
            datasourceRequest.setQuery(createQuerySQL(ds.getType(), table, fieldArray) + " LIMIT 0," + dataSetTableRequest.getRow());

            try {
                data.addAll(datasourceProvider.getData(datasourceRequest));
            } catch (Exception e) {
            }
        } else if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "sql")) {
            Datasource ds = datasourceMapper.selectByPrimaryKey(dataSetTableRequest.getDataSourceId());
            DatasourceProvider datasourceProvider = ProviderFactory.getProvider(ds.getType());
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(ds);

            String sql = dataTableInfoDTO.getSql();
            datasourceRequest.setQuery(createQuerySQL(ds.getType(), " (" + sql + ") AS tmp ", fieldArray) + " LIMIT 0," + dataSetTableRequest.getRow());

            try {
                data.addAll(datasourceProvider.getData(datasourceRequest));
            } catch (Exception e) {
            }
        } else if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "excel")) {

        } else if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "custom")) {

        }

        List<Map<String, Object>> jsonArray = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(data)) {
            jsonArray = data.stream().map(ele -> {
                Map<String, Object> map = new HashMap<>();
                for (int i = 0; i < ele.length; i++) {
                    map.put(fieldArray[i], ele[i]);
                }
                return map;
            }).collect(Collectors.toList());
        }

        Map<String, Object> map = new HashMap<>();
        map.put("fields", fields);
        map.put("data", jsonArray);

        return map;
    }

    public Map<String, Object> getSQLPreview(DataSetTableRequest dataSetTableRequest) throws Exception {
        Datasource ds = datasourceMapper.selectByPrimaryKey(dataSetTableRequest.getDataSourceId());
        DatasourceProvider datasourceProvider = ProviderFactory.getProvider(ds.getType());
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(ds);
        String sql = new Gson().fromJson(dataSetTableRequest.getInfo(), DataTableInfoDTO.class).getSql();
        datasourceRequest.setQuery(sql);
        ResultSet dataResultSet = datasourceProvider.getDataResultSet(datasourceRequest);
        List<String[]> data = datasourceProvider.fetchResult(dataResultSet);
        List<TableFiled> fields = datasourceProvider.fetchResultField(dataResultSet);
        String[] fieldArray = fields.stream().map(TableFiled::getFieldName).toArray(String[]::new);

        List<Map<String, Object>> jsonArray = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(data)) {
            jsonArray = data.stream().map(ele -> {
                Map<String, Object> map = new HashMap<>();
                for (int i = 0; i < ele.length; i++) {
                    map.put(fieldArray[i], ele[i]);
                }
                return map;
            }).collect(Collectors.toList());
        }

        Map<String, Object> map = new HashMap<>();
        map.put("fields", fields);
        map.put("data", jsonArray);

        return map;
    }

    public List<String[]> getDataSetData(String datasourceId, String table, List<DatasetTableField> fields) {
        List<String[]> data = new ArrayList<>();
        Datasource ds = datasourceMapper.selectByPrimaryKey(datasourceId);
        DatasourceProvider datasourceProvider = ProviderFactory.getProvider(ds.getType());
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(ds);
        String[] fieldArray = fields.stream().map(DatasetTableField::getOriginName).toArray(String[]::new);
        datasourceRequest.setQuery(createQuerySQL(ds.getType(), table, fieldArray) + " LIMIT 0, 10");
        try {
            data.addAll(datasourceProvider.getData(datasourceRequest));
        } catch (Exception e) {
        }
        return data;
    }

    public Long getDataSetTotalData(String datasourceId, String table) {
        List<String[]> data = new ArrayList<>();
        Datasource ds = datasourceMapper.selectByPrimaryKey(datasourceId);
        DatasourceProvider datasourceProvider = ProviderFactory.getProvider(ds.getType());
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(ds);
        datasourceRequest.setQuery("select count(*) from " + table);
        try {
            return datasourceProvider.count(datasourceRequest);
        } catch (Exception e) {

        }
        return 0l;
    }

    public List<String[]> getDataSetPageData(String datasourceId, String table, List<DatasetTableField> fields, Long startPage, Long pageSize) {
        List<String[]> data = new ArrayList<>();
        Datasource ds = datasourceMapper.selectByPrimaryKey(datasourceId);
        DatasourceProvider datasourceProvider = ProviderFactory.getProvider(ds.getType());
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(ds);
        String[] fieldArray = fields.stream().map(DatasetTableField::getOriginName).toArray(String[]::new);
        datasourceRequest.setPageSize(pageSize);
        datasourceRequest.setStartPage(startPage);
        datasourceRequest.setQuery(createQuerySQL(ds.getType(), table, fieldArray));
        try {
            return datasourceProvider.getData(datasourceRequest);
        } catch (Exception e) {
        }
        return data;
    }

    public List<String[]> getDataSetDataBySql(String datasourceId, String table, String sql) {
        List<String[]> data = new ArrayList<>();
        Datasource ds = datasourceMapper.selectByPrimaryKey(datasourceId);
        DatasourceProvider datasourceProvider = ProviderFactory.getProvider(ds.getType());
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(ds);
        datasourceRequest.setQuery(sql);
        try {
            return datasourceProvider.getData(datasourceRequest);
        } catch (Exception e) {
        }
        return data;
    }

    public void saveTableField(DatasetTable datasetTable) throws Exception {
        Datasource ds = datasourceMapper.selectByPrimaryKey(datasetTable.getDataSourceId());
        DataSetTableRequest dataSetTableRequest = new DataSetTableRequest();
        BeanUtils.copyBean(dataSetTableRequest, datasetTable);

        List<TableFiled> fields = new ArrayList<>();
        long syncTime = System.currentTimeMillis();
        if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "db")) {
            fields = getFields(dataSetTableRequest);
        } else if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "sql")) {
            DatasourceProvider datasourceProvider = ProviderFactory.getProvider(ds.getType());
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(ds);
            datasourceRequest.setQuery(new Gson().fromJson(dataSetTableRequest.getInfo(), DataTableInfoDTO.class).getSql());
            ResultSet dataResultSet = datasourceProvider.getDataResultSet(datasourceRequest);
            fields = datasourceProvider.fetchResultField(dataResultSet);
        } else if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "excel")) {
            DataTableInfoDTO dataTableInfoDTO = new Gson().fromJson(dataSetTableRequest.getInfo(), DataTableInfoDTO.class);
            String path = dataTableInfoDTO.getData();
            File file = new File(path);
            // save field
            Map<String, Object> map = parseExcel(path.substring(path.lastIndexOf("/") + 1), new FileInputStream(file), false);
            fields = (List<TableFiled>) map.get("fields");
            List<Map<String, Object>> data = (List<Map<String, Object>>) map.get("data");
            // save data
        } else if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "custom")) {
            // save field

            // save data
        }
        if (CollectionUtils.isNotEmpty(fields)) {
            for (int i = 0; i < fields.size(); i++) {
                TableFiled filed = fields.get(i);
                DatasetTableField datasetTableField = DatasetTableField.builder().build();
                datasetTableField.setTableId(datasetTable.getId());
                datasetTableField.setOriginName(filed.getFieldName());
                datasetTableField.setName(filed.getRemarks());
                datasetTableField.setType(filed.getFieldType());
                if (ObjectUtils.isEmpty(ds)) {
                    datasetTableField.setDeType(transFieldType(filed.getFieldType()));
                } else {
                    datasetTableField.setDeType(transFieldType(ds.getType(), filed.getFieldType()));
                }
                datasetTableField.setChecked(true);
                datasetTableField.setColumnIndex(i);
                datasetTableField.setLastSyncTime(syncTime);
                dataSetTableFieldsService.save(datasetTableField);
            }
        }
    }

    public String createQuerySQL(String type, String table, String[] fields) {
        DatasourceTypes datasourceType = DatasourceTypes.valueOf(type);
        switch (datasourceType) {
            case mysql:
                return MessageFormat.format("SELECT {0} FROM {1}", StringUtils.join(fields, ","), table);
            case sqlServer:
                return MessageFormat.format("SELECT {0} FROM {1}", StringUtils.join(fields, ","), table);
            default:
                return MessageFormat.format("SELECT {0} FROM {1}", StringUtils.join(fields, ","), table);
        }
    }

    public Integer transFieldType(String field) {
        switch (field) {
            case "TEXT":
                return 0;
            case "TIME":
                return 1;
            case "INT":
                return 2;
            case "DOUBLE":
                return 3;
            default:
                return 0;
        }
    }

    public Integer transFieldType(String type, String field) {
        DatasourceTypes datasourceType = DatasourceTypes.valueOf(type);
        switch (datasourceType) {
            case mysql:
                return transMysqlField(field);
            case sqlServer:
            default:
                return 0;
        }
    }

    public Integer transMysqlField(String field) {
        switch (field) {
            case "CHAR":
            case "VARCHAR":
            case "TEXT":
            case "TINYTEXT":
            case "MEDIUMTEXT":
            case "LONGTEXT":
            case "ENUM":
                return 0;// 文本
            case "DATE":
            case "TIME":
            case "YEAR":
            case "DATETIME":
            case "TIMESTAMP":
                return 1;// 时间
            case "INT":
            case "BIT":
            case "TINYINT":
            case "SMALLINT":
            case "MEDIUMINT":
            case "INTEGER":
            case "BIGINT":
                return 2;// 整型
            case "FLOAT":
            case "DOUBLE":
            case "DECIMAL":
                return 3;// 浮点
            default:
                return 0;
        }
    }

    public DatasetTableIncrementalConfig incrementalConfig(DatasetTableIncrementalConfig datasetTableIncrementalConfig) {
        if (StringUtils.isEmpty(datasetTableIncrementalConfig.getTableId())) {
            return new DatasetTableIncrementalConfig();
        }
        DatasetTableIncrementalConfigExample example = new DatasetTableIncrementalConfigExample();
        example.createCriteria().andTableIdEqualTo(datasetTableIncrementalConfig.getTableId());
        List<DatasetTableIncrementalConfig> configs = datasetTableIncrementalConfigMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(configs)) {
            return configs.get(0);
        } else {
            return new DatasetTableIncrementalConfig();
        }
    }

    public DatasetTableIncrementalConfig incrementalConfig(String datasetTableId) {
        DatasetTableIncrementalConfig datasetTableIncrementalConfig = new DatasetTableIncrementalConfig();
        datasetTableIncrementalConfig.setTableId(datasetTableId);
        return incrementalConfig(datasetTableIncrementalConfig);
    }


    public void saveIncrementalConfig(DatasetTableIncrementalConfig datasetTableIncrementalConfig) {
        if (StringUtils.isEmpty(datasetTableIncrementalConfig.getId())) {
            datasetTableIncrementalConfig.setId(UUID.randomUUID().toString());
            datasetTableIncrementalConfigMapper.insertSelective(datasetTableIncrementalConfig);
        } else {
            DatasetTableIncrementalConfigExample example = new DatasetTableIncrementalConfigExample();
            example.createCriteria().andTableIdEqualTo(datasetTableIncrementalConfig.getTableId());
            datasetTableIncrementalConfigMapper.updateByExample(datasetTableIncrementalConfig, example);
        }
    }

    private void checkName(DatasetTable datasetTable) {
        if (StringUtils.isEmpty(datasetTable.getId()) && StringUtils.equalsIgnoreCase("db", datasetTable.getType())) {
            return;
        }
        DatasetTableExample datasetTableExample = new DatasetTableExample();
        DatasetTableExample.Criteria criteria = datasetTableExample.createCriteria();
        if (StringUtils.isNotEmpty(datasetTable.getId())) {
            criteria.andIdNotEqualTo(datasetTable.getId());
        }
        if (StringUtils.isNotEmpty(datasetTable.getSceneId())) {
            criteria.andSceneIdEqualTo(datasetTable.getSceneId());
        }
        if (StringUtils.isNotEmpty(datasetTable.getName())) {
            criteria.andNameEqualTo(datasetTable.getName());
        }
        List<DatasetTable> list = datasetTableMapper.selectByExample(datasetTableExample);
        if (list.size() > 0) {
            throw new RuntimeException("Name can't repeat in same group.");
        }
    }

    public Map<String, Object> getDatasetDetail(String id) {
        Map<String, Object> map = new HashMap<>();
        DatasetTable table = datasetTableMapper.selectByPrimaryKey(id);
        map.put("table", table);
        if (ObjectUtils.isNotEmpty(table)) {
            Datasource datasource = datasourceMapper.selectByPrimaryKey(table.getDataSourceId());
            map.put("datasource", datasource);
        }
        return map;
    }

    public Map<String, Object> excelSaveAndParse(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename();
        // parse file
        Map<String, Object> fileMap = parseExcel(filename, file.getInputStream(), true);
        // save file
        String filePath = saveFile(file);
        Map<String, Object> map = new HashMap<>(fileMap);
        map.put("path", filePath);
        return map;
    }

    private Map<String, Object> parseExcel(String filename, InputStream inputStream, boolean isPreview) throws Exception {
        String suffix = filename.substring(filename.lastIndexOf(".") + 1);
        List<TableFiled> fields = new ArrayList<>();
        List<String[]> data = new ArrayList<>();
        List<Map<String, Object>> jsonArray = new ArrayList<>();

        if (StringUtils.equalsIgnoreCase(suffix, "xls")) {
            HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
            HSSFSheet sheet0 = workbook.getSheetAt(0);
            if (sheet0.getNumMergedRegions() > 0) {
                throw new RuntimeException("Sheet have merged regions.");
            }
            int rows;
            if (isPreview) {
                rows = Math.min(sheet0.getPhysicalNumberOfRows(), 100);
            } else {
                rows = sheet0.getPhysicalNumberOfRows();
            }
            for (int i = 0; i < rows; i++) {
                HSSFRow row = sheet0.getRow(i);
                String[] r = new String[row.getPhysicalNumberOfCells()];
                for (int j = 0; j < row.getPhysicalNumberOfCells(); j++) {
                    if (i == 0) {
                        TableFiled tableFiled = new TableFiled();
                        tableFiled.setFieldName(readCell(row.getCell(j)));
                        tableFiled.setRemarks(readCell(row.getCell(j)));
                        tableFiled.setFieldType("TEXT");
                        fields.add(tableFiled);
                    } else {
                        r[j] = readCell(row.getCell(j));
                    }
                }
                if (i > 0) {
                    data.add(r);
                }
            }
        } else if (StringUtils.equalsIgnoreCase(suffix, "xlsx")) {
            XSSFWorkbook xssfWorkbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet0 = xssfWorkbook.getSheetAt(0);
            if (sheet0.getNumMergedRegions() > 0) {
                throw new RuntimeException("Sheet have merged regions.");
            }
            int rows;
            if (isPreview) {
                rows = Math.min(sheet0.getPhysicalNumberOfRows(), 100);
            } else {
                rows = sheet0.getPhysicalNumberOfRows();
            }
            for (int i = 0; i < rows; i++) {
                XSSFRow row = sheet0.getRow(i);
                String[] r = new String[row.getPhysicalNumberOfCells()];
                for (int j = 0; j < row.getPhysicalNumberOfCells(); j++) {
                    if (i == 0) {
                        TableFiled tableFiled = new TableFiled();
                        tableFiled.setFieldName(readCell(row.getCell(j)));
                        tableFiled.setRemarks(readCell(row.getCell(j)));
                        tableFiled.setFieldType("TEXT");
                        fields.add(tableFiled);
                    } else {
                        r[j] = readCell(row.getCell(j));
                    }
                }
                if (i > 0) {
                    data.add(r);
                }
            }
        } else if (StringUtils.equalsIgnoreCase(suffix, "csv")) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String s = reader.readLine();// first line
            String[] split = s.split(",");
            for (String s1 : split) {
                TableFiled tableFiled = new TableFiled();
                tableFiled.setFieldName(s1);
                tableFiled.setRemarks(s1);
                tableFiled.setFieldType("TEXT");
                fields.add(tableFiled);
            }
            int num = 1;
            String line;
            while ((line = reader.readLine()) != null) {
                if (isPreview) {
                    if (num > 100) {
                        break;
                    }
                }
                data.add(line.split(","));
                num++;
            }
        }

        String[] fieldArray = fields.stream().map(TableFiled::getFieldName).toArray(String[]::new);
        if (CollectionUtils.isNotEmpty(data)) {
            jsonArray = data.stream().map(ele -> {
                Map<String, Object> map = new HashMap<>();
                for (int i = 0; i < ele.length; i++) {
                    map.put(fieldArray[i], ele[i]);
                }
                return map;
            }).collect(Collectors.toList());
        }
        inputStream.close();

        Map<String, Object> map = new HashMap<>();
        map.put("fields", fields);
        map.put("data", jsonArray);
        return map;
    }

    private String readCell(Cell cell) {
        CellType cellTypeEnum = cell.getCellTypeEnum();
        if (cellTypeEnum.equals(CellType.STRING)) {
            return cell.getStringCellValue();
        } else if (cellTypeEnum.equals(CellType.NUMERIC)) {
            double d = cell.getNumericCellValue();
            try {
                return new Double(d).longValue() + "";
            } catch (Exception e) {
                BigDecimal b = new BigDecimal(d);
                return b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() + "";
            }
        } else if (cellTypeEnum.equals(CellType.BOOLEAN)) {
            return cell.getBooleanCellValue() ? "1" : "0";
        } else {
            return "";
        }
    }

    private String saveFile(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename();
        String dirPath = path + AuthUtils.getUser().getUsername() + "/";
        File p = new File(dirPath);
        if (!p.exists()) {
            p.mkdirs();
        }
        String filePath = dirPath + filename;
        File f = new File(filePath);
        FileOutputStream fileOutputStream = new FileOutputStream(f);
        fileOutputStream.write(file.getBytes());
        fileOutputStream.flush();
        fileOutputStream.close();
        return filePath;
    }
}
