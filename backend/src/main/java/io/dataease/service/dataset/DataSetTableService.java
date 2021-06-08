package io.dataease.service.dataset;


import com.fit2cloud.quartz.anno.QuartzScheduled;
import com.google.gson.Gson;
import io.dataease.base.domain.*;
import io.dataease.base.mapper.*;
import io.dataease.base.mapper.ext.ExtDataSetTableMapper;
import io.dataease.base.mapper.ext.UtilMapper;
import io.dataease.commons.constants.JobStatus;
import io.dataease.commons.utils.*;
import io.dataease.controller.request.dataset.DataSetTableRequest;
import io.dataease.datasource.dto.TableFiled;
import io.dataease.datasource.provider.DatasourceProvider;
import io.dataease.datasource.provider.JdbcProvider;
import io.dataease.datasource.provider.ProviderFactory;
import io.dataease.datasource.request.DatasourceRequest;
import io.dataease.dto.dataset.DataSetPreviewPage;
import io.dataease.dto.dataset.DataSetTableDTO;
import io.dataease.dto.dataset.DataSetTableUnionDTO;
import io.dataease.dto.dataset.DataTableInfoCustomUnion;
import io.dataease.dto.dataset.DataTableInfoDTO;
import io.dataease.i18n.Translator;
import io.dataease.provider.DDLProvider;
import io.dataease.provider.QueryProvider;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author gin
 * @Date 2021/2/23 2:54 下午
 */
@Service
@Transactional(rollbackFor = Exception.class)
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
    private CommonThreadPool commonThreadPool;
    @Resource
    private ExtractDataService extractDataService;
    @Resource
    private ExtDataSetTableMapper extDataSetTableMapper;
    @Resource
    private DatasetTableIncrementalConfigMapper datasetTableIncrementalConfigMapper;
    @Resource
    private DataSetTableUnionService dataSetTableUnionService;
    @Resource
    private DataSetTableTaskLogService dataSetTableTaskLogService;
    @Resource
    private QrtzSchedulerStateMapper qrtzSchedulerStateMapper;
    @Resource
    private DatasetTableTaskLogMapper datasetTableTaskLogMapper;
    @Value("${upload.file.path}")
    private String path;

    public void batchInsert(List<DataSetTableRequest> datasetTable) throws Exception {
        for (DataSetTableRequest table : datasetTable) {
            save(table);
        }
    }

    public DatasetTable save(DataSetTableRequest datasetTable) throws Exception {
        checkName(datasetTable);
        if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "sql")) {
            DataSetTableRequest dataSetTableRequest = new DataSetTableRequest();
            BeanUtils.copyBean(dataSetTableRequest, datasetTable);
            getSQLPreview(dataSetTableRequest);
        }
        if (StringUtils.isEmpty(datasetTable.getId())) {
            datasetTable.setId(UUID.randomUUID().toString());
            datasetTable.setCreateBy(AuthUtils.getUser().getUsername());
            datasetTable.setCreateTime(System.currentTimeMillis());
            DataTableInfoDTO dataTableInfoDTO = new DataTableInfoDTO();
            int insert = datasetTableMapper.insert(datasetTable);
            // 添加表成功后，获取当前表字段和类型，抽象到dataease数据库
            if (insert == 1) {
                saveTableField(datasetTable);
                if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "excel")) {
                    commonThreadPool.addTask(() -> {
                        extractDataService.extractData(datasetTable.getId(), null, "all_scope", null);
                    });
                }
            }
        } else {
            int update = datasetTableMapper.updateByPrimaryKeySelective(datasetTable);
            if (datasetTable.getIsRename() == null || !datasetTable.getIsRename()) {
                // 更新数据和字段
                if (update == 1) {
                    if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "sql") || StringUtils.equalsIgnoreCase(datasetTable.getType(), "custom")) {
                        // 删除所有字段，重新抽象
                        dataSetTableFieldsService.deleteByTableId(datasetTable.getId());
                        saveTableField(datasetTable);
                    }
                    if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "excel")) {
                        if (datasetTable.getEditType() == 0) {
                            commonThreadPool.addTask(() -> {
                                extractDataService.extractData(datasetTable.getId(), null, "all_scope", null);
                            });
                        } else if (datasetTable.getEditType() == 1) {
                            commonThreadPool.addTask(() -> {
                                extractDataService.extractData(datasetTable.getId(), null, "add_scope", null);
                            });
                        }
                    }
                }
            }
        }
        return datasetTable;
    }

    public void delete(String id) throws Exception {
        DatasetTable table = datasetTableMapper.selectByPrimaryKey(id);
        datasetTableMapper.deleteByPrimaryKey(id);
        dataSetTableFieldsService.deleteByTableId(id);
        // 删除同步任务
        dataSetTableTaskService.deleteByTableId(id);
        // 删除关联关系
        dataSetTableUnionService.deleteUnionByTableId(id);
        try {
            deleteDorisTable(id, table);
        } catch (Exception e) {

        }
    }

    private void deleteDorisTable(String datasetId, DatasetTable table) throws Exception {
        String dorisTableName = DorisTableUtils.dorisName(datasetId);
        Datasource dorisDatasource = (Datasource) CommonBeanFactory.getBean("DorisDatasource");
        JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(dorisDatasource);
        DDLProvider ddlProvider = ProviderFactory.getDDLProvider(dorisDatasource.getType());
        if (StringUtils.equalsIgnoreCase("custom", table.getType())) {
            datasourceRequest.setQuery(ddlProvider.dropView(dorisTableName));
            jdbcProvider.exec(datasourceRequest);
            datasourceRequest.setQuery(ddlProvider.dropView(DorisTableUtils.dorisTmpName(dorisTableName)));
            jdbcProvider.exec(datasourceRequest);
        } else {
            datasourceRequest.setQuery(ddlProvider.dropTable(dorisTableName));
            jdbcProvider.exec(datasourceRequest);
            datasourceRequest.setQuery(ddlProvider.dropTable(DorisTableUtils.dorisTmpName(dorisTableName)));
            jdbcProvider.exec(datasourceRequest);
        }
    }

    public List<DataSetTableDTO> list(DataSetTableRequest dataSetTableRequest) {
        dataSetTableRequest.setUserId(String.valueOf(AuthUtils.getUser().getUserId()));
        return extDataSetTableMapper.search(dataSetTableRequest);
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
                .name(Translator.get("i18n_chart_count"))
                .dataeaseName("*")
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

    public Map<String, Object> getPreviewData(DataSetTableRequest dataSetTableRequest, Integer page, Integer pageSize) throws Exception {
        Map<String, Object> map = new HashMap<>();
        DatasetTableField datasetTableField = DatasetTableField.builder().build();
        datasetTableField.setTableId(dataSetTableRequest.getId());
        datasetTableField.setChecked(Boolean.TRUE);
        List<DatasetTableField> fields = dataSetTableFieldsService.list(datasetTableField);
        String[] fieldArray = fields.stream().map(DatasetTableField::getDataeaseName).toArray(String[]::new);

        DataTableInfoDTO dataTableInfoDTO = new Gson().fromJson(dataSetTableRequest.getInfo(), DataTableInfoDTO.class);
        DatasetTable datasetTable = datasetTableMapper.selectByPrimaryKey(dataSetTableRequest.getId());

        List<String[]> data = new ArrayList<>();
        DataSetPreviewPage dataSetPreviewPage = new DataSetPreviewPage();
        dataSetPreviewPage.setShow(Integer.valueOf(dataSetTableRequest.getRow()));
        dataSetPreviewPage.setPage(page);
        dataSetPreviewPage.setPageSize(pageSize);
        int realSize = Integer.parseInt(dataSetTableRequest.getRow()) < pageSize ? Integer.parseInt(dataSetTableRequest.getRow()) : pageSize;
        if (page == Integer.parseInt(dataSetTableRequest.getRow()) / pageSize + 1) {
            realSize = Integer.parseInt(dataSetTableRequest.getRow()) % pageSize;
        }
        if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "db")) {
            Datasource ds = datasourceMapper.selectByPrimaryKey(dataSetTableRequest.getDataSourceId());
            if (ObjectUtils.isEmpty(ds)) {
                throw new RuntimeException(Translator.get("i18n_datasource_delete"));
            }
            DatasourceProvider datasourceProvider = ProviderFactory.getProvider(ds.getType());
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(ds);

            String table = dataTableInfoDTO.getTable();
            QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
            datasourceRequest.setQuery(qp.createQuerySQLWithPage(table, fields, page, pageSize, realSize));
            try {
                data.addAll(datasourceProvider.getData(datasourceRequest));
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                datasourceRequest.setQuery(qp.createQueryCountSQL(table));
                dataSetPreviewPage.setTotal(Integer.valueOf(datasourceProvider.getData(datasourceRequest).get(0)[0]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "sql")) {
            Datasource ds = datasourceMapper.selectByPrimaryKey(dataSetTableRequest.getDataSourceId());
            if (ObjectUtils.isEmpty(ds)) {
                throw new RuntimeException(Translator.get("i18n_datasource_delete"));
            }
            DatasourceProvider datasourceProvider = ProviderFactory.getProvider(ds.getType());
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(ds);

            String sql = dataTableInfoDTO.getSql();
            QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
            datasourceRequest.setQuery(qp.createQuerySQLAsTmpWithPage(sql, fields, page, pageSize, realSize));
            try {
                data.addAll(datasourceProvider.getData(datasourceRequest));
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                datasourceRequest.setQuery(qp.createQueryCountSQLAsTmp(sql));
                dataSetPreviewPage.setTotal(Integer.valueOf(datasourceProvider.getData(datasourceRequest).get(0)[0]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "excel")) {
            if (StringUtils.isEmpty(datasetTable.getSyncStatus()) || datasetTable.getSyncStatus().equalsIgnoreCase(JobStatus.Underway.name())) {
                map.put("status", "warnning");
                map.put("msg", Translator.get("i18n_processing_data"));
                dataSetPreviewPage.setTotal(0);
            } else if (datasetTable.getSyncStatus().equalsIgnoreCase(JobStatus.Error.name())) {
                List<DatasetTableTaskLog> datasetTableTaskLogs = dataSetTableTaskLogService.getByTableId(datasetTable.getId());
                map.put("status", "error");
                if (CollectionUtils.isNotEmpty(datasetTableTaskLogs)) {
                    map.put("msg", "Failed to extract data: " + datasetTableTaskLogs.get(0).getInfo());
                } else {
                    map.put("msg", "Failed to extract data.");
                }
                dataSetPreviewPage.setTotal(0);
            } else {
                Datasource ds = (Datasource) CommonBeanFactory.getBean("DorisDatasource");
                JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
                DatasourceRequest datasourceRequest = new DatasourceRequest();
                datasourceRequest.setDatasource(ds);
                String table = DorisTableUtils.dorisName(dataSetTableRequest.getId());
                QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
                datasourceRequest.setQuery(qp.createQuerySQLWithPage(table, fields, page, pageSize, realSize));
                try {
                    data.addAll(jdbcProvider.getData(datasourceRequest));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    datasourceRequest.setQuery(qp.createQueryCountSQL(table));
                    dataSetPreviewPage.setTotal(Integer.valueOf(jdbcProvider.getData(datasourceRequest).get(0)[0]));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } else if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "custom")) {
            Datasource ds = (Datasource) CommonBeanFactory.getBean("DorisDatasource");
            JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(ds);
            String table = DorisTableUtils.dorisName(dataSetTableRequest.getId());
            QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
            datasourceRequest.setQuery(qp.createQuerySQLWithPage(table, fields, page, pageSize, realSize));
            try {
                data.addAll(jdbcProvider.getData(datasourceRequest));
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                datasourceRequest.setQuery(qp.createQueryCountSQL(table));
                dataSetPreviewPage.setTotal(Integer.valueOf(jdbcProvider.getData(datasourceRequest).get(0)[0]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        List<Map<String, Object>> jsonArray = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(data)) {
            jsonArray = data.stream().map(ele -> {
                Map<String, Object> tmpMap = new HashMap<>();
                for (int i = 0; i < ele.length; i++) {
                    tmpMap.put(fieldArray[i], ele[i]);
                }
                return tmpMap;
            }).collect(Collectors.toList());
        }

        if (!map.containsKey("status")) {
            map.put("status", "success");
        }
        map.put("fields", fields);
        map.put("data", jsonArray);
        map.put("page", dataSetPreviewPage);

        return map;
    }

    public Map<String, Object> getSQLPreview(DataSetTableRequest dataSetTableRequest) throws Exception {
        Datasource ds = datasourceMapper.selectByPrimaryKey(dataSetTableRequest.getDataSourceId());
        DatasourceProvider datasourceProvider = ProviderFactory.getProvider(ds.getType());
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(ds);
        String sql = new Gson().fromJson(dataSetTableRequest.getInfo(), DataTableInfoDTO.class).getSql();
        // 使用输入的sql先预执行一次,并拿到所有字段
        if (StringUtils.isEmpty(sql)) {
            throw new Exception(Translator.get("i18n_sql_not_empty"));
        }
        datasourceRequest.setQuery(sql);
        List<TableFiled> previewFields = datasourceProvider.fetchResultField(datasourceRequest);
        // 正式执行
        QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
        datasourceRequest.setQuery(qp.createSQLPreview(sql, previewFields.get(0).getFieldName()));
        Map<String, List> result = datasourceProvider.fetchResultAndField(datasourceRequest);
        List<String[]> data = result.get("dataList");
        List<TableFiled> fields = result.get("fieldList");
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

    public Map<String, Object> getCustomPreview(DataSetTableRequest dataSetTableRequest) throws Exception {
        Datasource ds = (Datasource) CommonBeanFactory.getBean("DorisDatasource");
        JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(ds);
//        String table = DorisTableUtils.dorisName(dataSetTableRequest.getId());

        DataTableInfoDTO dataTableInfoDTO = new Gson().fromJson(dataSetTableRequest.getInfo(), DataTableInfoDTO.class);
        List<DataSetTableUnionDTO> list = dataSetTableUnionService.listByTableId(dataTableInfoDTO.getList().get(0).getTableId());
        String sql = getCustomSQL(dataTableInfoDTO, list);
        // 使用输入的sql先预执行一次,并拿到所有字段
        datasourceRequest.setQuery(sql);
        List<TableFiled> previewFields = jdbcProvider.fetchResultField(datasourceRequest);

        QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
        datasourceRequest.setQuery(qp.createSQLPreview(sql, previewFields.get(0).getFieldName()));
        Map<String, List> result = jdbcProvider.fetchResultAndField(datasourceRequest);
        List<String[]> data = result.get("dataList");
        List<TableFiled> fields = result.get("fieldList");
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

        // 获取每个字段在当前de数据库中的name，作为sql查询后的remarks返回前端展示
        List<DatasetTableField> checkedFieldList = new ArrayList<>();
        dataTableInfoDTO.getList().forEach(ele -> {
            checkedFieldList.addAll(dataSetTableFieldsService.getListByIds(ele.getCheckedFields()));
        });
        for (DatasetTableField datasetTableField : checkedFieldList) {
            for (TableFiled tableFiled : fields) {
                if (StringUtils.equalsIgnoreCase(tableFiled.getFieldName(), DorisTableUtils.dorisFieldName(datasetTableField.getTableId() + "_" + datasetTableField.getDataeaseName()))) {
                    tableFiled.setRemarks(datasetTableField.getName());
                    break;
                }
            }
        }

        Map<String, Object> map = new HashMap<>();
        map.put("fields", fields);
        map.put("data", jsonArray);

        return map;
    }

    // 自助数据集从doris里预览数据
    private String getCustomSQL(DataTableInfoDTO dataTableInfoDTO, List<DataSetTableUnionDTO> list) {
        Map<String, String[]> customInfo = new TreeMap<>();
        dataTableInfoDTO.getList().forEach(ele -> {
            String table = DorisTableUtils.dorisName(ele.getTableId());
            List<DatasetTableField> fields = dataSetTableFieldsService.getListByIds(ele.getCheckedFields());
            String[] array = fields.stream().map(f -> table + "." + f.getDataeaseName() + " AS " + DorisTableUtils.dorisFieldName(ele.getTableId() + "_" + f.getDataeaseName())).toArray(String[]::new);
            customInfo.put(table, array);
        });
        DataTableInfoCustomUnion first = dataTableInfoDTO.getList().get(0);
        if (CollectionUtils.isNotEmpty(list)) {
            StringBuilder field = new StringBuilder();
            Iterator<Map.Entry<String, String[]>> iterator = customInfo.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String[]> next = iterator.next();
                field.append(StringUtils.join(next.getValue(), ",")).append(",");
            }
            String f = field.substring(0, field.length() - 1);

            StringBuilder join = new StringBuilder();
            for (DataTableInfoCustomUnion dataTableInfoCustomUnion : dataTableInfoDTO.getList()) {
                for (DataSetTableUnionDTO dto : list) {
                    // 被关联表和自助数据集的表相等
                    if (StringUtils.equals(dto.getTargetTableId(), dataTableInfoCustomUnion.getTableId())) {
                        DatasetTableField sourceField = dataSetTableFieldsService.get(dto.getSourceTableFieldId());
                        DatasetTableField targetField = dataSetTableFieldsService.get(dto.getTargetTableFieldId());
                        join.append(convertUnionTypeToSQL(dto.getSourceUnionRelation()))
                                .append(DorisTableUtils.dorisName(dto.getTargetTableId()))
                                .append(" ON ")
                                .append(DorisTableUtils.dorisName(dto.getSourceTableId())).append(".").append(sourceField.getDataeaseName())
                                .append(" = ")
                                .append(DorisTableUtils.dorisName(dto.getTargetTableId())).append(".").append(targetField.getDataeaseName());
                    }
                }
            }
            return MessageFormat.format("SELECT {0} FROM {1}", f, DorisTableUtils.dorisName(first.getTableId())) + join.toString();
        } else {
            return MessageFormat.format("SELECT {0} FROM {1}", StringUtils.join(customInfo.get(DorisTableUtils.dorisName(first.getTableId())), ","), DorisTableUtils.dorisName(first.getTableId()));
        }
    }

    private String convertUnionTypeToSQL(String unionType) {
        switch (unionType) {
            case "1:1":
                return " INNER JOIN ";
            case "1:N":
                return " LEFT JOIN ";
            case "N:1":
                return " RIGHT JOIN ";
            default:
                return " INNER JOIN ";
        }
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
            fields = datasourceProvider.fetchResultField(datasourceRequest);
        } else if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "excel")) {
            DataTableInfoDTO dataTableInfoDTO = new Gson().fromJson(dataSetTableRequest.getInfo(), DataTableInfoDTO.class);
            String path = dataTableInfoDTO.getData();
            File file = new File(path);
            Map<String, Object> map = parseExcel(path.substring(path.lastIndexOf("/") + 1), new FileInputStream(file), false);
            fields = (List<TableFiled>) map.get("fields");
        } else if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "custom")) {
            // save field
            DataTableInfoDTO dataTableInfoDTO = new Gson().fromJson(dataSetTableRequest.getInfo(), DataTableInfoDTO.class);
            List<DataTableInfoCustomUnion> list = dataTableInfoDTO.getList();
            List<DatasetTableField> fieldList = new ArrayList<>();
            list.forEach(ele -> {
                List<DatasetTableField> listByIds = dataSetTableFieldsService.getListByIds(ele.getCheckedFields());
                listByIds.forEach(f -> {
                    f.setDataeaseName(DorisTableUtils.dorisFieldName(ele.getTableId() + "_" + f.getDataeaseName()));
                });
                fieldList.addAll(listByIds);
            });
            for (int i = 0; i < fieldList.size(); i++) {
                DatasetTableField datasetTableField = fieldList.get(i);
                datasetTableField.setId(null);
                datasetTableField.setTableId(datasetTable.getId());
                datasetTableField.setColumnIndex(i);
            }
            dataSetTableFieldsService.batchEdit(fieldList);
            // custom 创建doris视图
            createDorisView(DorisTableUtils.dorisName(datasetTable.getId()), getCustomSQL(dataTableInfoDTO, dataSetTableUnionService.listByTableId(dataTableInfoDTO.getList().get(0).getTableId())));
            return;
        }
        QueryProvider qp = null;
        if (!ObjectUtils.isEmpty(ds)) {
            qp = ProviderFactory.getQueryProvider(ds.getType());
        }
        if (CollectionUtils.isNotEmpty(fields)) {
            for (int i = 0; i < fields.size(); i++) {
                TableFiled filed = fields.get(i);
                DatasetTableField datasetTableField = DatasetTableField.builder().build();
                datasetTableField.setTableId(datasetTable.getId());
                datasetTableField.setOriginName(filed.getFieldName());
                datasetTableField.setName(filed.getRemarks());
                datasetTableField.setDataeaseName(DorisTableUtils.columnName(filed.getFieldName()));
                datasetTableField.setType(filed.getFieldType());
                if (ObjectUtils.isEmpty(ds)) {
                    datasetTableField.setDeType(transFieldType(filed.getFieldType()));
                    datasetTableField.setDeExtractType(transFieldType(filed.getFieldType()));
                } else {
                    Integer fieldType = qp.transFieldType(filed.getFieldType());
                    datasetTableField.setDeType(fieldType == 4 ? 2 : fieldType);
                    datasetTableField.setDeExtractType(fieldType);
                }
                datasetTableField.setSize(filed.getFieldSize());
                datasetTableField.setChecked(true);
                datasetTableField.setColumnIndex(i);
                datasetTableField.setLastSyncTime(syncTime);
                dataSetTableFieldsService.save(datasetTableField);
            }
        }
    }

    private void createDorisView(String dorisTableName, String customSql) throws Exception {
        Datasource dorisDatasource = (Datasource) CommonBeanFactory.getBean("DorisDatasource");
        JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(dorisDatasource);
        DDLProvider ddlProvider = ProviderFactory.getDDLProvider(dorisDatasource.getType());
        // 先删除表
        datasourceRequest.setQuery(ddlProvider.dropView(dorisTableName));
        jdbcProvider.exec(datasourceRequest);
        datasourceRequest.setQuery(ddlProvider.createView(dorisTableName, customSql));
        jdbcProvider.exec(datasourceRequest);
    }

    public Integer transFieldType(String field) {
        switch (field) {
            case "TEXT":
                return 0;
            case "DATETIME":
                return 1;
            case "LONG":
            case "INT":
                return 2;
            case "DOUBLE":
                return 3;
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
        if (datasetTableIncrementalConfig == null || StringUtils.isEmpty(datasetTableIncrementalConfig.getTableId())) {
            return;
        }
        if (StringUtils.isEmpty(datasetTableIncrementalConfig.getId())) {
            datasetTableIncrementalConfig.setId(UUID.randomUUID().toString());
            datasetTableIncrementalConfigMapper.insertSelective(datasetTableIncrementalConfig);
        } else {
            datasetTableIncrementalConfigMapper.updateByPrimaryKey(datasetTableIncrementalConfig);
        }
    }

    private void checkName(DatasetTable datasetTable) {
//        if (StringUtils.isEmpty(datasetTable.getId()) && StringUtils.equalsIgnoreCase("db", datasetTable.getType())) {
//            return;
//        }
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
            throw new RuntimeException(Translator.get("i18n_name_cant_repeat_same_group"));
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
        List<String> sheets = new ArrayList<>();

        if (StringUtils.equalsIgnoreCase(suffix, "xls")) {
            HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
            HSSFSheet sheet0 = workbook.getSheetAt(0);
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                sheets.add(workbook.getSheetAt(i).getSheetName());
            }
            if (sheet0.getNumMergedRegions() > 0) {
                throw new RuntimeException(Translator.get("i18n_excel_have_merge_region"));
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
                        tableFiled.setFieldType("TEXT");
                        tableFiled.setFieldSize(1024);
                        String columnName = readCell(row.getCell(j), false, null);
                        if (StringUtils.isEmpty(columnName)) {
                            columnName = "NONE_" + String.valueOf(j);
                        }
                        tableFiled.setFieldName(columnName);
                        tableFiled.setRemarks(columnName);
                        fields.add(tableFiled);
                    } else if (i == 1) {
                        r[j] = readCell(row.getCell(j), true, fields.get(j));
                    } else {
                        r[j] = readCell(row.getCell(j), false, null);
                    }
                }
                if (i > 0) {
                    data.add(r);
                }
            }
        } else if (StringUtils.equalsIgnoreCase(suffix, "xlsx")) {
            XSSFWorkbook xssfWorkbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet0 = xssfWorkbook.getSheetAt(0);
            for (int i = 0; i < xssfWorkbook.getNumberOfSheets(); i++) {
                sheets.add(xssfWorkbook.getSheetAt(i).getSheetName());
            }
            if (sheet0.getNumMergedRegions() > 0) {
                throw new RuntimeException(Translator.get("i18n_excel_have_merge_region"));
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
                        tableFiled.setFieldType("TEXT");
                        tableFiled.setFieldSize(1024);
                        String columnName = readCell(row.getCell(j), false, null);
                        if (StringUtils.isEmpty(columnName)) {
                            columnName = "NONE_" + String.valueOf(j);
                        }
                        tableFiled.setFieldName(columnName);
                        tableFiled.setRemarks(columnName);
                        fields.add(tableFiled);
                    } else if (i == 1) {
                        r[j] = readCell(row.getCell(j), true, fields.get(j));
                    } else {
                        r[j] = readCell(row.getCell(j), false, null);
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
        map.put("sheets", sheets);
        return map;
    }

    private String readCell(Cell cell, boolean cellType, TableFiled tableFiled) {
        CellType cellTypeEnum = cell.getCellTypeEnum();
        if (cellTypeEnum.equals(CellType.STRING)) {
            if (cellType) {
                tableFiled.setFieldType("TEXT");
                tableFiled.setFieldSize(65533);
            }
            return cell.getStringCellValue();
        }
        if (cellTypeEnum.equals(CellType.NUMERIC)) {
            if (HSSFDateUtil.isCellDateFormatted(cell)) {
                if (cellType) {
                    tableFiled.setFieldType("DATETIME");
                }
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    return sdf.format(cell.getDateCellValue());
                } catch (Exception e) {
                    return "";
                }
            } else {
                double d = cell.getNumericCellValue();
                try {
                    Double value = new Double(d);
                    double eps = 1e-10;
                    if (value - Math.floor(value) < eps) {
                        if (cellType) {
                            tableFiled.setFieldType("LONG");
                        }
                        return value.longValue() + "";
                    } else {
                        if (cellType) {
                            tableFiled.setFieldType("DOUBLE");
                        }
                        NumberFormat nf = NumberFormat.getInstance();
                        nf.setGroupingUsed(false);
                        return nf.format(value);
                    }
                } catch (Exception e) {
                    BigDecimal b = new BigDecimal(d);
                    return b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() + "";
                }
            }
        }
        if (cellTypeEnum.equals(CellType.BOOLEAN)) {
            return cell.getBooleanCellValue() ? "1" : "0";
        }
        return "";
    }

    private String saveFile(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename();
        String suffix = filename.substring(filename.lastIndexOf(".") + 1);
        filename = Md5Utils.md5(filename.substring(0, filename.length() - suffix.length()));
        String dirPath = path + AuthUtils.getUser().getUsername() + "/";
        File p = new File(dirPath);
        if (!p.exists()) {
            p.mkdirs();
        }
        String filePath = dirPath + filename + "." + suffix;
        File f = new File(filePath);
        FileOutputStream fileOutputStream = new FileOutputStream(f);
        fileOutputStream.write(file.getBytes());
        fileOutputStream.flush();
        fileOutputStream.close();
        return filePath;
    }

    public Boolean checkDorisTableIsExists(String id) throws Exception {
        Datasource dorisDatasource = (Datasource) CommonBeanFactory.getBean("DorisDatasource");
        JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(dorisDatasource);
        QueryProvider qp = ProviderFactory.getQueryProvider(dorisDatasource.getType());
        datasourceRequest.setQuery(qp.searchTable(DorisTableUtils.dorisName(id)));
        List<String[]> data = jdbcProvider.getData(datasourceRequest);
        return CollectionUtils.isNotEmpty(data);
    }

    @Resource
    private UtilMapper utilMapper;

    @QuartzScheduled(cron = "0 0/3 * * * ?")
    public void updateDatasetTableStatus() {
        List<QrtzSchedulerState> qrtzSchedulerStates = qrtzSchedulerStateMapper.selectByExample(null);
        List<String> activeQrtzInstances = qrtzSchedulerStates.stream().filter(qrtzSchedulerState -> qrtzSchedulerState.getLastCheckinTime() + qrtzSchedulerState.getCheckinInterval() + 1000 > utilMapper.currentTimestamp()).map(QrtzSchedulerStateKey::getInstanceName).collect(Collectors.toList());
        List<DatasetTable> jobStoppeddDatasetTables = new ArrayList<>();
        DatasetTableExample example = new DatasetTableExample();
        example.createCriteria().andSyncStatusEqualTo(JobStatus.Underway.name());

        datasetTableMapper.selectByExample(example).forEach(datasetTable -> {
            if (StringUtils.isEmpty(datasetTable.getQrtzInstance()) || !activeQrtzInstances.contains(datasetTable.getQrtzInstance().substring(0, datasetTable.getQrtzInstance().length() - 13))) {
                jobStoppeddDatasetTables.add(datasetTable);
            }
        });

        if (CollectionUtils.isEmpty(jobStoppeddDatasetTables)) {
            return;
        }

        DatasetTable record = new DatasetTable();
        record.setSyncStatus(JobStatus.Error.name());
        example.clear();
        example.createCriteria().andSyncStatusEqualTo(JobStatus.Underway.name()).andIdIn(jobStoppeddDatasetTables.stream().map(DatasetTable::getId).collect(Collectors.toList()));
        datasetTableMapper.updateByExampleSelective(record, example);

        DatasetTableTaskLog datasetTableTaskLog = new DatasetTableTaskLog();
        datasetTableTaskLog.setStatus(JobStatus.Error.name());
        datasetTableTaskLog.setInfo("Job stopped due to system error.");

        DatasetTableTaskLogExample datasetTableTaskLogExample = new DatasetTableTaskLogExample();
        datasetTableTaskLogExample.createCriteria().andStatusEqualTo(JobStatus.Underway.name()).andTableIdIn(jobStoppeddDatasetTables.stream().map(DatasetTable::getId).collect(Collectors.toList()));
        datasetTableTaskLogMapper.updateByExampleSelective(datasetTableTaskLog, datasetTableTaskLogExample);

    }
}
