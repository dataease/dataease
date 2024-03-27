package io.dataease.service.exportCenter;


import com.google.gson.Gson;
import io.dataease.commons.constants.JobStatus;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.CommonBeanFactory;
import io.dataease.commons.utils.LogUtil;
import io.dataease.commons.utils.TableUtils;
import io.dataease.controller.dataset.request.DataSetTaskInstanceGridRequest;
import io.dataease.controller.request.dataset.DataSetExportRequest;
import io.dataease.controller.request.dataset.DataSetTableRequest;
import io.dataease.dto.dataset.DataSetPreviewPage;
import io.dataease.dto.dataset.DataSetTableUnionDTO;
import io.dataease.dto.dataset.DataSetTaskLogDTO;
import io.dataease.i18n.Translator;
import io.dataease.plugins.common.base.domain.*;
import io.dataease.plugins.common.base.mapper.DatasetTableMapper;
import io.dataease.plugins.common.base.mapper.DatasourceMapper;
import io.dataease.plugins.common.base.mapper.ExportTaskMapper;
import io.dataease.plugins.common.constants.DatasetType;
import io.dataease.plugins.common.constants.DeTypeConstants;
import io.dataease.plugins.common.dto.dataset.DataTableInfoDTO;
import io.dataease.plugins.common.exception.DataEaseException;
import io.dataease.plugins.common.request.datasource.DatasourceRequest;
import io.dataease.plugins.common.request.permission.DataSetRowPermissionsTreeDTO;
import io.dataease.plugins.common.request.permission.DatasetRowPermissionsTreeObj;
import io.dataease.plugins.datasource.provider.Provider;
import io.dataease.plugins.datasource.provider.ProviderFactory;
import io.dataease.plugins.datasource.query.QueryProvider;
import io.dataease.plugins.xpack.auth.dto.request.ColumnPermissionItem;
import io.dataease.provider.datasource.JdbcProvider;
import io.dataease.service.chart.util.ChartDataBuild;
import io.dataease.service.dataset.*;
import io.dataease.service.datasource.DatasourceService;
import io.dataease.service.engine.EngineService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service
public class ExportCenterService {

    @Resource
    private ExportTaskMapper exportTaskMapper;
    @Value("${export.dataset.limit:100000}")
    private int limit;

    private static final String exportData_path = "/opt/dataease/data/exportData/";

    @Value("${extract.page.size:50000}")
    private Integer extractPageSize;
    static private List<String> STATUS = Arrays.asList("SUCCESS", "FAILED", "PENDING", "IN_PROGRESS", "ALL");

    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
    @Resource
    private DataSetTableService dataSetTableService;
    @Resource
    private DataSetTableUnionService dataSetTableUnionService;
    @Resource
    private EngineService engineService;
    @Resource
    private DatasetTableMapper datasetTableMapper;
    @Resource
    private DatasourceMapper datasourceMapper;
    @Resource
    private DataSetTableFieldsService dataSetTableFieldsService;
    @Resource
    private PermissionsTreeService permissionsTreeService;
    @Resource
    private DatasourceService datasourceService;
    @Resource
    private PermissionService permissionService;

    private int corePoolSize = 10;
    private int keepAliveSeconds = 600;

    @PostConstruct
    public void init() {
        scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(corePoolSize);
        scheduledThreadPoolExecutor.setKeepAliveTime(keepAliveSeconds, TimeUnit.SECONDS);
    }

    public List<ExportTask> exportTasks(String status) {
        if (!STATUS.contains(status)) {
            DataEaseException.throwException("Invalid status: " + status);
        }
        ExportTaskExample exportTaskExample = new ExportTaskExample();
        ExportTaskExample.Criteria criteria = exportTaskExample.createCriteria();
        if (!status.equalsIgnoreCase("ALL")) {
            criteria.andExportStatusEqualTo(status);
        }
        criteria.andUserIdEqualTo(AuthUtils.getUser().getUserId());
        return exportTaskMapper.selectByExample(exportTaskExample);
    }

    public void addTask(String exportFrom, String exportFromType, DataSetExportRequest request) {
        ExportTask exportTask = new ExportTask();
        exportTask.setId(UUID.randomUUID().toString());
        exportTask.setUserId(AuthUtils.getUser().getUserId());
        exportTask.setExportFrom(exportFrom);
        exportTask.setExportFromType(exportFromType);
        exportTask.setExportStatus("PENDING");
        exportTask.setFileName(request.getFilename() + ".xlsx");
        exportTask.setExportPogress("0");
        exportTask.setExportTime(System.currentTimeMillis());
        exportTaskMapper.insert(exportTask);

        String dataPath = exportData_path + exportTask.getId();
        File directory = new File(dataPath);
        boolean isCreated = directory.mkdir();


        scheduledThreadPoolExecutor.execute(() -> {
            try {
                exportTask.setExportStatus("IN_PROGRESS");
                exportTaskMapper.updateByPrimaryKey(exportTask);
                DatasetRowPermissionsTreeObj tree = null;
                if (StringUtils.isNotEmpty(request.getExpressionTree())) {
                    Gson gson = new Gson();
                    tree = gson.fromJson(request.getExpressionTree(), DatasetRowPermissionsTreeObj.class);
                    permissionsTreeService.getField(tree);
                }
                Datasource datasource = datasourceService.get(request.getDataSourceId());

                Integer totalCount = getTotal(request, limit, tree);

                if (totalCount == null) {

                    Workbook wb = new SXSSFWorkbook();
                    // Sheet
                    Sheet detailsSheet = wb.createSheet("数据");
                    //给单元格设置样式
                    CellStyle cellStyle = wb.createCellStyle();
                    Font font = wb.createFont();
                    //设置字体大小
                    font.setFontHeightInPoints((short) 12);
                    //设置字体加粗
                    font.setBold(true);
                    //给字体设置样式
                    cellStyle.setFont(font);
                    //设置单元格背景颜色
                    cellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                    //设置单元格填充样式(使用纯色背景颜色填充)
                    cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                    int pageSize = (datasource != null && StringUtils.equalsIgnoreCase(datasource.getType(), "es")) ? 10000 : limit;
                    request.setRow(String.valueOf(pageSize));
                    Map<String, Object> previewData = dataSetTableService.getPreviewData(request, 1, pageSize, null, tree);
                    List<DatasetTableField> fields = (List<DatasetTableField>) previewData.get("fields");
                    List<Map<String, Object>> data = (List<Map<String, Object>>) previewData.get("data");
                    List<String> header = new ArrayList<>();
                    for (DatasetTableField field : fields) {
                        header.add(field.getName());
                    }

                    List<List<String>> details = new ArrayList<>();
                    details.add(header);

                    for (Map<String, Object> obj : data) {
                        List<String> row = new ArrayList<>();
                        for (DatasetTableField field : fields) {
                            String string = (String) obj.get(field.getDataeaseName());
                            row.add(string);
                        }
                        details.add(row);
                    }
                    if (CollectionUtils.isNotEmpty(details)) {
                        for (int i = 0; i < details.size(); i++) {
                            Row row = detailsSheet.createRow(i);
                            List<String> rowData = details.get(i);
                            if (rowData != null) {
                                for (int j = 0; j < rowData.size(); j++) {
                                    Cell cell = row.createCell(j);
                                    if (i == 0) {// 头部
                                        cell.setCellValue(rowData.get(j));
                                        cell.setCellStyle(cellStyle);
                                        //设置列的宽度
                                        detailsSheet.setColumnWidth(j, 255 * 20);
                                    } else {
                                        if ((fields.get(j).getDeType().equals(DeTypeConstants.DE_INT) || fields.get(j).getDeType() == DeTypeConstants.DE_FLOAT) && StringUtils.isNotEmpty(rowData.get(j))) {
                                            try {
                                                cell.setCellValue(Double.valueOf(rowData.get(j)));
                                            } catch (Exception e) {
                                                LogUtil.warn("export excel data transform error");
                                            }
                                        } else {
                                            cell.setCellValue(rowData.get(j));
                                        }
                                    }
                                }
                            }
                        }
                    }

                    try (FileOutputStream outputStream = new FileOutputStream(dataPath + "/" + request.getFilename() + ".xlsx")) {
                        wb.write(outputStream);
                    }
                    wb.close();
                } else {
                    Integer totalPage = (totalCount / extractPageSize) + (totalCount % extractPageSize > 0 ? 1 : 0);
                    List<List<String>> details = new ArrayList<>();
                    List<DatasetTableField> fields = new ArrayList<>();
                    for (Integer p = 1; p < totalPage + 1; p++) {
                        Integer offset = p * extractPageSize;
                        Integer all = offset + extractPageSize;

                        Map<String, Object> previewData = getPreviewData(request, p, extractPageSize, all, null, tree);

                        Workbook wb = null;
                        Sheet detailsSheet = null;
                        CellStyle cellStyle = null;
                        FileInputStream fis = null;
                        if (p == 0L) {
                            wb = new SXSSFWorkbook();
                            // Sheet
                            detailsSheet = wb.createSheet("数据");
                            //给单元格设置样式
                            cellStyle = wb.createCellStyle();
                            Font font = wb.createFont();
                            //设置字体大小
                            font.setFontHeightInPoints((short) 12);
                            //设置字体加粗
                            font.setBold(true);
                            //给字体设置样式
                            cellStyle.setFont(font);
                            //设置单元格背景颜色
                            cellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                            //设置单元格填充样式(使用纯色背景颜色填充)
                            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                            fields = (List<DatasetTableField>) previewData.get("fields");
                            List<String> header = new ArrayList<>();
                            for (DatasetTableField field : fields) {
                                header.add(field.getName());
                            }
                            details.add(header);
                        } else {
                            fis = new FileInputStream("existing_file.xlsx");
                            wb = new XSSFWorkbook(fis); // 假设这是个.xlsx格式的文件
                            detailsSheet = wb.getSheetAt(0);
                        }

                        List<Map<String, Object>> data = (List<Map<String, Object>>) previewData.get("data");
                        for (Map<String, Object> obj : data) {
                            List<String> row = new ArrayList<>();
                            for (DatasetTableField field : fields) {
                                String string = (String) obj.get(field.getDataeaseName());
                                row.add(string);
                            }
                            details.add(row);
                        }


                        if (CollectionUtils.isNotEmpty(details)) {
                            for (int i = 0; i < details.size(); i++) {
                                Row row = detailsSheet.createRow(i);
                                List<String> rowData = details.get(i);
                                if (rowData != null) {
                                    for (int j = 0; j < rowData.size(); j++) {
                                        Cell cell = row.createCell(j);
                                        if (i == 0) {// 头部
                                            cell.setCellValue(rowData.get(j));
                                            cell.setCellStyle(cellStyle);
                                            //设置列的宽度
                                            detailsSheet.setColumnWidth(j, 255 * 20);
                                        } else {
                                            if ((fields.get(j).getDeType().equals(DeTypeConstants.DE_INT) || fields.get(j).getDeType() == DeTypeConstants.DE_FLOAT) && StringUtils.isNotEmpty(rowData.get(j))) {
                                                try {
                                                    cell.setCellValue(Double.valueOf(rowData.get(j)));
                                                } catch (Exception e) {
                                                    LogUtil.warn("export excel data transform error");
                                                }
                                            } else {
                                                cell.setCellValue(rowData.get(j));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (p == 1) {
                            try (FileOutputStream outputStream = new FileOutputStream(dataPath + "/" + request.getFilename() + ".xlsx")) {
                                wb.write(outputStream);
                            }
                            wb.close();
                        } else {
                            fis.close();
                            wb.close();
                        }


                        exportTask.setExportStatus("IN_PROGRESS");
                        double exportRogress = (double) (p / totalCount);
                        DecimalFormat df = new DecimalFormat("#.##");
                        String formattedResult = df.format(exportRogress * 100);
                        exportTask.setExportPogress(formattedResult);
                        exportTaskMapper.updateByPrimaryKey(exportTask);


                    }

                }
                exportTask.setExportStatus("SUCCESS");
            } catch (Exception e) {
                exportTask.setExportStatus("FAILED");
            } finally {
                exportTaskMapper.updateByPrimaryKey(exportTask);
            }
        });


    }

    public Boolean checkEngineTableIsExists(String id) throws Exception {
        Datasource engine = engineService.getDeEngine();
        JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(engine);
        QueryProvider qp = ProviderFactory.getQueryProvider(engine.getType());
        datasourceRequest.setQuery(qp.searchTable(TableUtils.tableName(id)));
        List<String[]> data = jdbcProvider.getData(datasourceRequest);
        return CollectionUtils.isNotEmpty(data);
    }

    private Integer getTotal(DataSetTableRequest dataSetTableRequest, Integer limit, DatasetRowPermissionsTreeObj extTree) throws Exception {
        DatasetTableField datasetTableField = DatasetTableField.builder().tableId(dataSetTableRequest.getId()).checked(Boolean.TRUE).build();
        List<DatasetTableField> fields = dataSetTableFieldsService.list(datasetTableField);
        if (CollectionUtils.isEmpty(fields)) {
            return null;
        }
        DatasetTable datasetTable = datasetTableMapper.selectByPrimaryKey(dataSetTableRequest.getId());
        // 行权限
        List<DataSetRowPermissionsTreeDTO> rowPermissionsTree = permissionsTreeService.getRowPermissionsTree(fields, datasetTable, null);
        // ext filter
        if (extTree != null) {
            DataSetRowPermissionsTreeDTO dto = new DataSetRowPermissionsTreeDTO();
            dto.setTree(extTree);
            rowPermissionsTree.add(dto);
        }
        // 列权限
        Map<String, ColumnPermissionItem> desensitizationList = new HashMap<>();
        fields = permissionService.filterColumnPermissions(fields, desensitizationList, datasetTable.getId(), null);
        if (CollectionUtils.isEmpty(fields)) {
            return null;
        }
        DataTableInfoDTO dataTableInfoDTO = new Gson().fromJson(dataSetTableRequest.getInfo(), DataTableInfoDTO.class);
        if (StringUtils.equalsIgnoreCase(datasetTable.getType(), DatasetType.DB.name()) || StringUtils.equalsIgnoreCase(datasetTable.getType(), DatasetType.API.name())) {
            if (datasetTable.getMode() == 0) {
                Datasource ds = datasourceMapper.selectByPrimaryKey(dataSetTableRequest.getDataSourceId());
                if (ObjectUtils.isEmpty(ds)) {
                    throw new RuntimeException(Translator.get("i18n_datasource_delete"));
                }
                if (StringUtils.isNotEmpty(ds.getStatus()) && ds.getStatus().equalsIgnoreCase("Error")) {
                    throw new Exception(Translator.get("i18n_invalid_ds"));
                }
                Provider datasourceProvider = ProviderFactory.getProvider(ds.getType());
                DatasourceRequest datasourceRequest = new DatasourceRequest();
                datasourceRequest.setDatasource(ds);
                String table = dataTableInfoDTO.getTable();
                QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
                try {
                    String totalSql = qp.getTotalCount(false, qp.createQuerySQL(table, fields, false, ds, null, rowPermissionsTree), ds);
                    if (totalSql == null) {
                        return null;
                    }
                    datasourceRequest.setQuery(totalSql);
                    List<String[]> tmpData = datasourceProvider.getData(datasourceRequest);
                    return CollectionUtils.isEmpty(tmpData) ? 0 : Integer.valueOf(tmpData.get(0)[0]);
                } catch (Exception e) {
                    DataEaseException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
                }
            } else {
                // check doris table
                if (!checkEngineTableIsExists(dataSetTableRequest.getId())) {
                    if (dataSetTableRequest.isPreviewForTask()) {
                        return null;
                    } else {
                        throw new RuntimeException(Translator.get("i18n_data_not_sync"));
                    }
                }
                Datasource ds = engineService.getDeEngine();
                JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
                DatasourceRequest datasourceRequest = new DatasourceRequest();
                datasourceRequest.setDatasource(ds);
                String table = TableUtils.tableName(dataSetTableRequest.getId());
                QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());

                try {
                    String totalSql = qp.getTotalCount(false, qp.createQuerySQL(table, fields, false, ds, null, rowPermissionsTree), ds);
                    if (totalSql == null) {
                        return null;
                    }
                    datasourceRequest.setQuery(totalSql);
                    List<String[]> tmpData = jdbcProvider.getData(datasourceRequest);
                    return CollectionUtils.isEmpty(tmpData) ? 0 : Integer.valueOf(tmpData.get(0)[0]);
                } catch (Exception e) {
                    DataEaseException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
                }
            }

        } else if (StringUtils.equalsIgnoreCase(datasetTable.getType(), DatasetType.SQL.name())) {
            if (datasetTable.getMode() == 0) {
                Datasource ds = datasourceMapper.selectByPrimaryKey(dataSetTableRequest.getDataSourceId());
                if (ObjectUtils.isEmpty(ds)) {
                    throw new RuntimeException(Translator.get("i18n_datasource_delete"));
                }
                if (StringUtils.isNotEmpty(ds.getStatus()) && ds.getStatus().equalsIgnoreCase("Error")) {
                    throw new Exception(Translator.get("i18n_invalid_ds"));
                }
                Provider datasourceProvider = ProviderFactory.getProvider(ds.getType());
                DatasourceRequest datasourceRequest = new DatasourceRequest();
                datasourceRequest.setDatasource(ds);
                DataTableInfoDTO dataTableInfo = new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class);
                String sql = dataTableInfo.isBase64Encryption() ? new String(java.util.Base64.getDecoder().decode(dataTableInfo.getSql())) : dataTableInfo.getSql();
                sql = dataSetTableService.handleVariableDefaultValue(sql, datasetTable.getSqlVariableDetails(), ds.getType(), false);
                QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
                try {
                    String totalSql = qp.getTotalCount(false, qp.createQuerySQLAsTmp(sql, fields, false, null, rowPermissionsTree), ds);
                    if (totalSql == null) {
                        return null;
                    }
                    datasourceRequest.setQuery(totalSql);
                    List<String[]> tmpData = datasourceProvider.getData(datasourceRequest);
                    return CollectionUtils.isEmpty(tmpData) ? 0 : Integer.valueOf(tmpData.get(0)[0]);
                } catch (Exception e) {
                    DataEaseException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
                }
            } else {
                // check doris table
                if (!checkEngineTableIsExists(dataSetTableRequest.getId())) {
                    throw new RuntimeException(Translator.get("i18n_data_not_sync"));
                }
                Datasource ds = engineService.getDeEngine();
                JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
                DatasourceRequest datasourceRequest = new DatasourceRequest();
                datasourceRequest.setDatasource(ds);
                String table = TableUtils.tableName(dataSetTableRequest.getId());
                QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());


                try {
                    String totalSql = qp.getTotalCount(true, qp.createQuerySQL(table, fields, false, ds, null, rowPermissionsTree), ds);
                    if (totalSql == null) {
                        return null;
                    }
                    datasourceRequest.setQuery(totalSql);
                    List<String[]> tmpData = jdbcProvider.getData(datasourceRequest);
                    return CollectionUtils.isEmpty(tmpData) ? 0 : Integer.valueOf(tmpData.get(0)[0]);
                } catch (Exception e) {
                    DataEaseException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
                }
            }
        } else if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "excel")) {
            if (!checkEngineTableIsExists(dataSetTableRequest.getId())) {
                throw new RuntimeException(Translator.get("i18n_data_not_sync"));
            }

            Datasource ds = engineService.getDeEngine();
            JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(ds);
            String table = TableUtils.tableName(dataSetTableRequest.getId());
            QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
            try {
                String totalSql = qp.getTotalCount(true, qp.createQuerySQL(table, fields, false, ds, null, rowPermissionsTree), ds);
                if (totalSql == null) {
                    return null;
                }
                datasourceRequest.setQuery(totalSql);
                List<String[]> tmpData = jdbcProvider.getData(datasourceRequest);
                return CollectionUtils.isEmpty(tmpData) ? 0 : Integer.valueOf(tmpData.get(0)[0]);
            } catch (Exception e) {
                DataEaseException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
            }

        } else if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "custom")) {
            if (datasetTable.getMode() == 0) {
                Datasource ds = datasourceMapper.selectByPrimaryKey(dataSetTableRequest.getDataSourceId());
                if (ObjectUtils.isEmpty(ds)) {
                    throw new RuntimeException(Translator.get("i18n_datasource_delete"));
                }
                Provider datasourceProvider = ProviderFactory.getProvider(ds.getType());
                DatasourceRequest datasourceRequest = new DatasourceRequest();
                datasourceRequest.setDatasource(ds);

                DataTableInfoDTO dt = new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class);
                List<DataSetTableUnionDTO> list = dataSetTableUnionService.listByTableId(dt.getList().get(0).getTableId());

                String sql = "";
                try {
                    sql = dataSetTableService.getCustomSQLDatasource(dt, list, ds);
                } catch (Exception e) {
                    DataEaseException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
                }
                QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
                try {
                    String totalSql = qp.getTotalCount(false, qp.createQuerySQLAsTmp(sql, fields, false, null, rowPermissionsTree), ds);
                    if (totalSql == null) {
                        return null;
                    }
                    datasourceRequest.setQuery(totalSql);
                    List<String[]> tmpData = datasourceProvider.getData(datasourceRequest);
                    return CollectionUtils.isEmpty(tmpData) ? 0 : Integer.valueOf(tmpData.get(0)[0]);
                } catch (Exception e) {
                    DataEaseException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
                }

            } else {
                Datasource ds = engineService.getDeEngine();
                JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
                DatasourceRequest datasourceRequest = new DatasourceRequest();
                datasourceRequest.setDatasource(ds);
                String table = TableUtils.tableName(dataSetTableRequest.getId());
                QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
                try {
                    String totalSql = qp.getTotalCount(true, qp.createQuerySQL(table, fields, false, ds, null, rowPermissionsTree), ds);
                    if (totalSql == null) {
                        return null;
                    }
                    datasourceRequest.setQuery(totalSql);
                    List<String[]> tmpData = jdbcProvider.getData(datasourceRequest);
                    return CollectionUtils.isEmpty(tmpData) ? 0 : Integer.valueOf(tmpData.get(0)[0]);
                } catch (Exception e) {
                    DataEaseException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
                }
            }
        } else if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "union")) {
            if (datasetTable.getMode() == 0) {
                Datasource ds = datasourceMapper.selectByPrimaryKey(dataSetTableRequest.getDataSourceId());
                if (ObjectUtils.isEmpty(ds)) {
                    DataEaseException.throwException(Translator.get("i18n_datasource_delete"));
                }
                Provider datasourceProvider = ProviderFactory.getProvider(ds.getType());
                DatasourceRequest datasourceRequest = new DatasourceRequest();
                datasourceRequest.setDatasource(ds);

                DataTableInfoDTO dt = new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class);

                String sql = "";
                try {
                    sql = (String) dataSetTableService.getUnionSQLDatasource(dt, ds).get("sql");
                } catch (Exception e) {

                    DataEaseException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
                }
                QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
                try {
                    String totalSql = qp.getTotalCount(true, qp.createQuerySQLAsTmp(sql, fields, false, null, rowPermissionsTree), ds);
                    if (totalSql == null) {
                        return null;
                    }
                    datasourceRequest.setQuery(totalSql);
                    List<String[]> tmpData = datasourceProvider.getData(datasourceRequest);
                    return CollectionUtils.isEmpty(tmpData) ? 0 : Integer.valueOf(tmpData.get(0)[0]);
                } catch (Exception e) {
                    DataEaseException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
                }
            } else {
                Datasource ds = engineService.getDeEngine();
                JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
                DatasourceRequest datasourceRequest = new DatasourceRequest();
                datasourceRequest.setDatasource(ds);
                String table = TableUtils.tableName(dataSetTableRequest.getId());
                QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
                try {
                    String totalSql = qp.getTotalCount(true, qp.createQuerySQL(table, fields, false, ds, null, rowPermissionsTree), ds);
                    if (totalSql == null) {
                        return null;
                    }
                    datasourceRequest.setQuery(totalSql);
                    List<String[]> tmpData = jdbcProvider.getData(datasourceRequest);
                    return CollectionUtils.isEmpty(tmpData) ? 0 : Integer.valueOf(tmpData.get(0)[0]);
                } catch (Exception e) {
                    DataEaseException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
                }
            }
        }
        return null;
    }


    public Map<String, Object> getPreviewData(DataSetTableRequest dataSetTableRequest, Integer page, Integer pageSize, Integer realSize, List<DatasetTableField> extFields, DatasetRowPermissionsTreeObj extTree) throws Exception {
        Map<String, Object> map = new HashMap<>();
        String syncStatus = "";
        DatasetTableField datasetTableField = DatasetTableField.builder().tableId(dataSetTableRequest.getId()).checked(Boolean.TRUE).build();
        List<DatasetTableField> fields = dataSetTableFieldsService.list(datasetTableField);
        if (CollectionUtils.isNotEmpty(extFields)) {
            fields = extFields;
        }
        if (CollectionUtils.isEmpty(fields)) {
            map.put("fields", fields);
            map.put("data", new ArrayList<>());
            return map;
        }
        DatasetTable datasetTable = datasetTableMapper.selectByPrimaryKey(dataSetTableRequest.getId());
        // 行权限
        List<DataSetRowPermissionsTreeDTO> rowPermissionsTree = permissionsTreeService.getRowPermissionsTree(fields, datasetTable, null);
        // ext filter
        if (extTree != null) {
            DataSetRowPermissionsTreeDTO dto = new DataSetRowPermissionsTreeDTO();
            dto.setTree(extTree);
            rowPermissionsTree.add(dto);
        }
        // 列权限
        Map<String, ColumnPermissionItem> desensitizationList = new HashMap<>();
        fields = permissionService.filterColumnPermissions(fields, desensitizationList, datasetTable.getId(), null);
        if (CollectionUtils.isEmpty(fields)) {
            map.put("fields", fields);
            map.put("data", new ArrayList<>());
            return map;
        }

        String[] fieldArray = fields.stream().map(DatasetTableField::getDataeaseName).toArray(String[]::new);

        DataTableInfoDTO dataTableInfoDTO = new Gson().fromJson(dataSetTableRequest.getInfo(), DataTableInfoDTO.class);

        List<String[]> data = new ArrayList<>();
        if (StringUtils.equalsIgnoreCase(datasetTable.getType(), DatasetType.DB.name()) || StringUtils.equalsIgnoreCase(datasetTable.getType(), DatasetType.API.name())) {
            if (datasetTable.getMode() == 0) {
                Datasource ds = datasourceMapper.selectByPrimaryKey(dataSetTableRequest.getDataSourceId());
                if (ObjectUtils.isEmpty(ds)) {
                    throw new RuntimeException(Translator.get("i18n_datasource_delete"));
                }
                if (StringUtils.isNotEmpty(ds.getStatus()) && ds.getStatus().equalsIgnoreCase("Error")) {
                    throw new Exception(Translator.get("i18n_invalid_ds"));
                }
                Provider datasourceProvider = ProviderFactory.getProvider(ds.getType());
                DatasourceRequest datasourceRequest = new DatasourceRequest();
                datasourceRequest.setDatasource(ds);
                String table = dataTableInfoDTO.getTable();
                QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
                datasourceRequest.setQuery(qp.createQueryTableWithPage(table, fields, page, pageSize, realSize, false, ds, null, rowPermissionsTree));

                try {

                    data.addAll(datasourceProvider.getData(datasourceRequest));
                } catch (Exception e) {

                    DataEaseException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
                }

            } else {
                // check doris table
                if (!checkEngineTableIsExists(dataSetTableRequest.getId())) {
                    if (dataSetTableRequest.isPreviewForTask()) {
                        map.put("fields", fields);
                        map.put("data", new ArrayList<>());
                        map.put("page", new DataSetPreviewPage());
                        return map;
                    } else {
                        throw new RuntimeException(Translator.get("i18n_data_not_sync"));
                    }
                }
                Datasource ds = engineService.getDeEngine();
                JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
                DatasourceRequest datasourceRequest = new DatasourceRequest();
                datasourceRequest.setDatasource(ds);
                String table = TableUtils.tableName(dataSetTableRequest.getId());
                QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
                datasourceRequest.setQuery(qp.createQueryTableWithPage(table, fields, page, pageSize, realSize, false, ds, null, rowPermissionsTree));
                map.put("sql", java.util.Base64.getEncoder().encodeToString(datasourceRequest.getQuery().getBytes()));
                try {
                    data.addAll(jdbcProvider.getData(datasourceRequest));
                } catch (Exception e) {

                    DataEaseException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
                }

            }

        } else if (StringUtils.equalsIgnoreCase(datasetTable.getType(), DatasetType.SQL.name())) {
            if (datasetTable.getMode() == 0) {
                Datasource ds = datasourceMapper.selectByPrimaryKey(dataSetTableRequest.getDataSourceId());
                if (ObjectUtils.isEmpty(ds)) {
                    throw new RuntimeException(Translator.get("i18n_datasource_delete"));
                }
                if (StringUtils.isNotEmpty(ds.getStatus()) && ds.getStatus().equalsIgnoreCase("Error")) {
                    throw new Exception(Translator.get("i18n_invalid_ds"));
                }
                Provider datasourceProvider = ProviderFactory.getProvider(ds.getType());
                DatasourceRequest datasourceRequest = new DatasourceRequest();
                datasourceRequest.setDatasource(ds);
                DataTableInfoDTO dataTableInfo = new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class);
                String sql = dataTableInfo.isBase64Encryption() ? new String(java.util.Base64.getDecoder().decode(dataTableInfo.getSql())) : dataTableInfo.getSql();
                sql = dataSetTableService.handleVariableDefaultValue(sql, datasetTable.getSqlVariableDetails(), ds.getType(), false);
                QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
                datasourceRequest.setQuery(qp.createQuerySQLWithPage(sql, fields, page, pageSize, realSize, false, null, rowPermissionsTree));
                map.put("sql", java.util.Base64.getEncoder().encodeToString(datasourceRequest.getQuery().getBytes()));
                datasourceRequest.setPage(page);
                datasourceRequest.setFetchSize(Integer.parseInt(dataSetTableRequest.getRow()));
                datasourceRequest.setPageSize(pageSize);
                datasourceRequest.setRealSize(realSize);
                datasourceRequest.setPreviewData(true);
                try {
                    datasourceRequest.setPageable(true);
                    datasourceRequest.setPermissionFields(fields);
                    data.addAll(datasourceProvider.getData(datasourceRequest));
                } catch (Exception e) {

                    DataEaseException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
                }

            } else {
                // check doris table
                if (!checkEngineTableIsExists(dataSetTableRequest.getId())) {
                    throw new RuntimeException(Translator.get("i18n_data_not_sync"));
                }
                Datasource ds = engineService.getDeEngine();
                JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
                DatasourceRequest datasourceRequest = new DatasourceRequest();
                datasourceRequest.setDatasource(ds);
                String table = TableUtils.tableName(dataSetTableRequest.getId());
                QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
                datasourceRequest.setQuery(qp.createQueryTableWithPage(table, fields, page, pageSize, realSize, false, ds, null, rowPermissionsTree));
                map.put("sql", java.util.Base64.getEncoder().encodeToString(datasourceRequest.getQuery().getBytes()));
                try {
                    data.addAll(jdbcProvider.getData(datasourceRequest));
                } catch (Exception e) {

                    DataEaseException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
                }

            }
        } else if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "excel")) {
            if (!checkEngineTableIsExists(dataSetTableRequest.getId())) {
                throw new RuntimeException(Translator.get("i18n_data_not_sync"));
            }

            Datasource ds = engineService.getDeEngine();
            JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(ds);
            String table = TableUtils.tableName(dataSetTableRequest.getId());
            QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
            datasourceRequest.setQuery(qp.createQueryTableWithPage(table, fields, page, pageSize, realSize, false, ds, null, rowPermissionsTree));
            map.put("sql", java.util.Base64.getEncoder().encodeToString(datasourceRequest.getQuery().getBytes()));
            try {
                data.addAll(jdbcProvider.getData(datasourceRequest));
            } catch (Exception e) {

                DataEaseException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
            }


        } else if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "custom")) {
            if (datasetTable.getMode() == 0) {
                Datasource ds = datasourceMapper.selectByPrimaryKey(dataSetTableRequest.getDataSourceId());
                if (ObjectUtils.isEmpty(ds)) {
                    throw new RuntimeException(Translator.get("i18n_datasource_delete"));
                }
                Provider datasourceProvider = ProviderFactory.getProvider(ds.getType());
                DatasourceRequest datasourceRequest = new DatasourceRequest();
                datasourceRequest.setDatasource(ds);

                DataTableInfoDTO dt = new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class);
                List<DataSetTableUnionDTO> list = dataSetTableUnionService.listByTableId(dt.getList().get(0).getTableId());

                String sql = "";
                try {
                    sql = dataSetTableService.getCustomSQLDatasource(dt, list, ds);
                } catch (Exception e) {

                    DataEaseException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
                }
                QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
                datasourceRequest.setQuery(qp.createQuerySQLWithPage(sql, fields, page, pageSize, realSize, false, null, rowPermissionsTree));
                map.put("sql", java.util.Base64.getEncoder().encodeToString(datasourceRequest.getQuery().getBytes()));

                try {

                    data.addAll(datasourceProvider.getData(datasourceRequest));
                } catch (Exception e) {

                    DataEaseException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
                }

            } else {
                Datasource ds = engineService.getDeEngine();
                JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
                DatasourceRequest datasourceRequest = new DatasourceRequest();
                datasourceRequest.setDatasource(ds);
                String table = TableUtils.tableName(dataSetTableRequest.getId());
                QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
                datasourceRequest.setQuery(qp.createQueryTableWithPage(table, fields, page, pageSize, realSize, false, ds, null, rowPermissionsTree));
                map.put("sql", java.util.Base64.getEncoder().encodeToString(datasourceRequest.getQuery().getBytes()));
                try {
                    data.addAll(jdbcProvider.getData(datasourceRequest));
                } catch (Exception e) {

                    DataEaseException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
                }

            }
        } else if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "union")) {
            if (datasetTable.getMode() == 0) {
                Datasource ds = datasourceMapper.selectByPrimaryKey(dataSetTableRequest.getDataSourceId());
                if (ObjectUtils.isEmpty(ds)) {
                    DataEaseException.throwException(Translator.get("i18n_datasource_delete"));
                }
                Provider datasourceProvider = ProviderFactory.getProvider(ds.getType());
                DatasourceRequest datasourceRequest = new DatasourceRequest();
                datasourceRequest.setDatasource(ds);

                DataTableInfoDTO dt = new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class);

                String sql = "";
                try {
                    sql = (String) dataSetTableService.getUnionSQLDatasource(dt, ds).get("sql");
                } catch (Exception e) {

                    DataEaseException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
                }
                QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
                datasourceRequest.setQuery(qp.createQuerySQLWithPage(sql, fields, page, pageSize, realSize, false, null, rowPermissionsTree));
                map.put("sql", java.util.Base64.getEncoder().encodeToString(datasourceRequest.getQuery().getBytes()));

                try {

                    data.addAll(datasourceProvider.getData(datasourceRequest));
                } catch (Exception e) {

                    DataEaseException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
                }

            } else {
                Datasource ds = engineService.getDeEngine();
                JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
                DatasourceRequest datasourceRequest = new DatasourceRequest();
                datasourceRequest.setDatasource(ds);
                String table = TableUtils.tableName(dataSetTableRequest.getId());
                QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
                datasourceRequest.setQuery(qp.createQueryTableWithPage(table, fields, page, pageSize, realSize, false, ds, null, rowPermissionsTree));
                map.put("sql", java.util.Base64.getEncoder().encodeToString(datasourceRequest.getQuery().getBytes()));
                try {
                    data.addAll(jdbcProvider.getData(datasourceRequest));
                } catch (Exception e) {

                    DataEaseException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
                }


            }
        }

        List<Map<String, Object>> jsonArray = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(data)) {
            jsonArray = data.stream().map(ele -> {
                Map<String, Object> tmpMap = new HashMap<>();
                for (int i = 0; i < ele.length; i++) {
                    if (desensitizationList.keySet().contains(fieldArray[i])) {
                        tmpMap.put(fieldArray[i], ChartDataBuild.desensitizationValue(desensitizationList.get(fieldArray[i]), String.valueOf(ele[i])));
                    } else {
                        tmpMap.put(fieldArray[i], ele[i]);
                    }
                }
                return tmpMap;
            }).collect(Collectors.toList());
        }
        map.put("fields", fields);
        map.put("data", jsonArray);
        return map;
    }


}

