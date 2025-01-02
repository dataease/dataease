package io.dataease.service.datafill;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.CellData;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.dataease.auth.service.AuthUserService;
import io.dataease.commons.utils.CommonBeanFactory;
import io.dataease.controller.request.datafill.DataFillFormTableDataRequest;
import io.dataease.controller.response.datafill.DataFillFormTableDataResponse;
import io.dataease.dto.datafill.DataFillCommitLogDTO;
import io.dataease.dto.datasource.MysqlConfiguration;
import io.dataease.ext.ExtDataFillFormMapper;
import io.dataease.i18n.Translator;
import io.dataease.plugins.common.base.domain.DataFillFormWithBLOBs;
import io.dataease.plugins.common.base.domain.Datasource;
import io.dataease.plugins.common.base.mapper.DataFillFormMapper;
import io.dataease.plugins.common.constants.DatasourceTypes;
import io.dataease.plugins.common.dto.datafill.ExtTableField;
import io.dataease.plugins.common.dto.datasource.TableField;
import io.dataease.plugins.common.exception.DataEaseException;
import io.dataease.plugins.common.request.datasource.DatasourceRequest;
import io.dataease.plugins.datasource.provider.ExtDDLProvider;
import io.dataease.plugins.datasource.provider.Provider;
import io.dataease.plugins.datasource.provider.ProviderFactory;
import io.dataease.provider.datasource.JdbcProvider;
import io.dataease.service.datasource.DatasourceService;
import io.dataease.service.sys.SysAuthService;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.pentaho.di.core.util.UUIDUtil;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class DataFillDataService {

    @Resource
    private Environment env;
    @Resource
    private DataFillFormMapper dataFillFormMapper;
    @Resource
    private ExtDataFillFormMapper extDataFillFormMapper;
    @Resource
    private DatasourceService datasource;
    @Resource
    private SysAuthService sysAuthService;

    @Resource
    private DataFillLogService dataFillLogService;


    private final static Gson gson = new Gson();


    private Datasource getBuiltInDataSource() {
        MysqlConfiguration mysqlConfiguration = new MysqlConfiguration();
        Pattern WITH_SQL_FRAGMENT = Pattern.compile("jdbc:mysql://(.*):(\\d+)/(.*)");
        Matcher matcher = WITH_SQL_FRAGMENT.matcher(env.getProperty("spring.datasource.url"));
        if (!matcher.find()) {
            return null;
        }
        mysqlConfiguration.setHost(matcher.group(1));
        mysqlConfiguration.setPort(Integer.valueOf(matcher.group(2)));
        String[] databasePrams = matcher.group(3).split("\\?");
        mysqlConfiguration.setDataBase(databasePrams[0]);
        if (databasePrams.length == 2) {
            mysqlConfiguration.setExtraParams(databasePrams[1]);
        }
        if (StringUtils.isNotEmpty(mysqlConfiguration.getExtraParams()) && !mysqlConfiguration.getExtraParams().contains("connectionCollation")) {
            mysqlConfiguration.setExtraParams(mysqlConfiguration.getExtraParams() + "&connectionCollation=utf8mb4_general_ci");
        }
        mysqlConfiguration.setUsername(env.getProperty("spring.datasource.username"));
        mysqlConfiguration.setPassword(env.getProperty("spring.datasource.password"));

        Datasource datasource = new Datasource();
        datasource.setId("default-built-in");
        datasource.setType("mysql");
        datasource.setName(Translator.get("I18N_DATA_FILL_DATASOURCE_DEFAULT_BUILT_IN"));
        datasource.setConfiguration(new Gson().toJson(mysqlConfiguration));

        return datasource;
    }

    public Datasource getDataSource(String datasourceId) {
        return getDataSource(datasourceId, false);
    }

    public Datasource getDataSource(String datasourceId, boolean withCreatePrivileges) {
        Datasource ds = null;
        if (StringUtils.equals("default-built-in", datasourceId)) {
            ds = getBuiltInDataSource();
        } else {
            if (!withCreatePrivileges) {
                ds = datasource.get(datasourceId);
            } else {
                ds = datasource.getDataSourceDetails(datasourceId);
                if (ds.getEnableDataFill() && ds.getEnableDataFillCreateTable()) {
                    ds.setConfiguration(new String(java.util.Base64.getDecoder().decode(ds.getConfiguration())));
                } else {
                    return null;
                }
            }
        }

        if (ds == null) {
            DataEaseException.throwException(Translator.get("I18N_DATA_FILL_DATASOURCE_NOT_EXIST"));
        }
        return ds;
    }

    public static void setLowerCaseRequest(Datasource ds, Provider datasourceProvider, ExtDDLProvider extDDLProvider, DatasourceRequest datasourceRequest) throws Exception {
        DatasourceTypes datasourceType = DatasourceTypes.valueOf(ds.getType());
        switch (datasourceType) {
            case mysql:
            case mariadb:
                String checkLowerCaseSql = extDDLProvider.getLowerCaseTaleNames();
                datasourceRequest.setQuery(checkLowerCaseSql);
                List<String[]> checkLowerCaseData = datasourceProvider.getData(datasourceRequest);
                long lowCase = NumberUtils.toLong(checkLowerCaseData.get(0)[1]);
                datasourceRequest.setLowerCaseTaleNames(lowCase > 0);
                break;
            default:
                datasourceRequest.setLowerCaseTaleNames(true);
        }
    }

    public DataFillFormTableDataResponse listData(DataFillFormTableDataRequest searchRequest) throws Exception {
        return listData(searchRequest, true);
    }

    public DataFillFormTableDataResponse listData(DataFillFormTableDataRequest searchRequest, boolean withLogs) throws Exception {

        DataFillFormWithBLOBs dataFillForm = dataFillFormMapper.selectByPrimaryKey(searchRequest.getId());

        if (StringUtils.equals(dataFillForm.getNodeType(), "folder")) {
            return null;
        }
        List<ExtTableField> fields = gson.fromJson(dataFillForm.getForms(), new TypeToken<List<ExtTableField>>() {
        }.getType());

        Datasource ds = getDataSource(dataFillForm.getDatasource());
        Provider datasourceProvider = ProviderFactory.getProvider(ds.getType());

        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(ds);
        datasourceRequest.setTable(dataFillForm.getTableName());

        ExtDDLProvider extDDLProvider = ProviderFactory.gerExtDDLProvider(ds.getType());

        setLowerCaseRequest(ds, datasourceProvider, extDDLProvider, datasourceRequest);

        List<TableField> tableFields = datasourceProvider.getTableFields(datasourceRequest);
        Map<String, ExtTableField.BaseType> extTableFieldTypeMap = new HashMap<>();
        Map<String, TableField> tableFieldMap = new HashMap<>();
        List<TableField> searchFields = new ArrayList<>();

        String key = "";
        TableField pk = null;

        for (TableField tableField : tableFields) {
            if (tableField.isPrimaryKey()) {
                //先把ID放进来
                searchFields.add(tableField);
                key = tableField.getFieldName();
                pk = tableField;
            }
            tableFieldMap.put(tableField.getFieldName(), tableField);
        }

        //核对一下字段
        for (ExtTableField field : fields) {
            if (field.isRemoved()) {
                continue;
            }

            if (StringUtils.equalsIgnoreCase(field.getType(), "dateRange")) {
                String name1 = field.getSettings().getMapping().getColumnName1();
                extTableFieldTypeMap.put(name1, field.getSettings().getMapping().getType());
                TableField f1 = tableFieldMap.get(name1);
                if (f1 != null) {
                    //调整类型，给后面解析字段用
                    f1.setFieldType(field.getSettings().getMapping().getType().name());
                    searchFields.add(f1);
                }
                String name2 = field.getSettings().getMapping().getColumnName2();
                extTableFieldTypeMap.put(name2, field.getSettings().getMapping().getType());
                TableField f2 = tableFieldMap.get(name2);
                if (f2 != null) {
                    //调整类型，给后面解析字段用
                    f2.setFieldType(field.getSettings().getMapping().getType().name());
                    searchFields.add(f2);
                }
            } else {
                String name = field.getSettings().getMapping().getColumnName();
                extTableFieldTypeMap.put(name, field.getSettings().getMapping().getType());
                TableField f = tableFieldMap.get(field.getSettings().getMapping().getColumnName());
                if (f != null) {
                    //调整类型，给后面解析字段用
                    f.setFieldType(field.getSettings().getMapping().getType().name());
                    searchFields.add(f);
                }
            }
        }


        String whereSql = "";
        if (StringUtils.isNotBlank(searchRequest.getPrimaryKeyValue())) {
            whereSql = extDDLProvider.whereSql(dataFillForm.getTableName(), List.of(pk));
            datasourceRequest.setTableFieldWithValues(List.of(new DatasourceRequest.TableFieldWithValue()
                    .setValue(searchRequest.getPrimaryKeyValue())
                    .setFiledName(pk.getFieldName())
                    .setTypeName(pk.getFieldType())
                    .setType(pk.getType())));
        }

        if (CollectionUtils.isNotEmpty(searchRequest.getPrimaryKeyValueList())) {
            pk.setInCount(searchRequest.getPrimaryKeyValueList().size());
            whereSql = extDDLProvider.whereSql(dataFillForm.getTableName(), List.of(pk));
            List<DatasourceRequest.TableFieldWithValue> ids = new ArrayList<>();
            for (String s : searchRequest.getPrimaryKeyValueList()) {
                ids.add(new DatasourceRequest.TableFieldWithValue()
                        .setValue(s)
                        .setFiledName(pk.getFieldName())
                        .setTypeName(pk.getFieldType())
                        .setType(pk.getType()));
            }
            datasourceRequest.setTableFieldWithValues(ids);
        }

        String countSql = extDDLProvider.countSql(dataFillForm.getTableName(), searchFields, whereSql);

        datasourceRequest.setQuery(countSql);
        List<String[]> countData = datasourceProvider.getData(datasourceRequest);
        long count = NumberUtils.toLong(countData.get(0)[0]);

        long totalPage = searchRequest.getPageSize() <= 0 ? 1L : new BigDecimal(count).divide(new BigDecimal(searchRequest.getPageSize()), 0, RoundingMode.CEILING).longValue();

        long currentPage = totalPage < searchRequest.getCurrentPage() ? totalPage - 1 : searchRequest.getCurrentPage();

        if (currentPage < 1) {
            currentPage = 1;
        }

        String searchSql = extDDLProvider.searchSql(dataFillForm.getTableName(), searchFields, whereSql, searchRequest.getPageSize(), (currentPage - 1) * searchRequest.getPageSize());
        datasourceRequest.setQuery(searchSql);

        List<String[]> data2 = datasourceProvider.getData(datasourceRequest);
        List<Map<String, Object>> result = new ArrayList<>();
        List<Map<String, Object>> resultList = new ArrayList<>();

        List<String> ids = new ArrayList<>();

        for (String[] strings : data2) {
            Map<String, Object> object = new HashMap<>();
            for (int i = 0; i < searchFields.size(); i++) {
                TableField f = searchFields.get(i);
                String name = f.getFieldName();
                String data = strings[i];
                if (data == null) {
                    object.put(name, null);
                    continue;
                }

                if (StringUtils.equals(key, name)) {
                    ids.add(strings[i]);
                }

                ExtTableField.BaseType extFieldType = extTableFieldTypeMap.get(name);
                if (extFieldType != null) {
                    switch (extFieldType) {
                        case number:
                            object.put(name, NumberUtils.toLong(strings[i]));
                            break;
                        case decimal:
                            object.put(name, new BigDecimal(strings[i]));
                            break;
                        case datetime:
                            SimpleDateFormat sdf = new SimpleDateFormat(extDDLProvider.DEFAULT_DATE_FORMAT_STR);
                            object.put(name, sdf.parse(strings[i]));
                            break;
                        default:
                            object.put(name, strings[i]);
                            break;
                    }
                } else {
                    object.put(name, strings[i]);
                }
            }
            result.add(object);
        }


        List<DataFillCommitLogDTO> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(ids) && withLogs) {
            list = extDataFillFormMapper.selectLatestLogByFormDataIds(dataFillForm.getId(), ids);
        }
        Map<String, DataFillCommitLogDTO> logMap = new HashMap<>();
        for (DataFillCommitLogDTO log : list) {
            logMap.put(log.getFormId() + "__" + log.getDataId(), log);
        }

        if (withLogs) {
            for (Map<String, Object> object : result) {
                Map<String, Object> temp = new HashMap<>();
                temp.put("data", object);
                temp.put("logInfo", logMap.get(dataFillForm.getId() + "__" + object.get(key)));

                resultList.add(temp);
            }
        }


        return new DataFillFormTableDataResponse()
                .setKey(key)
                .setData(withLogs ? resultList : result)
                .setFields(fields)
                .setTotal(count)
                .setPageSize(searchRequest.getPageSize())
                .setCurrentPage(currentPage);
    }


    public void deleteRowData(String formId, String id) throws Exception {
        deleteRowData(formId, List.of(id));
    }

    public void deleteRowData(String formId, List<String> ids) throws Exception {
        DataFillFormWithBLOBs dataFillForm = dataFillFormMapper.selectByPrimaryKey(formId);

        if (StringUtils.equals(dataFillForm.getNodeType(), "folder")) {
            return;
        }
        Datasource ds = getDataSource(dataFillForm.getDatasource());

        ExtDDLProvider extDDLProvider = ProviderFactory.gerExtDDLProvider(ds.getType());

        Provider datasourceProvider = ProviderFactory.getProvider(ds.getType());
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(ds);
        datasourceRequest.setTable(dataFillForm.getTableName());

        setLowerCaseRequest(ds, datasourceProvider, extDDLProvider, datasourceRequest);

        List<TableField> tableFields = datasourceProvider.getTableFields(datasourceRequest).stream().filter(TableField::isPrimaryKey).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(tableFields)) {
            throw new RuntimeException(Translator.get("I18N_DATA_FILL_NO_PRIMARY_KEY"));
        }
        TableField key = tableFields.get(0);

        List<DatasourceRequest.TableFieldWithValue> pks = new ArrayList<>();
        for (String id : ids) {
            DatasourceRequest.TableFieldWithValue pk = new DatasourceRequest.TableFieldWithValue()
                    .setValue(id) //todo 有可能是数字
                    .setFiledName(key.getFieldName())
                    .setTypeName(key.getFieldType())
                    .setType(key.getType());
            pks.add(pk);
        }

        String deleteSql = extDDLProvider.deleteDataByIdsSql(dataFillForm.getTableName(), pks);

        datasourceRequest.setQuery(deleteSql);
        datasourceRequest.setTableFieldWithValues(pks);

        JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
        jdbcProvider.execWithPreparedStatement(datasourceRequest);

        for (String id : ids) {
            dataFillLogService.saveCommitOperation(DataFillLogService.COMMIT_OPERATE_DELETE, dataFillForm.getId(), id);
        }

    }

    public List<String> updateOrInsertRowData(String formId, List<RowDataDatum> datumList) throws Exception {
        if (!CommonBeanFactory.getBean(AuthUserService.class).pluginLoaded()) {
            DataEaseException.throwException("invalid");
        }

        DataFillFormWithBLOBs dataFillForm = dataFillFormMapper.selectByPrimaryKey(formId);

        List<String> results = new ArrayList<>();
        List<ExtTableField> fields = gson.fromJson(dataFillForm.getForms(), new TypeToken<List<ExtTableField>>() {
        }.getType());

        Datasource ds = getDataSource(dataFillForm.getDatasource());
        Provider datasourceProvider = ProviderFactory.getProvider(ds.getType());
        ExtDDLProvider extDDLProvider = ProviderFactory.gerExtDDLProvider(ds.getType());

        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(ds);
        datasourceRequest.setTable(dataFillForm.getTableName());

        setLowerCaseRequest(ds, datasourceProvider, extDDLProvider, datasourceRequest);

        List<TableField> tableFields = datasourceProvider.getTableFields(datasourceRequest);

        //实际的表内字段
        Map<String, TableField> tableFieldMap = new HashMap<>();
        DatasourceRequest.TableFieldWithValue pkField = null;
        for (TableField tableField : tableFields) {
            if (tableField.isPrimaryKey()) {
                pkField = new DatasourceRequest.TableFieldWithValue()
                        .setFiledName(tableField.getFieldName())
                        .setTypeName(tableField.getFieldType())
                        .setType(tableField.getType());
                continue;
            }
            tableFieldMap.put(tableField.getFieldName(), tableField);
        }

        List<RowDataDatum> insertList = new ArrayList<>();
        List<RowDataDatum> updateList = new ArrayList<>();

        Map<String, ExtTableField> extTableFields = new HashMap<>();

        Map<String, List<String>> uniqueMap = new HashMap<>();

        for (RowDataDatum row : datumList) {
            String rowId = row.getId();
            if (rowId == null) {
                //默认主键是uuid
                rowId = UUIDUtil.getUUIDAsString();

                //再设置回去
                row.setInsert(true);
                row.setId(rowId);
            }
            if (row.isInsert()) {
                insertList.add(row);
            } else {
                updateList.add(row);
            }

            Map<String, Object> data = row.getData();
            for (ExtTableField field : fields) {
                if (field.isRemoved()) {
                    continue;
                }

                String name = field.getSettings().getMapping().getColumnName();

                if (StringUtils.equalsIgnoreCase(field.getType(), "dateRange")) {
                    String name1 = field.getSettings().getMapping().getColumnName1();
                    String name2 = field.getSettings().getMapping().getColumnName2();
                    if (tableFieldMap.containsKey(name1)) {
                        extTableFields.put(name1, field);
                    }
                    if (tableFieldMap.containsKey(name2)) {
                        extTableFields.put(name2, field);
                    }
                } else {
                    extTableFields.put(name, field);
                }

                if (StringUtils.equalsIgnoreCase(field.getType(), "input")) { //input框支持unique
                    if (field.getSettings().isUnique() && data.get(name) != null) {
                        uniqueMap.putIfAbsent(name, new ArrayList<>());
                        if (uniqueMap.get(name).contains(data.get(name).toString())) {
                            //提前判断录入的数据有没有unique字段重复的
                            DataEaseException.throwException("[" + extTableFields.get(name).getSettings().getName() + "] 值: " + data.get(name) + " 不能重复");
                        } else {
                            uniqueMap.get(name).add(data.get(name).toString());
                        }
                    }
                }
            }
        }

        //进入数据库查询是否有重复
        for (RowDataDatum row : datumList) {
            boolean insert = row.isInsert();
            String rowId = row.getId();
            DatasourceRequest.TableFieldWithValue pk = gson.fromJson(gson.toJson(pkField), DatasourceRequest.TableFieldWithValue.class).setValue(rowId);
            Map<String, Object> data = row.getData();
            //一条条去判断
            for (ExtTableField field : fields) {
                if (field.isRemoved()) {
                    continue;
                }
                String name = field.getSettings().getMapping().getColumnName();

                if (!StringUtils.equalsIgnoreCase(field.getType(), "dateRange") && data.get(name) == null) {
                    if (field.getSettings().isRequired()) {
                        DataEaseException.throwException("[" + field.getSettings().getName() + "] 不能为空");
                    }
                } else if (StringUtils.equalsIgnoreCase(field.getType(), "dateRange")) {
                    if (field.getSettings().isRequired()) {
                        String name1 = field.getSettings().getMapping().getColumnName1();
                        String name2 = field.getSettings().getMapping().getColumnName2();
                        if (data.get(name1) == null || data.get(name2) == null) {
                            DataEaseException.throwException("[" + field.getSettings().getName() + "] 不能为空");
                        }
                    }
                }

                if (StringUtils.equalsIgnoreCase(field.getType(), "input")) { //input框支持unique
                    if (field.getSettings().isUnique() && data.get(name) != null) {
                        DatasourceRequest.TableFieldWithValue uniqueField = new DatasourceRequest.TableFieldWithValue()
                                .setValue(data.get(name))
                                .setFiledName(name)
                                .setTypeName(tableFieldMap.get(name).getFieldType())
                                .setType(tableFieldMap.get(name).getType());

                        String sql = extDDLProvider.checkUniqueValueSql(dataFillForm.getTableName(), uniqueField, insert ? null : pk);
                        datasourceRequest.setQuery(sql);
                        List<DatasourceRequest.TableFieldWithValue> fs = new ArrayList<>();
                        fs.add(uniqueField);
                        if (!insert) {
                            fs.add(pk);
                        }
                        datasourceRequest.setTableFieldWithValues(fs);
                        List<String[]> countData = datasourceProvider.getData(datasourceRequest);
                        long count = NumberUtils.toLong(countData.get(0)[0]);

                        if (count > 0) {
                            DataEaseException.throwException("[" + extTableFields.get(uniqueField.getFiledName()).getSettings().getName() + "] 值: " + data.get(name) + " 在数据库中已存在, 不能重复");
                        }

                    }
                }
            }
        }

        JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);

        for (RowDataDatum rowDataDatum : updateList) {
            String rowId = rowDataDatum.getId();
            Map<String, Object> data = rowDataDatum.getData();

            //只能一个个更新
            String result = updateRowData(rowId, data, pkField, fields, tableFieldMap, dataFillForm, datasourceRequest, jdbcProvider, extDDLProvider);
            results.add(result);
        }

        List<String> result = insertRowData(insertList, pkField, fields, tableFieldMap, dataFillForm, datasourceRequest, jdbcProvider, extDDLProvider);

        if (result != null) {
            results.addAll(result);
        }
        return results;
    }

    private List<String> insertRowData(List<RowDataDatum> insertList, DatasourceRequest.TableFieldWithValue pkField, List<ExtTableField> fields, Map<String, TableField> tableFieldMap, DataFillFormWithBLOBs dataFillForm, DatasourceRequest datasourceRequest, JdbcProvider jdbcProvider, ExtDDLProvider extDDLProvider) throws Exception {
        if (CollectionUtils.isEmpty(insertList)) {
            //批量插入
            return null;
        }
        List<DatasourceRequest.TableFieldWithValue> baseSearchFields = null;
        List<DatasourceRequest.TableFieldWithValue> allSearchFields = new ArrayList<>();

        List<String> ids = new ArrayList<>();
        for (RowDataDatum row : insertList) {
            String rowId = row.getId();
            ids.add(rowId);
            Map<String, Object> data = row.getData();

            List<DatasourceRequest.TableFieldWithValue> searchFields = new ArrayList<>();

            DatasourceRequest.TableFieldWithValue pk = gson.fromJson(gson.toJson(pkField), DatasourceRequest.TableFieldWithValue.class).setValue(rowId);
            searchFields.add(pk);

            for (ExtTableField field : fields) {
                if (field.isRemoved()) {
                    continue;
                }
                if (StringUtils.equalsIgnoreCase(field.getType(), "dateRange")) {
                    String name1 = field.getSettings().getMapping().getColumnName1();
                    String name2 = field.getSettings().getMapping().getColumnName2();
                    if (tableFieldMap.containsKey(name1)) {
                        DatasourceRequest.TableFieldWithValue value1 = new DatasourceRequest.TableFieldWithValue()
                                .setValue(data.get(name1) != null ? new java.sql.Date((long) data.get(name1)) : null)
                                .setFiledName(name1)
                                .setTypeName(tableFieldMap.get(name1).getFieldType())
                                .setType(tableFieldMap.get(name1).getType());
                        searchFields.add(value1);

                    }
                    if (tableFieldMap.containsKey(name2)) {
                        DatasourceRequest.TableFieldWithValue value2 = new DatasourceRequest.TableFieldWithValue()
                                .setValue(data.get(name2) != null ? new java.sql.Date((long) data.get(name2)) : null)
                                .setFiledName(name2)
                                .setTypeName(tableFieldMap.get(name2).getFieldType())
                                .setType(tableFieldMap.get(name2).getType());
                        searchFields.add(value2);

                    }
                } else {
                    String name = field.getSettings().getMapping().getColumnName();
                    if (tableFieldMap.containsKey(name)) {
                        DatasourceRequest.TableFieldWithValue value = new DatasourceRequest.TableFieldWithValue()
                                .setValue(data.get(name))
                                .setFiledName(name)
                                .setTypeName(tableFieldMap.get(name).getFieldType())
                                .setType(tableFieldMap.get(name).getType());

                        if (StringUtils.equalsIgnoreCase(field.getType(), "date")) {
                            value.setValue(data.get(name) != null ? new java.sql.Date((long) data.get(name)) : null);
                        }
                        searchFields.add(value);

                    }
                }
            }

            if (baseSearchFields == null) {
                baseSearchFields = searchFields;
            }
            allSearchFields.addAll(searchFields);

        }

        String sql = extDDLProvider.insertDataSql(dataFillForm.getTableName(), baseSearchFields, insertList.size());
        datasourceRequest.setQuery(sql);


        datasourceRequest.setTableFieldWithValues(allSearchFields);


        int result = jdbcProvider.execWithPreparedStatement(datasourceRequest);

        dataFillLogService.saveCommitOperations(DataFillLogService.COMMIT_OPERATE_INSERT, dataFillForm.getId(), ids);

        return ids;
    }

    private String updateRowData(String rowId, Map<String, Object> data, DatasourceRequest.TableFieldWithValue pkField, List<ExtTableField> fields, Map<String, TableField> tableFieldMap, DataFillFormWithBLOBs dataFillForm, DatasourceRequest datasourceRequest, JdbcProvider jdbcProvider, ExtDDLProvider extDDLProvider) throws Exception {
        List<DatasourceRequest.TableFieldWithValue> searchFields = new ArrayList<>();

        DatasourceRequest.TableFieldWithValue pk = gson.fromJson(gson.toJson(pkField), DatasourceRequest.TableFieldWithValue.class).setValue(rowId);

        for (ExtTableField field : fields) {
            if (field.isRemoved()) {
                continue;
            }
            if (StringUtils.equalsIgnoreCase(field.getType(), "dateRange")) {
                String name1 = field.getSettings().getMapping().getColumnName1();
                String name2 = field.getSettings().getMapping().getColumnName2();
                if (tableFieldMap.containsKey(name1)) {
                    DatasourceRequest.TableFieldWithValue value1 = new DatasourceRequest.TableFieldWithValue()
                            .setValue(data.get(name1) != null ? new java.sql.Date((long) data.get(name1)) : null)
                            .setFiledName(name1)
                            .setTypeName(tableFieldMap.get(name1).getFieldType())
                            .setType(tableFieldMap.get(name1).getType());
                    searchFields.add(value1);
                }
                if (tableFieldMap.containsKey(name2)) {
                    DatasourceRequest.TableFieldWithValue value2 = new DatasourceRequest.TableFieldWithValue()
                            .setValue(data.get(name2) != null ? new java.sql.Date((long) data.get(name2)) : null)
                            .setFiledName(name2)
                            .setTypeName(tableFieldMap.get(name2).getFieldType())
                            .setType(tableFieldMap.get(name2).getType());
                    searchFields.add(value2);
                }
            } else {
                String name = field.getSettings().getMapping().getColumnName();
                if (tableFieldMap.containsKey(name)) {
                    DatasourceRequest.TableFieldWithValue value = new DatasourceRequest.TableFieldWithValue()
                            .setValue(data.get(name))
                            .setFiledName(name)
                            .setTypeName(tableFieldMap.get(name).getFieldType())
                            .setType(tableFieldMap.get(name).getType());

                    if (StringUtils.equalsIgnoreCase(field.getType(), "date")) {
                        value.setValue(data.get(name) != null ? new java.sql.Date((long) data.get(name)) : null);
                    }
                    searchFields.add(value);

                }
            }
        }

        String sql = extDDLProvider.updateDataByIdSql(dataFillForm.getTableName(), searchFields, pk);
        datasourceRequest.setQuery(sql);

        boolean skip = false;
        if (CollectionUtils.isEmpty(searchFields)) {
            //DataEaseException.throwException("没有修改");
            skip = true;
        }

        // 主键在最后传进去
        searchFields.add(pk);
        datasourceRequest.setTableFieldWithValues(searchFields);

        if (!skip) {
            int result = jdbcProvider.execWithPreparedStatement(datasourceRequest);
        }

        dataFillLogService.saveCommitOperation(DataFillLogService.COMMIT_OPERATE_UPDATE, dataFillForm.getId(), rowId);

        return rowId;
    }

    public List<ExtTableField.Option> listColumnData(String optionDatasource, String optionTable, String optionColumn, String optionOrder) throws Exception {
        return listColumnData(optionDatasource, optionTable, optionColumn, optionOrder, false);
    }

    public List<ExtTableField.Option> listColumnData(String optionDatasource, String optionTable, String optionColumn, String optionOrder, boolean limit) throws Exception {
        return listColumnData(optionDatasource, optionTable, optionColumn, optionOrder, null, limit);
    }

    public List<ExtTableField.Option> listColumnData(String optionDatasource, String optionTable, String optionColumn, String optionOrder, String query, boolean limit) throws Exception {
        Datasource ds = datasource.get(optionDatasource);
        Provider datasourceProvider = ProviderFactory.getProvider(ds.getType());

        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(ds);

        ExtDDLProvider extDDLProvider = ProviderFactory.gerExtDDLProvider(ds.getType());

        String sql = extDDLProvider.searchColumnData(optionTable, optionColumn, optionOrder, query, limit);
        System.out.println(sql);

        datasourceRequest.setQuery(sql);

        List<String[]> data = datasourceProvider.getData(datasourceRequest);

        List<ExtTableField.Option> result = new ArrayList<>();
        for (String[] datum : data) {
            ExtTableField.Option option = new ExtTableField.Option();
            if (StringUtils.isBlank(datum[0])) {
                continue;
            }
            option.setName(datum[0]);
            option.setValue(datum[0]);
            result.add(option);
        }

        return result;
    }

    @Data
    public static class ExcelDataListener extends AnalysisEventListener<Map<Integer, String>> {
        private List<List<String>> data = new ArrayList<>();
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
                    value = "";
                }
                line.add(value);
            }
            ;
            int size = line.size();
            if (size < header.size()) {
                for (int i = 0; i < header.size() - size; i++) {
                    line.add("");
                }
            }
            data.add(line);
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        }

        public void clear() {
            data.clear();
            header.clear();
        }
    }

    public void importExcelData(MultipartFile file, String formId) throws Exception {
        if (!CommonBeanFactory.getBean(AuthUserService.class).pluginLoaded()) {
            DataEaseException.throwException("invalid");
        }

        DataFillFormWithBLOBs dataFillForm = dataFillFormMapper.selectByPrimaryKey(formId);

        List<ExtTableField> formFields = gson.fromJson(dataFillForm.getForms(), new TypeToken<List<ExtTableField>>() {
        }.getType());

        List<ExtTableField> fields = new ArrayList<>();
        Map<String, String> dateRangeNameMap = new HashMap<>();
        for (ExtTableField field : formFields) {
            if (field.isRemoved()) {
                continue;
            }
            if (StringUtils.equalsIgnoreCase(field.getType(), "dateRange")) {
                dateRangeNameMap.put(field.getId(), field.getSettings().getName());

                ExtTableField start = gson.fromJson(gson.toJson(field), ExtTableField.class);
                start.getSettings().getMapping().setColumnName(start.getSettings().getMapping().getColumnName1());
                start.getSettings().setName(start.getSettings().getName() + "（开始）");
                fields.add(start);

                ExtTableField end = gson.fromJson(gson.toJson(field), ExtTableField.class);
                end.getSettings().setName(end.getSettings().getName() + "（结束）");
                end.getSettings().getMapping().setColumnName(end.getSettings().getMapping().getColumnName2());
                fields.add(end);
            } else {
                fields.add(field);
            }
        }

        ExcelDataListener listener = new ExcelDataListener();
        ExcelReader excelReader = EasyExcel.read(file.getInputStream(), listener).build();
        List<ReadSheet> sheets = excelReader.excelExecutor().sheetList();
        excelReader.read(sheets.get(0));

        List<String> excelHeaders = listener.getHeader();
        List<List<String>> excelData = listener.getData();

        if (excelHeaders.size() != fields.size()) {
            DataEaseException.throwException("模板字段个数不匹配");
        }

        List<RowDataDatum> dataList = new ArrayList<>();
        for (List<String> excelDatum : excelData) {
            Map<String, Object> rowData = new HashMap<>();
            Map<String, List<Long>> dateRangeCheckMap = new HashMap<>();
            for (String key : dateRangeNameMap.keySet()) {
                //初始化
                List<Long> list = new ArrayList<>();
                list.add(null);
                list.add(null);
                dateRangeCheckMap.put(key, list);
            }

            for (int i = 0; i < fields.size(); i++) {
                ExtTableField field = fields.get(i);

                String excelRowData = null;
                if (i < excelDatum.size()) {
                    excelRowData = excelDatum.get(i);
                }
                if (StringUtils.isBlank(excelRowData)) { //处理必填
                    excelRowData = null;
                    if (field.getSettings().isRequired()) {
                        DataEaseException.throwException("[" + field.getSettings().getName() + "] 不能为空");
                    }
                }
                if (excelRowData == null) {
                    continue;
                }
                try {
                    switch (field.getSettings().getMapping().getType()) {
                        case decimal:
                            BigDecimal decimalValue = new BigDecimal(excelRowData);
                            rowData.put(field.getSettings().getMapping().getColumnName(), decimalValue);
                            break;
                        case number:
                            BigDecimal longValue = new BigDecimal(excelRowData);
                            rowData.put(field.getSettings().getMapping().getColumnName(), longValue.longValue());
                            break;
                        case datetime:
                            Date date = getDate(field, excelRowData);
                            Long time = date == null ? null : date.getTime();
                            if (time != null && time < 0) {
                                throw new Exception("时间不能小于1970/01/01");
                            }
                            //检测dateRange下，开始要小于结束，且必须两个都有或都没有
                            if (StringUtils.equalsIgnoreCase(field.getType(), "dateRange")) {
                                if (StringUtils.equals(field.getSettings().getMapping().getColumnName(), field.getSettings().getMapping().getColumnName1())) {
                                    dateRangeCheckMap.get(field.getId()).set(0, time);
                                } else if (StringUtils.equals(field.getSettings().getMapping().getColumnName(), field.getSettings().getMapping().getColumnName2())) {
                                    dateRangeCheckMap.get(field.getId()).set(1, time);
                                }
                            }
                            rowData.put(field.getSettings().getMapping().getColumnName(), time);
                            break;
                        default:
                            if (StringUtils.equalsIgnoreCase(field.getType(), "select") && !field.getSettings().isMultiple() || StringUtils.equalsIgnoreCase(field.getType(), "radio")) {
                                boolean has = false;
                                List<ExtTableField.Option> options = field.getSettings().getOptions();
                                if (field.getSettings().getOptionSourceType() == 2
                                        && StringUtils.isNotBlank(field.getSettings().getOptionDatasource())
                                        && StringUtils.isNotBlank(field.getSettings().getOptionTable())
                                        && StringUtils.isNotBlank(field.getSettings().getOptionColumn())
                                ) {
                                    options = listColumnData(field.getSettings().getOptionDatasource(), field.getSettings().getOptionTable(), field.getSettings().getOptionColumn(), field.getSettings().getOptionOrder());
                                }

                                for (ExtTableField.Option option : options) {
                                    if (StringUtils.equals((String) option.getValue(), excelRowData)) {
                                        has = true;
                                        break;
                                    }
                                }
                                if (!has) {
                                    DataEaseException.throwException("[" + field.getSettings().getName() + "] 值: " + excelRowData + " 不在范围内");
                                }
                                rowData.put(field.getSettings().getMapping().getColumnName(), excelRowData);
                            } else if (StringUtils.equalsIgnoreCase(field.getType(), "checkbox") ||
                                    StringUtils.equalsIgnoreCase(field.getType(), "select") && field.getSettings().isMultiple()) {
                                Set<String> list = new HashSet<>();
                                String[] strArr = excelRowData.split(";");
                                for (String str : strArr) {
                                    if (StringUtils.isNotBlank(str)) {
                                        list.add(str);
                                    }
                                }
                                if (field.getSettings().isRequired()) {
                                    if (CollectionUtils.isEmpty(list)) {
                                        DataEaseException.throwException("[" + field.getSettings().getName() + "] 不能为空");
                                    }
                                }

                                List<String> result = new ArrayList<>();
                                if (CollectionUtils.isNotEmpty(list)) {

                                    List<ExtTableField.Option> options = field.getSettings().getOptions();
                                    if (field.getSettings().getOptionSourceType() == 2
                                            && StringUtils.isNotBlank(field.getSettings().getOptionDatasource())
                                            && StringUtils.isNotBlank(field.getSettings().getOptionTable())
                                            && StringUtils.isNotBlank(field.getSettings().getOptionColumn())
                                    ) {
                                        options = listColumnData(field.getSettings().getOptionDatasource(), field.getSettings().getOptionTable(), field.getSettings().getOptionColumn(), field.getSettings().getOptionOrder());
                                    }

                                    for (String str : list) {
                                        boolean has = false;
                                        for (ExtTableField.Option option : options) {
                                            if (StringUtils.equals((String) option.getValue(), str)) {
                                                has = true;
                                                break;
                                            }
                                        }
                                        if (has) {
                                            result.add(str);
                                        } else {
                                            DataEaseException.throwException("[" + field.getSettings().getName() + "] 输入值[" + str + "]不在范围内");
                                        }
                                    }
                                    if (CollectionUtils.isEmpty(result)) {
                                        DataEaseException.throwException("[" + field.getSettings().getName() + "] 输入值不在范围内");
                                    }
                                }

                                rowData.put(field.getSettings().getMapping().getColumnName(), gson.toJson(result));
                            } else {
                                //校验手机号，校验邮箱格式
                                if (StringUtils.equalsAnyIgnoreCase(field.getSettings().getInputType(), "tel")) {
                                    if (!excelRowData.matches("^1[3|4|5|7|8][0-9]{9}$")) {
                                        throw new Exception(Translator.get("i18n_wrong_tel"));
                                    }
                                }
                                if (StringUtils.equalsAnyIgnoreCase(field.getSettings().getInputType(), "email")) {
                                    if (!excelRowData.matches("^[a-zA-Z0-9_._-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$")) {
                                        throw new Exception(Translator.get("i18n_wrong_email"));
                                    }
                                }

                                rowData.put(field.getSettings().getMapping().getColumnName(), excelRowData);
                            }
                    }

                } catch (DataEaseException e) {
                    DataEaseException.throwException(e.getMessage());
                } catch (Exception e) {
                    DataEaseException.throwException("[" + field.getSettings().getName() + "] 值: " + excelRowData + " 格式解析错误: " + e.getMessage());
                }
            }

            //判断时间范围的开始结束是否符合要求
            dateRangeCheckMap.forEach((key, list) -> {
                if (list.get(0) == null && list.get(1) != null) {
                    DataEaseException.throwException("[" + dateRangeNameMap.get(key) + "（开始/结束）] 不能只有一个为空");
                } else if (list.get(0) != null && list.get(1) == null) {
                    DataEaseException.throwException("[" + dateRangeNameMap.get(key) + "（结束/结束）] 不能只有一个为空");
                } else if (list.get(0) != null && list.get(1) != null) {
                    if (list.get(0) > list.get(1)) {
                        DataEaseException.throwException("[" + dateRangeNameMap.get(key) + "（结束）] 不能早于 [" + dateRangeNameMap.get(key) + "（开始）]");
                    }
                }
            });

            if (rowData.isEmpty()) {
                continue;
            }

            dataList.add(new RowDataDatum().setData(rowData));
        }

        List<RowDataDatum> valueList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(dataList)) {

            //表单提交模式为更新时，需要根据条件加上主键id
            boolean updateMode = false;
            List<ExtTableField> fieldsForCheck = new ArrayList<>();
            if (BooleanUtils.isTrue(dataFillForm.getCommitNewUpdate())) {
                //这里把所有数据都拿出来遍历真的合适吗？
                for (ExtTableField field : fields) {
                    if (field.getSettings().isUpdateRuleCheck()) {
                        updateMode = true;
                        fieldsForCheck.add(field);
                    }
                }
            }

            if (!updateMode) {
                valueList = new ArrayList<>(dataList);
            } else {
                DataFillFormTableDataRequest req = new DataFillFormTableDataRequest();
                req.setId(dataFillForm.getId());
                DataFillFormTableDataResponse res = listData(req, false);
                List<Map<String, Object>> searchData = new ArrayList<>();
                String key = "";

                if (res != null && res.getData() != null) {
                    searchData = (List<Map<String, Object>>) res.getData();

                    Datasource ds = getDataSource(dataFillForm.getDatasource());
                    DatasourceRequest datasourceRequest = new DatasourceRequest();
                    datasourceRequest.setDatasource(ds);
                    datasourceRequest.setTable(dataFillForm.getTableName());
                    Provider datasourceProvider = ProviderFactory.getProvider(ds.getType());

                    ExtDDLProvider extDDLProvider = ProviderFactory.gerExtDDLProvider(ds.getType());
                    setLowerCaseRequest(ds, datasourceProvider, extDDLProvider, datasourceRequest);

                    List<TableField> tableFields = datasourceProvider.getTableFields(datasourceRequest);

                    for (TableField tableField : tableFields) {
                        if (tableField.isPrimaryKey()) {
                            //先把ID放进来
                            key = tableField.getFieldName();
                        }
                    }
                }

                for (RowDataDatum datum : dataList) {
                    boolean hasUpdate = false;
                    for (Map<String, Object> searchDatum : searchData) {
                        if (processValueList(valueList, key, datum, searchDatum, fieldsForCheck)) {
                            hasUpdate = true;
                        }
                    }
                    if (!hasUpdate) {
                        RowDataDatum row = new RowDataDatum()
                                .setInsert(true)
                                .setData(new HashMap<>(datum.getData()));
                        valueList.add(row);
                    }
                }

            }

            updateOrInsertRowData(formId, valueList);

        }

    }

    public static boolean processValueList(List<RowDataDatum> valueList, String key, RowDataDatum datum, Map<String, Object> searchDatum, List<ExtTableField> fieldsForCheck) {
        int count = 0;
        for (ExtTableField field : fieldsForCheck) {
            if (StringUtils.equalsIgnoreCase(field.getType(), "checkbox") ||
                    StringUtils.equalsIgnoreCase(field.getType(), "select") && field.getSettings().isMultiple()) {
                List<String> data1 = gson.fromJson((String) datum.getData().get(field.getSettings().getMapping().getColumnName()), new TypeToken<List<String>>() {
                }.getType());
                List<String> data2 = gson.fromJson((String) searchDatum.get(field.getSettings().getMapping().getColumnName()), new TypeToken<List<String>>() {
                }.getType());
                if (data1 == data2) {
                    count++;
                    continue;
                }
                if (data1 != null && data2 != null) {
                    if (data1.size() != data2.size()) {
                        continue;
                    }
                    if (CollectionUtils.isEqualCollection(data1, data2)) {
                        count++;
                    }
                }
            } else {
                if (datum.getData() != null && searchDatum != null) {
                    Object data1 = datum.getData().get(field.getSettings().getMapping().getColumnName());
                    Object data2 = searchDatum.get(field.getSettings().getMapping().getColumnName());
                    if (data1 == data2) {
                        count++;
                        continue;
                    }
                    if (data1 != null && data2 != null) {
                        if (data1.equals(data2)) {
                            count++;
                            continue;
                        }
                        switch (field.getSettings().getMapping().getType()) {
                            case number:
                                if (((Long) data1).longValue() == ((Long) data2).longValue()) {
                                    count++;
                                }
                                continue;
                            case decimal:
                                if (((BigDecimal) data1).compareTo((BigDecimal) data2) == 0) {
                                    count++;
                                }
                                continue;
                            case datetime:
                                if ((Long) data1 == ((Date) data2).getTime()) {
                                    count++;
                                }
                                continue;
                            default:
                                if (((String) data1).equals((String) data2)) {
                                    count++;
                                }
                        }

                    }
                }
            }
        }
        if (count == fieldsForCheck.size()) {
            RowDataDatum row = new RowDataDatum()
                    .setId((String) searchDatum.get(key))
                    .setInsert(datum.isInsert())
                    .setData(new HashMap<>(datum.getData()));
            valueList.add(row);
            return true;
        }
        return false;
    }

    private static Date getDate(ExtTableField field, String excelRowData) throws ParseException {
        if (StringUtils.isBlank(excelRowData)) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); //默认会拿到这种格式的
        switch (field.getSettings().getDateType()) {
            case "year":
                sdf = new SimpleDateFormat("yyyy");
                break;
            case "month":
            case "monthrange":
                sdf = new SimpleDateFormat("yyyy-MM");
                break;
            case "datetime":
            case "datetimerange":
                sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                break;
            case "date":
            case "daterange":
                sdf = new SimpleDateFormat("yyyy-MM-dd");
                break;
            default:
                if (field.getSettings().isEnableTime()) { //兼容旧版
                    sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                }
        }

        Date date = null;
        try {
            date = sdf.parse(excelRowData);
        } catch (ParseException e) {
            sdf = new SimpleDateFormat("yyyy/MM/dd"); //以防万一也加上这种
            switch (field.getSettings().getDateType()) {
                case "year":
                    sdf = new SimpleDateFormat("yyyy");
                    break;
                case "month":
                case "monthrange":
                    sdf = new SimpleDateFormat("yyyy/MM");
                    break;
                case "datetime":
                case "datetimerange":
                    sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    break;
                case "date":
                case "daterange":
                    sdf = new SimpleDateFormat("yyyy/MM/dd");
                    break;
                default:
                    if (field.getSettings().isEnableTime()) { //兼容旧版
                        sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    }
            }
            date = sdf.parse(excelRowData);
        }
        return date;
    }

}
