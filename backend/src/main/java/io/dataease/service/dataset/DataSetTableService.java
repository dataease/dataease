package io.dataease.service.dataset;


import com.google.gson.Gson;
import io.dataease.base.domain.*;
import io.dataease.base.mapper.*;
import io.dataease.base.mapper.ext.ExtDataSetGroupMapper;
import io.dataease.base.mapper.ext.ExtDataSetTableMapper;
import io.dataease.base.mapper.ext.UtilMapper;
import io.dataease.commons.constants.DatasourceTypes;
import io.dataease.commons.constants.JobStatus;
import io.dataease.commons.constants.ScheduleType;
import io.dataease.commons.constants.TaskStatus;
import io.dataease.commons.exception.DEException;
import io.dataease.commons.utils.*;
import io.dataease.controller.request.dataset.DataSetGroupRequest;
import io.dataease.controller.request.dataset.DataSetTableRequest;
import io.dataease.controller.request.dataset.DataSetTaskRequest;
import io.dataease.controller.request.datasource.DatasourceRequest;
import io.dataease.controller.response.DataSetDetail;
import io.dataease.dto.dataset.*;
import io.dataease.dto.datasource.TableFiled;
import io.dataease.exception.DataEaseException;
import io.dataease.i18n.Translator;
import io.dataease.plugins.loader.ClassloaderResponsity;
import io.dataease.provider.ProviderFactory;
import io.dataease.provider.datasource.DatasourceProvider;
import io.dataease.provider.datasource.JdbcProvider;
import io.dataease.provider.query.DDLProvider;
import io.dataease.provider.query.QueryProvider;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private QrtzSchedulerStateMapper qrtzSchedulerStateMapper;
    @Resource
    private DatasetTableTaskLogMapper datasetTableTaskLogMapper;
    @Resource
    private ExtDataSetGroupMapper extDataSetGroupMapper;
    @Resource
    private DatasetTableFieldMapper datasetTableFieldMapper;
    private static final String lastUpdateTime = "${__last_update_time__}";
    private static final String currentUpdateTime = "${__current_update_time__}";

    @Value("${upload.file.path}")
    private String path;

    private static Logger logger = LoggerFactory.getLogger(ClassloaderResponsity.class);

    public void batchInsert(List<DataSetTableRequest> datasetTable) throws Exception {
        for (DataSetTableRequest table : datasetTable) {
            save(table);
        }
    }

    private void extractData(DataSetTableRequest datasetTable) throws Exception {
        if (datasetTable.getMode() == 1 && StringUtils.isNotEmpty(datasetTable.getSyncType()) && datasetTable.getSyncType().equalsIgnoreCase("sync_now")) {
            DataSetTaskRequest dataSetTaskRequest = new DataSetTaskRequest();
            DatasetTableTask datasetTableTask = new DatasetTableTask();
            datasetTableTask.setTableId(datasetTable.getId());
            datasetTableTask.setRate(ScheduleType.SIMPLE.toString());
            datasetTableTask.setType("all_scope");
            datasetTableTask.setName(datasetTable.getName() + " 更新设置");
            datasetTableTask.setEnd("0");
            datasetTableTask.setStatus(TaskStatus.Underway.name());
            datasetTableTask.setStartTime(System.currentTimeMillis());
            dataSetTaskRequest.setDatasetTableTask(datasetTableTask);
            dataSetTableTaskService.save(dataSetTaskRequest);
        }
    }

    public void saveExcel(DataSetTableRequest datasetTable) throws Exception {
        if (StringUtils.isEmpty(datasetTable.getId())) {
            if (datasetTable.isMergeSheet()) {
                Map<String, List<ExcelSheetData>> map = datasetTable.getSheets().stream().collect(Collectors.groupingBy(ExcelSheetData::getFieldsMd5));
                for (String s : map.keySet()) {
                    DataSetTableRequest sheetTable = new DataSetTableRequest();
                    BeanUtils.copyBean(sheetTable, datasetTable);
                    sheetTable.setId(UUID.randomUUID().toString());
                    sheetTable.setCreateBy(AuthUtils.getUser().getUsername());
                    sheetTable.setCreateTime(System.currentTimeMillis());
                    List<ExcelSheetData> excelSheetDataList = map.get(s);
                    sheetTable.setName(excelSheetDataList.get(0).getDatasetName());
                    checkName(sheetTable);
                    excelSheetDataList.forEach(excelSheetData -> {
                        String[] fieldArray = excelSheetData.getFields().stream().map(TableFiled::getFieldName).toArray(String[]::new);
                        if (checkIsRepeat(fieldArray)) {
                            DataEaseException.throwException(Translator.get("i18n_excel_field_repeat"));
                        }
                        excelSheetData.setData(null);
                        excelSheetData.setJsonArray(null);
                    });
                    DataTableInfoDTO info = new DataTableInfoDTO();
                    info.setExcelSheetDataList(excelSheetDataList);
                    sheetTable.setInfo(new Gson().toJson(info));
                    int insert = datasetTableMapper.insert(sheetTable);
                    if (insert == 1) {
                        saveExcelTableField(sheetTable.getId(), excelSheetDataList.get(0).getFields(), true);
                        commonThreadPool.addTask(() -> extractDataService.extractExcelData(sheetTable.getId(), "all_scope", "初始导入", null));
                    }
                }
            } else {
                for (ExcelSheetData sheet : datasetTable.getSheets()) {
                    String[] fieldArray = sheet.getFields().stream().map(TableFiled::getFieldName).toArray(String[]::new);
                    if (checkIsRepeat(fieldArray)) {
                        DataEaseException.throwException(Translator.get("i18n_excel_field_repeat"));
                    }
                    DataSetTableRequest sheetTable = new DataSetTableRequest();
                    BeanUtils.copyBean(sheetTable, datasetTable);
                    sheetTable.setId(UUID.randomUUID().toString());
                    sheetTable.setCreateBy(AuthUtils.getUser().getUsername());
                    sheetTable.setCreateTime(System.currentTimeMillis());
                    sheetTable.setName(sheet.getDatasetName());
                    checkName(sheetTable);
                    sheet.setData(null);
                    sheet.setJsonArray(null);
                    List<ExcelSheetData> excelSheetDataList = new ArrayList<>();
                    excelSheetDataList.add(sheet);
                    DataTableInfoDTO info = new DataTableInfoDTO();
                    info.setExcelSheetDataList(excelSheetDataList);
                    sheetTable.setInfo(new Gson().toJson(info));
                    int insert = datasetTableMapper.insert(sheetTable);
                    if (insert == 1) {
                        saveExcelTableField(sheetTable.getId(), sheet.getFields(), true);
                        commonThreadPool.addTask(() -> extractDataService.extractExcelData(sheetTable.getId(), "all_scope", "初始导入", null));
                    }
                }
            }
            return;
        }

        List<ExcelSheetData> excelSheetDataList = new ArrayList<>();
        List<String> oldFields = datasetTable.getSheets().get(0).getFields().stream().map(TableFiled::getRemarks).collect(Collectors.toList());
        for (ExcelSheetData sheet : datasetTable.getSheets()) {
            //替换时，
            if (datasetTable.getEditType() == 0) {
                List<String> newFields = sheet.getFields().stream().map(TableFiled::getRemarks).collect(Collectors.toList());
                if (!oldFields.equals(newFields)) {
                    DataEaseException.throwException(Translator.get("i18n_excel_column_inconsistent"));
                }
                oldFields = newFields;
            }

            String[] fieldArray = sheet.getFields().stream().map(TableFiled::getFieldName).toArray(String[]::new);
            if (checkIsRepeat(fieldArray)) {
                DataEaseException.throwException(Translator.get("i18n_excel_field_repeat"));
            }
            sheet.setData(null);
            sheet.setJsonArray(null);
            excelSheetDataList.add(sheet);
        }
        DataTableInfoDTO info = new DataTableInfoDTO();
        info.setExcelSheetDataList(excelSheetDataList);
        datasetTable.setInfo(new Gson().toJson(info));
        int update = datasetTableMapper.updateByPrimaryKeySelective(datasetTable);
        // 替換時，先不刪除旧字段；同步成功后再删除

        if (update == 1) {
            if (datasetTable.getEditType() == 0) {
                commonThreadPool.addTask(() -> extractDataService.extractExcelData(datasetTable.getId(), "all_scope", "替换", saveExcelTableField(datasetTable.getId(), datasetTable.getSheets().get(0).getFields(), false)));
            } else if (datasetTable.getEditType() == 1) {
                commonThreadPool.addTask(() -> extractDataService.extractExcelData(datasetTable.getId(), "add_scope", "追加", null));
            }
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
            int insert = datasetTableMapper.insert(datasetTable);
            // 添加表成功后，获取当前表字段和类型，抽象到dataease数据库
            if (insert == 1) {
                saveTableField(datasetTable);
                extractData(datasetTable);
            }
        } else {
            int update = datasetTableMapper.updateByPrimaryKeySelective(datasetTable);
            if (datasetTable.getIsRename() == null || !datasetTable.getIsRename()) {
                // 更新数据和字段
                if (update == 1) {
                    if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "sql") || StringUtils.equalsIgnoreCase(datasetTable.getType(), "custom")) {
                        saveTableField(datasetTable);
                    }
                }
            }
        }
        return datasetTable;
    }

    public void alter(DataSetTableRequest request) throws Exception {
        checkName(request);
        datasetTableMapper.updateByPrimaryKeySelective(request);
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
            // 抽取的数据集删除doris
            if (table.getMode() == 1) {
                deleteDorisTable(id, table);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        dataSetTableRequest.setTypeFilter(dataSetTableRequest.getTypeFilter());
        return extDataSetTableMapper.search(dataSetTableRequest);
    }

    public List<DataSetTableDTO> listAndGroup(DataSetTableRequest dataSetTableRequest) {
        dataSetTableRequest.setUserId(String.valueOf(AuthUtils.getUser().getUserId()));
        dataSetTableRequest.setTypeFilter(dataSetTableRequest.getTypeFilter());
        List<DataSetTableDTO> ds = extDataSetTableMapper.search(dataSetTableRequest);
        ds.forEach(ele -> ele.setIsLeaf(true));
        // 获取group下的子group
        DataSetGroupRequest datasetGroup = new DataSetGroupRequest();
        datasetGroup.setLevel(null);
        datasetGroup.setType("group");
        datasetGroup.setPid(dataSetTableRequest.getSceneId());
        datasetGroup.setUserId(String.valueOf(AuthUtils.getUser().getUserId()));
        datasetGroup.setSort("name asc,create_time desc");
        List<DataSetGroupDTO> groups = extDataSetGroupMapper.search(datasetGroup);
        List<DataSetTableDTO> group = groups.stream().map(ele -> {
            DataSetTableDTO dto = new DataSetTableDTO();
            BeanUtils.copyBean(dto, ele);
            dto.setIsLeaf(false);
            dto.setType("group");
            return dto;
        }).collect(Collectors.toList());
        group.addAll(ds);
        return group;
    }

    public List<DataSetTableDTO> search(DataSetTableRequest dataSetTableRequest) {
        String userId = String.valueOf(AuthUtils.getUser().getUserId());
        dataSetTableRequest.setUserId(userId);
        dataSetTableRequest.setSort("name asc");
        List<DataSetTableDTO> ds = extDataSetTableMapper.search(dataSetTableRequest);
        if (CollectionUtils.isEmpty(ds)) {
            return ds;
        }

        TreeSet<String> ids = new TreeSet<>();
        ds.forEach(ele -> {
            ele.setIsLeaf(true);
            ele.setPid(ele.getSceneId());
            ids.add(ele.getPid());
        });

        List<DataSetTableDTO> group = new ArrayList<>();
        DataSetGroupRequest dataSetGroupRequest = new DataSetGroupRequest();
        dataSetGroupRequest.setUserId(userId);
        dataSetGroupRequest.setIds(ids);
        List<DataSetGroupDTO> search = extDataSetGroupMapper.search(dataSetGroupRequest);
        while (CollectionUtils.isNotEmpty(search)) {
            ids.clear();
            search.forEach(ele -> {
                DataSetTableDTO dto = new DataSetTableDTO();
                BeanUtils.copyBean(dto, ele);
                dto.setIsLeaf(false);
                dto.setType("group");
                group.add(dto);
                ids.add(ele.getPid());
            });
            dataSetGroupRequest.setIds(ids);
            search = extDataSetGroupMapper.search(dataSetGroupRequest);
        }

        List<DataSetTableDTO> res = new ArrayList<>();
        Map<String, DataSetTableDTO> map = new TreeMap<>();
        group.forEach(ele -> map.put(ele.getId(), ele));
        Iterator<Map.Entry<String, DataSetTableDTO>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            res.add(iterator.next().getValue());
        }
        res.sort(Comparator.comparing(DatasetTable::getName));
        res.addAll(ds);
        return res;
    }

    public DatasetTable get(String id) {
        return datasetTableMapper.selectByPrimaryKey(id);
    }

    public DataSetTableDTO getWithPermission(String id) {
        DataSetTableRequest dataSetTableRequest = new DataSetTableRequest();
        dataSetTableRequest.setId(id);
        dataSetTableRequest.setUserId(String.valueOf(AuthUtils.getUser().getUserId()));
        dataSetTableRequest.setTypeFilter(dataSetTableRequest.getTypeFilter());
        return extDataSetTableMapper.searchOne(dataSetTableRequest);
    }

    public List<TableFiled> getFields(DatasetTable datasetTable) throws Exception {
        Datasource ds = datasourceMapper.selectByPrimaryKey(datasetTable.getDataSourceId());
        DatasourceProvider datasourceProvider = ProviderFactory.getProvider(ds.getType());
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(ds);
        datasourceRequest.setTable(new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class).getTable());
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
            if (StringUtils.equalsIgnoreCase("q", field.getGroupType())) {
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
                .extField(1)
                .groupType("q")
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
        if (CollectionUtils.isEmpty(fields)) {
            map.put("fields", fields);
            map.put("data", new ArrayList<>());
            map.put("page", new DataSetPreviewPage());
            return map;
        }
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
            if (datasetTable.getMode() == 0) {
                Datasource ds = datasourceMapper.selectByPrimaryKey(dataSetTableRequest.getDataSourceId());
                if (ObjectUtils.isEmpty(ds)) {
                    throw new RuntimeException(Translator.get("i18n_datasource_delete"));
                }
                DatasourceProvider datasourceProvider = ProviderFactory.getProvider(ds.getType());
                DatasourceRequest datasourceRequest = new DatasourceRequest();
                datasourceRequest.setDatasource(ds);
                String table = dataTableInfoDTO.getTable();
                QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
                datasourceRequest.setQuery(qp.createQueryTableWithPage(table, fields, page, pageSize, realSize, false, ds));
                map.put("sql", datasourceRequest.getQuery());
                datasourceRequest.setPage(page);
                datasourceRequest.setFetchSize(Integer.parseInt(dataSetTableRequest.getRow()));
                datasourceRequest.setPageSize(pageSize);
                datasourceRequest.setRealSize(realSize);
                datasourceRequest.setPreviewData(true);
                try {
                    datasourceRequest.setPageable(true);
                    data.addAll(datasourceProvider.getData(datasourceRequest));
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    DEException.throwException(Translator.get("i18n_ds_error"));
                }

                try {
                    datasourceRequest.setQuery(qp.createQueryTableWithLimit(table, fields, Integer.valueOf(dataSetTableRequest.getRow()), false, ds));
                    datasourceRequest.setPageable(false);
                    dataSetPreviewPage.setTotal(datasourceProvider.getData(datasourceRequest).size());
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    DEException.throwException(Translator.get("i18n_ds_error"));
                }
            } else {
                // check doris table
                if (!checkDorisTableIsExists(dataSetTableRequest.getId())) {
                    throw new RuntimeException(Translator.get("i18n_data_not_sync"));
                }
                Datasource ds = (Datasource) CommonBeanFactory.getBean("DorisDatasource");
                JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
                DatasourceRequest datasourceRequest = new DatasourceRequest();
                datasourceRequest.setDatasource(ds);
                String table = DorisTableUtils.dorisName(dataSetTableRequest.getId());
                QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
                datasourceRequest.setQuery(qp.createQueryTableWithPage(table, fields, page, pageSize, realSize, false, ds));
                map.put("sql", datasourceRequest.getQuery());
                try {
                    data.addAll(jdbcProvider.getData(datasourceRequest));
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    DEException.throwException(Translator.get("i18n_ds_error"));
                }
                try {
                    datasourceRequest.setQuery(qp.createQueryTableWithLimit(table, fields, Integer.valueOf(dataSetTableRequest.getRow()), false, ds));
                    dataSetPreviewPage.setTotal(jdbcProvider.getData(datasourceRequest).size());
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    DEException.throwException(Translator.get("i18n_ds_error"));
                }
            }

        } else if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "sql")) {
            if (datasetTable.getMode() == 0) {
                Datasource ds = datasourceMapper.selectByPrimaryKey(dataSetTableRequest.getDataSourceId());
                if (ObjectUtils.isEmpty(ds)) {
                    throw new RuntimeException(Translator.get("i18n_datasource_delete"));
                }
                DatasourceProvider datasourceProvider = ProviderFactory.getProvider(ds.getType());
                DatasourceRequest datasourceRequest = new DatasourceRequest();
                datasourceRequest.setDatasource(ds);

                String sql = dataTableInfoDTO.getSql();
                QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
                datasourceRequest.setQuery(qp.createQuerySQLWithPage(sql, fields, page, pageSize, realSize, false));
                map.put("sql", datasourceRequest.getQuery());
                datasourceRequest.setPage(page);
                datasourceRequest.setFetchSize(Integer.parseInt(dataSetTableRequest.getRow()));
                datasourceRequest.setPageSize(pageSize);
                datasourceRequest.setRealSize(realSize);
                datasourceRequest.setPreviewData(true);
                try {
                    datasourceRequest.setPageable(true);
                    data.addAll(datasourceProvider.getData(datasourceRequest));
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    DEException.throwException(Translator.get("i18n_ds_error"));
                }
                try {
                    datasourceRequest.setPageable(false);
                    datasourceRequest.setQuery(qp.createQuerySqlWithLimit(sql, fields, Integer.valueOf(dataSetTableRequest.getRow()), false));
                    dataSetPreviewPage.setTotal(datasourceProvider.getData(datasourceRequest).size());
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    DEException.throwException(Translator.get("i18n_ds_error"));
                }
            } else {
                // check doris table
                if (!checkDorisTableIsExists(dataSetTableRequest.getId())) {
                    throw new RuntimeException(Translator.get("i18n_data_not_sync"));
                }
                Datasource ds = (Datasource) CommonBeanFactory.getBean("DorisDatasource");
                JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
                DatasourceRequest datasourceRequest = new DatasourceRequest();
                datasourceRequest.setDatasource(ds);
                String table = DorisTableUtils.dorisName(dataSetTableRequest.getId());
                QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
                datasourceRequest.setQuery(qp.createQueryTableWithPage(table, fields, page, pageSize, realSize, false, ds));
                map.put("sql", datasourceRequest.getQuery());
                try {
                    data.addAll(jdbcProvider.getData(datasourceRequest));
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    DEException.throwException(Translator.get("i18n_ds_error"));
                }
                try {
                    datasourceRequest.setQuery(qp.createQueryTableWithLimit(table, fields, Integer.valueOf(dataSetTableRequest.getRow()), false, ds));
                    dataSetPreviewPage.setTotal(jdbcProvider.getData(datasourceRequest).size());
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    DEException.throwException(Translator.get("i18n_ds_error"));
                }
            }
        } else if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "excel")) {
            if (!checkDorisTableIsExists(dataSetTableRequest.getId())) {
                throw new RuntimeException(Translator.get("i18n_data_not_sync"));
            }

            Datasource ds = (Datasource) CommonBeanFactory.getBean("DorisDatasource");
            JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(ds);
            String table = DorisTableUtils.dorisName(dataSetTableRequest.getId());
            QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
            datasourceRequest.setQuery(qp.createQueryTableWithPage(table, fields, page, pageSize, realSize, false, ds));
            map.put("sql", datasourceRequest.getQuery());
            try {
                data.addAll(jdbcProvider.getData(datasourceRequest));
            } catch (Exception e) {
                logger.error(e.getMessage());
                DEException.throwException(Translator.get("i18n_ds_error"));
            }
            try {
                datasourceRequest.setQuery(qp.createQueryTableWithLimit(table, fields, Integer.valueOf(dataSetTableRequest.getRow()), false, ds));
                dataSetPreviewPage.setTotal(jdbcProvider.getData(datasourceRequest).size());
            } catch (Exception e) {
                logger.error(e.getMessage());
                DEException.throwException(Translator.get("i18n_ds_error"));
            }
        } else if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "custom")) {
            if (datasetTable.getMode() == 0) {
                Datasource ds = datasourceMapper.selectByPrimaryKey(dataSetTableRequest.getDataSourceId());
                if (ObjectUtils.isEmpty(ds)) {
                    throw new RuntimeException(Translator.get("i18n_datasource_delete"));
                }
                DatasourceProvider datasourceProvider = ProviderFactory.getProvider(ds.getType());
                DatasourceRequest datasourceRequest = new DatasourceRequest();
                datasourceRequest.setDatasource(ds);

                DataTableInfoDTO dt = new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class);
                List<DataSetTableUnionDTO> list = dataSetTableUnionService.listByTableId(dt.getList().get(0).getTableId());

                String sql = "";
                try {
                    sql = getCustomSQLDatasource(dt, list, ds);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    DEException.throwException(Translator.get("i18n_ds_error"));
                }
                QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
                datasourceRequest.setQuery(qp.createQuerySQLWithPage(sql, fields, page, pageSize, realSize, false));
                map.put("sql", datasourceRequest.getQuery());
                datasourceRequest.setPage(page);
                datasourceRequest.setFetchSize(Integer.parseInt(dataSetTableRequest.getRow()));
                datasourceRequest.setPageSize(pageSize);
                datasourceRequest.setRealSize(realSize);
                datasourceRequest.setPreviewData(true);
                try {
                    datasourceRequest.setPageable(true);
                    data.addAll(datasourceProvider.getData(datasourceRequest));
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    DEException.throwException(Translator.get("i18n_ds_error"));
                }
                try {
                    datasourceRequest.setPageable(false);
                    datasourceRequest.setQuery(qp.createQuerySqlWithLimit(sql, fields, Integer.valueOf(dataSetTableRequest.getRow()), false));
                    dataSetPreviewPage.setTotal(datasourceProvider.getData(datasourceRequest).size());
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    DEException.throwException(Translator.get("i18n_ds_error"));
                }
            } else {
                Datasource ds = (Datasource) CommonBeanFactory.getBean("DorisDatasource");
                JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
                DatasourceRequest datasourceRequest = new DatasourceRequest();
                datasourceRequest.setDatasource(ds);
                String table = DorisTableUtils.dorisName(dataSetTableRequest.getId());
                QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
                datasourceRequest.setQuery(qp.createQueryTableWithPage(table, fields, page, pageSize, realSize, false, ds));
                map.put("sql", datasourceRequest.getQuery());
                try {
                    data.addAll(jdbcProvider.getData(datasourceRequest));
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    DEException.throwException(Translator.get("i18n_ds_error"));
                }

                try {
                    datasourceRequest.setQuery(qp.createQueryTableWithLimit(table, fields, Integer.valueOf(dataSetTableRequest.getRow()), false, ds));
                    dataSetPreviewPage.setTotal(jdbcProvider.getData(datasourceRequest).size());
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    DEException.throwException(Translator.get("i18n_ds_error"));
                }
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

        if (StringUtils.isEmpty(sql)) {
            DataEaseException.throwException(Translator.get("i18n_sql_not_empty"));
        }
        QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
        String sqlAsTable = qp.createSQLPreview(sql, null);
        datasourceRequest.setQuery(sqlAsTable);
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
        DataTableInfoDTO dataTableInfoDTO = new Gson().fromJson(dataSetTableRequest.getInfo(), DataTableInfoDTO.class);
        List<DataSetTableUnionDTO> list = dataSetTableUnionService.listByTableId(dataTableInfoDTO.getList().get(0).getTableId());
        String sql;

        DatasourceRequest datasourceRequest = new DatasourceRequest();
        Datasource ds;
        if (dataSetTableRequest.getMode() == 0) {
            ds = datasourceMapper.selectByPrimaryKey(dataSetTableRequest.getDataSourceId());
            datasourceRequest.setDatasource(ds);
            sql = getCustomSQLDatasource(dataTableInfoDTO, list, ds);
        } else {
            ds = (Datasource) CommonBeanFactory.getBean("DorisDatasource");
            datasourceRequest.setDatasource(ds);
            sql = getCustomSQLDoris(dataTableInfoDTO, list);
        }
        DatasourceProvider datasourceProvider = ProviderFactory.getProvider(ds.getType());
        // 使用输入的sql先预执行一次,并拿到所有字段
        datasourceRequest.setQuery(sql);

        Map<String, Object> res = new HashMap<>();
        try {
            List<TableFiled> previewFields = datasourceProvider.fetchResultField(datasourceRequest);

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

            // 获取每个字段在当前de数据库中的name，作为sql查询后的remarks返回前端展示
            List<DatasetTableField> checkedFieldList = new ArrayList<>();
            dataTableInfoDTO.getList().forEach(ele -> checkedFieldList.addAll(dataSetTableFieldsService.getListByIds(ele.getCheckedFields())));
            for (DatasetTableField datasetTableField : checkedFieldList) {
                for (TableFiled tableFiled : fields) {
                    if (StringUtils.equalsIgnoreCase(tableFiled.getFieldName(), DorisTableUtils.dorisFieldName(datasetTableField.getTableId() + "_" + datasetTableField.getDataeaseName()))
                            || StringUtils.equalsIgnoreCase(tableFiled.getFieldName(), DorisTableUtils.dorisFieldName(datasetTableField.getTableId() + "_" + datasetTableField.getOriginName()))) {
                        tableFiled.setRemarks(datasetTableField.getName());
                        break;
                    }
                }
            }

            res.put("fields", fields);
            res.put("data", jsonArray);
            return res;
        } catch (Exception e) {
            return res;
        }
    }

    // 自助数据集从doris里预览数据
    private String getCustomSQLDoris(DataTableInfoDTO dataTableInfoDTO, List<DataSetTableUnionDTO> list) {
        Map<String, String[]> customInfo = new TreeMap<>();
        dataTableInfoDTO.getList().forEach(ele -> {
            String table = DorisTableUtils.dorisName(ele.getTableId());
            DatasetTable datasetTable = datasetTableMapper.selectByPrimaryKey(ele.getTableId());
            if (ObjectUtils.isEmpty(datasetTable)) {
                throw new RuntimeException(Translator.get("i18n_custom_ds_delete"));
            }
            List<DatasetTableField> fields = dataSetTableFieldsService.getListByIdsEach(ele.getCheckedFields());
            if (CollectionUtils.isEmpty(fields)) {
                throw new RuntimeException(Translator.get("i18n_cst_ds_tb_or_field_deleted"));
            }
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
                        if (ObjectUtils.isEmpty(sourceField) || ObjectUtils.isEmpty(targetField)) {
                            DEException.throwException(Translator.get("i18n_dataset_field_delete"));
                        }

                        join.append(convertUnionTypeToSQL(dto.getSourceUnionRelation()))
                                .append(DorisTableUtils.dorisName(dto.getTargetTableId()))
                                .append(" ON ")
                                .append(DorisTableUtils.dorisName(dto.getSourceTableId())).append(".").append(sourceField.getDataeaseName())
                                .append(" = ")
                                .append(DorisTableUtils.dorisName(dto.getTargetTableId())).append(".").append(targetField.getDataeaseName());
                    }
                }
            }
            if (StringUtils.isEmpty(f)) {
                throw new RuntimeException(Translator.get("i18n_custom_ds_delete"));
            }
            return MessageFormat.format("SELECT {0} FROM {1}", f, DorisTableUtils.dorisName(first.getTableId())) + join.toString();
        } else {
            if (StringUtils.isEmpty(StringUtils.join(customInfo.get(DorisTableUtils.dorisName(first.getTableId())), ","))) {
                throw new RuntimeException(Translator.get("i18n_custom_ds_delete"));
            }
            return MessageFormat.format("SELECT {0} FROM {1}", StringUtils.join(customInfo.get(DorisTableUtils.dorisName(first.getTableId())), ","), DorisTableUtils.dorisName(first.getTableId()));
        }
    }

    public String getCustomSQLDatasource(DataTableInfoDTO dataTableInfoDTO, List<DataSetTableUnionDTO> list, Datasource ds) {
        DatasourceTypes datasourceTypes = DatasourceTypes.valueOf(ds.getType());
        String keyword = datasourceTypes.getKeywordPrefix() + "%s" + datasourceTypes.getKeywordSuffix();
        Map<String, String[]> customInfo = new TreeMap<>();
        for (DataTableInfoCustomUnion ele : dataTableInfoDTO.getList()) {
            DatasetTable datasetTable = datasetTableMapper.selectByPrimaryKey(ele.getTableId());
            String table = new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class).getTable();
            if (ObjectUtils.isEmpty(datasetTable)) {
                throw new RuntimeException(Translator.get("i18n_custom_ds_delete"));
            }
            List<DatasetTableField> fields = dataSetTableFieldsService.getListByIdsEach(ele.getCheckedFields());
            if (CollectionUtils.isEmpty(fields)) {
                throw new RuntimeException(Translator.get("i18n_cst_ds_tb_or_field_deleted"));
            }
            String[] array = fields.stream().map(f -> String.format(keyword, table) + "." + String.format(keyword, f.getOriginName()) + " AS " + DorisTableUtils.dorisFieldName(ele.getTableId() + "_" + f.getOriginName())).toArray(String[]::new);
            customInfo.put(table, array);
        }
        DataTableInfoCustomUnion first = dataTableInfoDTO.getList().get(0);
        DatasetTable table = datasetTableMapper.selectByPrimaryKey(first.getTableId());
        String tableName = new Gson().fromJson(table.getInfo(), DataTableInfoDTO.class).getTable();
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
                        if (ObjectUtils.isEmpty(sourceField) || ObjectUtils.isEmpty(targetField)) {
                            DEException.throwException(Translator.get("i18n_dataset_field_delete"));
                        }
                        DatasetTable sourceTable = datasetTableMapper.selectByPrimaryKey(dto.getSourceTableId());
                        String sourceTableName = new Gson().fromJson(sourceTable.getInfo(), DataTableInfoDTO.class).getTable();
                        DatasetTable targetTable = datasetTableMapper.selectByPrimaryKey(dto.getTargetTableId());
                        String targetTableName = new Gson().fromJson(targetTable.getInfo(), DataTableInfoDTO.class).getTable();
                        join.append(convertUnionTypeToSQL(dto.getSourceUnionRelation()))
                                .append(String.format(keyword, targetTableName))
                                .append(" ON ")
                                .append(String.format(keyword, sourceTableName)).append(".").append(String.format(keyword, sourceField.getOriginName()))
                                .append(" = ")
                                .append(String.format(keyword, targetTableName)).append(".").append(String.format(keyword, targetField.getOriginName()));
                    }
                }
            }
            if (StringUtils.isEmpty(f)) {
                throw new RuntimeException(Translator.get("i18n_custom_ds_delete"));
            }
            return MessageFormat.format("SELECT {0} FROM {1}", f, String.format(keyword, tableName)) + join.toString();
        } else {
            if (StringUtils.isEmpty(StringUtils.join(customInfo.get(tableName), ","))) {
                throw new RuntimeException(Translator.get("i18n_custom_ds_delete"));
            }
            return MessageFormat.format("SELECT {0} FROM {1}", StringUtils.join(customInfo.get(tableName), ","), String.format(keyword, tableName));
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
            case "N:N":
                return " FULL JOIN ";
            default:
                return " INNER JOIN ";
        }
    }

    public List<DatasetTableField> saveExcelTableField(String datasetTableId, List<TableFiled> fields, boolean insert) {
        List<DatasetTableField> datasetTableFields = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(fields)) {
            for (int i = 0; i < fields.size(); i++) {
                TableFiled filed = fields.get(i);
                DatasetTableField datasetTableField = DatasetTableField.builder().build();
                datasetTableField.setTableId(datasetTableId);
                datasetTableField.setOriginName(filed.getFieldName());
                datasetTableField.setName(filed.getRemarks());
                datasetTableField.setDataeaseName(DorisTableUtils.columnName(filed.getFieldName()));
                datasetTableField.setType(filed.getFieldType());
                datasetTableField.setDeType(transFieldType(filed.getFieldType()));
                datasetTableField.setDeExtractType(transFieldType(filed.getFieldType()));
                datasetTableField.setSize(filed.getFieldSize());
                datasetTableField.setChecked(true);
                datasetTableField.setColumnIndex(i);
                datasetTableField.setLastSyncTime(System.currentTimeMillis());
                datasetTableField.setExtField(0);
                datasetTableField.setGroupType(datasetTableField.getDeType() < 2 ? "d" : "q");
                if (insert) {
                    dataSetTableFieldsService.save(datasetTableField);
                }
                datasetTableFields.add(datasetTableField);
            }
        }
        return datasetTableFields;
    }

    public void saveTableField(DatasetTable datasetTable) throws Exception {
        Datasource ds = datasourceMapper.selectByPrimaryKey(datasetTable.getDataSourceId());
        DataSetTableRequest dataSetTableRequest = new DataSetTableRequest();
        BeanUtils.copyBean(dataSetTableRequest, datasetTable);

        List<TableFiled> fields = new ArrayList<>();
        long syncTime = System.currentTimeMillis();
        if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "db")) {
            fields = getFields(datasetTable);
        } else if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "sql")) {
            DatasourceProvider datasourceProvider = ProviderFactory.getProvider(ds.getType());
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(ds);
            QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
            String sqlAsTable = qp.createSQLPreview(new Gson().fromJson(dataSetTableRequest.getInfo(), DataTableInfoDTO.class).getSql(), null);
            datasourceRequest.setQuery(sqlAsTable);
            fields = datasourceProvider.fetchResultField(datasourceRequest);
        } else if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "excel")) {
            fields = dataSetTableRequest.getFields();
        } else if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "custom")) {
            if (datasetTable.getMode() == 1) {
                // save field
                DataTableInfoDTO dataTableInfoDTO = new Gson().fromJson(dataSetTableRequest.getInfo(), DataTableInfoDTO.class);
                List<DataTableInfoCustomUnion> list = dataTableInfoDTO.getList();
                List<DatasetTableField> fieldList = new ArrayList<>();
                list.forEach(ele -> {
                    List<DatasetTableField> listByIds = dataSetTableFieldsService.getListByIdsEach(ele.getCheckedFields());
                    listByIds.forEach(f -> f.setDataeaseName(DorisTableUtils.dorisFieldName(ele.getTableId() + "_" + f.getDataeaseName())));
                    fieldList.addAll(listByIds);
                });
                for (int i = 0; i < fieldList.size(); i++) {
                    DatasetTableField datasetTableField = fieldList.get(i);
                    datasetTableField.setId(null);
                    datasetTableField.setTableId(datasetTable.getId());
                    datasetTableField.setColumnIndex(i);
                }
                dataSetTableFieldsService.deleteByTableId(datasetTable.getId());
                dataSetTableFieldsService.batchEdit(fieldList);
                // custom 创建doris视图
                if (datasetTable.getMode() == 1) {
                    createDorisView(DorisTableUtils.dorisName(datasetTable.getId()), getCustomSQLDoris(dataTableInfoDTO, dataSetTableUnionService.listByTableId(dataTableInfoDTO.getList().get(0).getTableId())));
                }
                return;
            } else {
                DatasourceProvider datasourceProvider = ProviderFactory.getProvider(ds.getType());
                DatasourceRequest datasourceRequest = new DatasourceRequest();
                datasourceRequest.setDatasource(ds);
                DataTableInfoDTO dt = new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class);
                List<DataSetTableUnionDTO> list = dataSetTableUnionService.listByTableId(dt.getList().get(0).getTableId());
                String sqlAsTable = getCustomSQLDatasource(dt, list, ds);
                datasourceRequest.setQuery(sqlAsTable);
                fields = datasourceProvider.fetchResultField(datasourceRequest);

                DataTableInfoDTO dataTableInfoDTO = new Gson().fromJson(dataSetTableRequest.getInfo(), DataTableInfoDTO.class);
                List<DataTableInfoCustomUnion> listField = dataTableInfoDTO.getList();
                List<DatasetTableField> fieldList = new ArrayList<>();
                listField.forEach(ele -> {
                    List<DatasetTableField> listByIds = dataSetTableFieldsService.getListByIdsEach(ele.getCheckedFields());
                    fieldList.addAll(listByIds);
                });
                for (DatasetTableField field : fieldList) {
                    for (TableFiled tableFiled : fields) {
                        if (StringUtils.equalsIgnoreCase(DorisTableUtils.dorisFieldName(field.getTableId() + "_" + field.getOriginName()), tableFiled.getFieldName())) {
                            tableFiled.setRemarks(field.getName());
                            break;
                        }
                    }
                }
            }
        }
        QueryProvider qp = null;
        if (!ObjectUtils.isEmpty(ds)) {
            qp = ProviderFactory.getQueryProvider(ds.getType());
        }
        if (CollectionUtils.isNotEmpty(fields)) {
            List<String> originNameList = new ArrayList<>();
            for (int i = 0; i < fields.size(); i++) {
                TableFiled filed = fields.get(i);
                System.out.println(new Gson().toJson(filed));
                originNameList.add(filed.getFieldName());

                DatasetTableField datasetTableField = DatasetTableField.builder().build();

                // 物理字段名设定为唯一，查询当前数据集下是否已存在该字段，存在则update，不存在则insert
                DatasetTableFieldExample datasetTableFieldExample = new DatasetTableFieldExample();
                // 字段名一致，认为字段没有改变
                datasetTableFieldExample.createCriteria().andTableIdEqualTo(datasetTable.getId()).andOriginNameEqualTo(filed.getFieldName());
                List<DatasetTableField> datasetTableFields = datasetTableFieldMapper.selectByExample(datasetTableFieldExample);
                if (CollectionUtils.isNotEmpty(datasetTableFields)) {
                    datasetTableField.setId(datasetTableFields.get(0).getId());
                    datasetTableField.setOriginName(filed.getFieldName());
                    datasetTableField.setType(filed.getFieldType());
                    datasetTableField.setSize(filed.getFieldSize());
                    if (ObjectUtils.isEmpty(ds)) {
                        datasetTableField.setDeExtractType(transFieldType(filed.getFieldType()));
                    } else {
                        Integer fieldType = qp.transFieldType(filed.getFieldType());
                        datasetTableField.setDeExtractType(fieldType);
                    }
                } else {
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
                    datasetTableField.setExtField(0);
                    datasetTableField.setGroupType(datasetTableField.getDeType() < 2 ? "d" : "q");
                }
                dataSetTableFieldsService.save(datasetTableField);
            }
            // delete 数据库中多余的字段
            DatasetTableFieldExample datasetTableFieldExample = new DatasetTableFieldExample();
            datasetTableFieldExample.createCriteria().andTableIdEqualTo(datasetTable.getId()).andExtFieldEqualTo(0).andOriginNameNotIn(originNameList);
            datasetTableFieldMapper.deleteByExample(datasetTableFieldExample);
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


    public void saveIncrementalConfig(DatasetTableIncrementalConfig datasetTableIncrementalConfig) throws Exception {
        if (datasetTableIncrementalConfig == null || StringUtils.isEmpty(datasetTableIncrementalConfig.getTableId())) {
            return;
        }
        if (StringUtils.isEmpty(datasetTableIncrementalConfig.getId())) {
            datasetTableIncrementalConfig.setId(UUID.randomUUID().toString());
            datasetTableIncrementalConfigMapper.insertSelective(datasetTableIncrementalConfig);
        } else {
            datasetTableIncrementalConfigMapper.updateByPrimaryKey(datasetTableIncrementalConfig);
        }
        checkColumes(datasetTableIncrementalConfig);
    }

    private void checkColumes(DatasetTableIncrementalConfig datasetTableIncrementalConfig) throws Exception {
        DatasetTable datasetTable = datasetTableMapper.selectByPrimaryKey(datasetTableIncrementalConfig.getTableId());
        List<DatasetTableField> datasetTableFields = dataSetTableFieldsService.getFieldsByTableId(datasetTable.getId());
        datasetTableFields.sort((o1, o2) -> {
            if (o1.getColumnIndex() == null) {
                return -1;
            }
            if (o2.getColumnIndex() == null) {
                return 1;
            }
            return o1.getColumnIndex().compareTo(o2.getColumnIndex());
        });

        List<String> originNameFileds = datasetTableFields.stream().map(DatasetTableField::getOriginName).collect(Collectors.toList());
        Datasource ds = datasourceMapper.selectByPrimaryKey(datasetTable.getDataSourceId());
        QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
        DatasourceProvider datasourceProvider = ProviderFactory.getProvider(ds.getType());
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(ds);
        if (StringUtils.isNotEmpty(datasetTableIncrementalConfig.getIncrementalAdd()) && StringUtils.isNotEmpty(datasetTableIncrementalConfig.getIncrementalAdd().replace(" ", ""))) {// 增量添加
            String sql = datasetTableIncrementalConfig.getIncrementalAdd().replace(lastUpdateTime, Long.valueOf(System.currentTimeMillis()).toString())
                    .replace(currentUpdateTime, Long.valueOf(System.currentTimeMillis()).toString());
            datasourceRequest.setQuery(qp.wrapSql(sql));
            List<String> sqlFileds = new ArrayList<>();
            try {
                datasourceProvider.fetchResultField(datasourceRequest).stream().map(TableFiled::getFieldName).forEach(filed -> {
                    sqlFileds.add(filed);
                });
            } catch (Exception e) {
                DataEaseException.throwException(Translator.get("i18n_check_sql_error") + e.getMessage());
            }

            if (!originNameFileds.equals(sqlFileds)) {
                DataEaseException.throwException(Translator.get("i18n_sql_add_not_matching") + sqlFileds.toString());
            }
        }
        if (StringUtils.isNotEmpty(datasetTableIncrementalConfig.getIncrementalDelete()) && StringUtils.isNotEmpty(datasetTableIncrementalConfig.getIncrementalDelete().replace(" ", ""))) {// 增量删除
            String sql = datasetTableIncrementalConfig.getIncrementalDelete().replace(lastUpdateTime, Long.valueOf(System.currentTimeMillis()).toString())
                    .replace(currentUpdateTime, Long.valueOf(System.currentTimeMillis()).toString());
            datasourceRequest.setQuery(qp.wrapSql(sql));
            List<String> sqlFileds = new ArrayList<>();
            try {
                datasourceProvider.fetchResultField(datasourceRequest).stream().map(TableFiled::getFieldName).forEach(filed -> sqlFileds.add(filed));
            } catch (Exception e) {
                DataEaseException.throwException(Translator.get("i18n_check_sql_error") + e.getMessage());
            }

            if (!originNameFileds.equals(sqlFileds)) {
                DataEaseException.throwException(Translator.get("i18n_sql_delete_not_matching") + sqlFileds.toString());
            }
        }
    }

    private void checkName(DatasetTable datasetTable) {
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

    public DataSetDetail getDatasetDetail(String id) {
        DataSetDetail dataSetDetail = new DataSetDetail();
        DatasetTable table = datasetTableMapper.selectByPrimaryKey(id);
        dataSetDetail.setTable(table);
        if (ObjectUtils.isNotEmpty(table)) {
            Datasource datasource = datasourceMapper.selectByPrimaryKey(table.getDataSourceId());
            dataSetDetail.setDatasource(datasource);
        }
        return dataSetDetail;
    }

    public ExcelFileData excelSaveAndParse(MultipartFile file, String tableId, Integer editType) throws Exception {
        String filename = file.getOriginalFilename();
        // parse file
        List<ExcelSheetData> excelSheetDataList = parseExcel2(filename, file.getInputStream(), true);
        List<ExcelSheetData> retrunSheetDataList = new ArrayList<>();

        if (StringUtils.isNotEmpty(tableId) && editType == 1) {
            List<DatasetTableField> datasetTableFields = dataSetTableFieldsService.getFieldsByTableId(tableId);
            datasetTableFields.stream().filter(datasetTableField -> datasetTableField.getExtField() == 0).collect(Collectors.toList());
            datasetTableFields.sort((o1, o2) -> {
                if (o1.getColumnIndex() == null) {
                    return -1;
                }
                if (o2.getColumnIndex() == null) {
                    return 1;
                }
                return o1.getColumnIndex().compareTo(o2.getColumnIndex());
            });
            List<String> oldFields = datasetTableFields.stream().map(DatasetTableField::getOriginName).collect(Collectors.toList());
            for (ExcelSheetData excelSheetData : excelSheetDataList) {
                List<TableFiled> fields = excelSheetData.getFields();
                List<String> newFields = fields.stream().map(TableFiled::getRemarks).collect(Collectors.toList());
                if (oldFields.equals(newFields)) {
                    retrunSheetDataList.add(excelSheetData);
                }
            }

            if (retrunSheetDataList.size() == 0) {
                DataEaseException.throwException(Translator.get("i18n_excel_column_change"));
            }
        } else {
            retrunSheetDataList = excelSheetDataList;
        }
        retrunSheetDataList = retrunSheetDataList.stream().filter(excelSheetData -> CollectionUtils.isNotEmpty(excelSheetData.getFields())).collect(Collectors.toList());
        // save file
        String excelId = UUID.randomUUID().toString();
        String filePath = saveFile(file, excelId);
        ExcelFileData excelFileData = new ExcelFileData();
        excelFileData.setExcelLable(filename);
        excelFileData.setId(excelId);
        excelFileData.setPath(filePath);

        filename = filename.substring(0, filename.lastIndexOf('.'));
        if (retrunSheetDataList.size() == 1) {
            retrunSheetDataList.get(0).setDatasetName(filename);
            retrunSheetDataList.get(0).setSheetExcelId(excelId);
            retrunSheetDataList.get(0).setId(UUID.randomUUID().toString());
            retrunSheetDataList.get(0).setPath(filePath);
        } else {
            for (ExcelSheetData excelSheetData : retrunSheetDataList) {
                excelSheetData.setDatasetName(filename + "-" + excelSheetData.getExcelLable());
                excelSheetData.setSheetExcelId(excelId);
                excelSheetData.setId(UUID.randomUUID().toString());
                excelSheetData.setPath(filePath);
            }
        }
        excelFileData.setSheets(retrunSheetDataList);
        return excelFileData;
    }

    private List<ExcelSheetData> parseExcel2(String filename, InputStream inputStream, boolean isPreview) throws Exception {
        List<ExcelSheetData> excelSheetDataList = new ArrayList<>();
        String suffix = filename.substring(filename.lastIndexOf(".") + 1);
        if (StringUtils.equalsIgnoreCase(suffix, "xls")) {
            ExcelXlsReader excelXlsReader = new ExcelXlsReader();
            excelXlsReader.process(inputStream);
            excelSheetDataList = excelXlsReader.totalSheets;
        }
        if (StringUtils.equalsIgnoreCase(suffix, "xlsx")) {
            ExcelXlsxReader excelXlsxReader = new ExcelXlsxReader();
            excelXlsxReader.process(inputStream);
            excelSheetDataList = excelXlsxReader.totalSheets;
        }
        inputStream.close();
        excelSheetDataList.forEach(excelSheetData -> {
            List<List<String>> data = excelSheetData.getData();
            String[] fieldArray = excelSheetData.getFields().stream().map(TableFiled::getFieldName).toArray(String[]::new);
            List<Map<String, Object>> jsonArray = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(data)) {
                jsonArray = data.stream().map(ele -> {
                    Map<String, Object> map = new HashMap<>();
                    for (int i = 0; i < ele.size(); i++) {
                        map.put(fieldArray[i], ele.get(i));
                    }
                    return map;
                }).collect(Collectors.toList());
            }
            excelSheetData.setFieldsMd5(Md5Utils.md5(StringUtils.join(fieldArray, ",")));
            excelSheetData.setJsonArray(jsonArray);
        });

        return excelSheetDataList;
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
            int columnNum = 0;
            for (int i = 0; i < rows; i++) {
                HSSFRow row = sheet0.getRow(i);
                if (i == 0) {
                    if (row == null) {
                        throw new RuntimeException(Translator.get("i18n_excel_header_empty"));
                    }
                    columnNum = row.getPhysicalNumberOfCells();
                }
                String[] r = new String[columnNum];
                for (int j = 0; j < columnNum; j++) {
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
                    } else {
                        if (row == null) {
                            break;
                        }
                        r[j] = readCell(row.getCell(j), true, fields.get(j));
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
            int columnNum = 0;
            for (int i = 0; i < rows; i++) {
                XSSFRow row = sheet0.getRow(i);
                if (i == 0) {
                    if (row == null) {
                        throw new RuntimeException(Translator.get("i18n_excel_header_empty"));
                    }
                    columnNum = row.getLastCellNum();
                }
                String[] r = new String[columnNum];
                for (int j = 0; j < columnNum; j++) {
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
                    } else {
                        if (row == null) {
                            break;
                        }
                        r[j] = readCell(row.getCell(j), true, fields.get(j));
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

        // 校验excel字段是否重名
        if (checkIsRepeat(fieldArray)) {
            DataEaseException.throwException(Translator.get("i18n_excel_field_repeat"));
        }

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
        if (cell == null) {
            return "";
        }
        CellType cellTypeEnum = cell.getCellTypeEnum();
        if (cellTypeEnum.equals(CellType.FORMULA)) {
            try {
                double d = cell.getNumericCellValue();
                try {
                    Double value = new Double(d);
                    double eps = 1e-10;
                    if (value - Math.floor(value) < eps) {
                        if (cellType) {
                            if (StringUtils.isEmpty(tableFiled.getFieldType()) || tableFiled.getFieldType().equalsIgnoreCase("TEXT")) {
                                tableFiled.setFieldType("LONG");
                            }
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
            } catch (IllegalStateException e) {
                String s = String.valueOf(cell.getRichStringCellValue());
                if (cellType) {
                    tableFiled.setFieldType("TEXT");
                    tableFiled.setFieldSize(65533);
                }
                return s;
            }
        }
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
                            if (StringUtils.isEmpty(tableFiled.getFieldType()) || tableFiled.getFieldType().equalsIgnoreCase("TEXT")) {
                                tableFiled.setFieldType("LONG");
                            }
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

    private String saveFile(MultipartFile file, String fileNameUUID) throws Exception {
        String filename = file.getOriginalFilename();
        String suffix = filename.substring(filename.lastIndexOf(".") + 1);
        String dirPath = path + AuthUtils.getUser().getUsername() + "/";
        File p = new File(dirPath);
        if (!p.exists()) {
            p.mkdirs();
        }
        String filePath = dirPath + fileNameUUID + "." + suffix;
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
        List<String> taskIds = datasetTableTaskLogMapper.selectByExample(datasetTableTaskLogExample).stream().map(DatasetTableTaskLog::getTaskId).collect(Collectors.toList());
        datasetTableTaskLogMapper.updateByExampleSelective(datasetTableTaskLog, datasetTableTaskLogExample);

        dataSetTableTaskService.updateTaskStatus(taskIds, JobStatus.Error);

        for (DatasetTable jobStoppeddDatasetTable : jobStoppeddDatasetTables) {
            extractDataService.deleteFile("all_scope", jobStoppeddDatasetTable.getId());
            extractDataService.deleteFile("incremental_add", jobStoppeddDatasetTable.getId());
            extractDataService.deleteFile("incremental_delete", jobStoppeddDatasetTable.getId());
        }
    }

    /*
     * 判断数组中是否有重复的值
     */
    public static boolean checkIsRepeat(String[] array) {
        HashSet<String> hashSet = new HashSet<String>();
        for (int i = 0; i < array.length; i++) {
            if (StringUtils.isEmpty(array[i])) {
                throw new RuntimeException(Translator.get("i18n_excel_empty_column"));
            }
            hashSet.add(array[i]);
        }
        if (hashSet.size() == array.length) {
            return false;
        } else {
            return true;
        }
    }

    public DatasetTable syncDatasetTableField(String id) throws Exception {
        DatasetTable datasetTable = datasetTableMapper.selectByPrimaryKey(id);
        saveTableField(datasetTable);
        return datasetTable;
    }
}
