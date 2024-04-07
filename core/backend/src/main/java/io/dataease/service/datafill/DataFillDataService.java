package io.dataease.service.datafill;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import io.dataease.auth.service.AuthUserService;
import io.dataease.commons.utils.CommonBeanFactory;
import io.dataease.controller.request.datafill.DataFillFormTableDataRequest;
import io.dataease.controller.response.datafill.DataFillFormTableDataResponse;
import io.dataease.dto.datafill.DataFillCommitLogDTO;
import io.dataease.ext.ExtDataFillFormMapper;
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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.pentaho.di.core.util.UUIDUtil;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        DataFillFormWithBLOBs dataFillForm = dataFillFormMapper.selectByPrimaryKey(searchRequest.getId());

        if (StringUtils.equals(dataFillForm.getNodeType(), "folder")) {
            return null;
        }
        List<ExtTableField> fields = gson.fromJson(dataFillForm.getForms(), new TypeToken<List<ExtTableField>>() {
        }.getType());

        Datasource ds = datasource.get(dataFillForm.getDatasource());
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
        if (StringUtils.isNoneBlank(searchRequest.getPrimaryKeyValue())) {
            whereSql = extDDLProvider.whereSql(dataFillForm.getTableName(), List.of(pk));
        }

        String countSql = extDDLProvider.countSql(dataFillForm.getTableName(), searchFields, whereSql);
        if (StringUtils.isNoneBlank(searchRequest.getPrimaryKeyValue())) {
            datasourceRequest.setTableFieldWithValues(List.of(new DatasourceRequest.TableFieldWithValue()
                    .setValue(searchRequest.getPrimaryKeyValue())
                    .setFiledName(pk.getFieldName())
                    .setTypeName(pk.getFieldType())
                    .setType(pk.getType())));
        }
        datasourceRequest.setQuery(countSql);
        List<String[]> countData = datasourceProvider.getData(datasourceRequest);
        long count = NumberUtils.toLong(countData.get(0)[0]);

        String searchSql = extDDLProvider.searchSql(dataFillForm.getTableName(), searchFields, whereSql, searchRequest.getPageSize(), (searchRequest.getCurrentPage() - 1) * searchRequest.getPageSize());
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
        if (CollectionUtils.isNotEmpty(ids)) {
            list = extDataFillFormMapper.selectLatestLogByFormDataIds(dataFillForm.getId(), ids);
        }
        Map<String, DataFillCommitLogDTO> logMap = list.stream().collect(Collectors.toMap(log -> log.getFormId() + "__" + log.getDataId(), log -> log));

        for (Map<String, Object> object : result) {
            Map<String, Object> temp = new HashMap<>();
            temp.put("data", object);
            temp.put("logInfo", logMap.get(dataFillForm.getId() + "__" + object.get(key)));

            resultList.add(temp);
        }


        return new DataFillFormTableDataResponse()
                .setKey(key)
                .setData(resultList)
                .setFields(fields)
                .setTotal(count)
                .setPageSize(searchRequest.getPageSize())
                .setCurrentPage(searchRequest.getCurrentPage());
    }


    public void deleteRowData(String formId, String id) throws Exception {
        DataFillFormWithBLOBs dataFillForm = dataFillFormMapper.selectByPrimaryKey(formId);

        if (StringUtils.equals(dataFillForm.getNodeType(), "folder")) {
            return;
        }
        Datasource ds = datasource.get(dataFillForm.getDatasource());

        ExtDDLProvider extDDLProvider = ProviderFactory.gerExtDDLProvider(ds.getType());

        Provider datasourceProvider = ProviderFactory.getProvider(ds.getType());
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(ds);
        datasourceRequest.setTable(dataFillForm.getTableName());

        setLowerCaseRequest(ds, datasourceProvider, extDDLProvider, datasourceRequest);

        List<TableField> tableFields = datasourceProvider.getTableFields(datasourceRequest).stream().filter(TableField::isPrimaryKey).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(tableFields)) {
            throw new RuntimeException("没有主键");
        }
        TableField key = tableFields.get(0);

        DatasourceRequest.TableFieldWithValue pk = new DatasourceRequest.TableFieldWithValue()
                .setValue(id) //todo 有可能是数字
                .setFiledName(key.getFieldName())
                .setTypeName(key.getFieldType())
                .setType(key.getType());

        String deleteSql = extDDLProvider.deleteDataByIdSql(dataFillForm.getTableName(), pk);

        datasourceRequest.setQuery(deleteSql);
        datasourceRequest.setTableFieldWithValues(List.of(pk));

        JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
        jdbcProvider.execWithPreparedStatement(datasourceRequest);

        dataFillLogService.saveCommitOperation(DataFillLogService.COMMIT_OPERATE_DELETE, dataFillForm.getId(), id);

    }

    public String updateRowData(String formId, String rowId, Map<String, Object> data, boolean insert) throws Exception {
        if (!CommonBeanFactory.getBean(AuthUserService.class).pluginLoaded()) {
            DataEaseException.throwException("invalid");
        }

        if (rowId == null) {
            insert = true;
            //先默认主键是uuid
            rowId = UUIDUtil.getUUIDAsString();
        }

        DataFillFormWithBLOBs dataFillForm = dataFillFormMapper.selectByPrimaryKey(formId);

        List<ExtTableField> fields = gson.fromJson(dataFillForm.getForms(), new TypeToken<List<ExtTableField>>() {
        }.getType());

        Datasource ds = datasource.get(dataFillForm.getDatasource());
        Provider datasourceProvider = ProviderFactory.getProvider(ds.getType());
        ExtDDLProvider extDDLProvider = ProviderFactory.gerExtDDLProvider(ds.getType());

        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(ds);
        datasourceRequest.setTable(dataFillForm.getTableName());

        setLowerCaseRequest(ds, datasourceProvider, extDDLProvider, datasourceRequest);

        List<TableField> tableFields = datasourceProvider.getTableFields(datasourceRequest);

        Map<String, TableField> tableFieldMap = new HashMap<>();

        List<DatasourceRequest.TableFieldWithValue> searchFields = new ArrayList<>();
        List<DatasourceRequest.TableFieldWithValue> uniqueFields = new ArrayList<>();
        Map<String, ExtTableField> extTableFields = new HashMap<>();

        DatasourceRequest.TableFieldWithValue pk = null;
        for (TableField tableField : tableFields) {
            if (tableField.isPrimaryKey()) {
                pk = new DatasourceRequest.TableFieldWithValue()
                        .setValue(rowId)
                        .setFiledName(tableField.getFieldName())
                        .setTypeName(tableField.getFieldType())
                        .setType(tableField.getType());
                if (insert) {
                    searchFields.add(pk);
                }
                continue;
            }
            tableFieldMap.put(tableField.getFieldName(), tableField);
        }

        for (ExtTableField field : fields) {
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
                    extTableFields.put(name1, field);
                }
                if (tableFieldMap.containsKey(name2)) {
                    DatasourceRequest.TableFieldWithValue value2 = new DatasourceRequest.TableFieldWithValue()
                            .setValue(data.get(name2) != null ? new java.sql.Date((long) data.get(name2)) : null)
                            .setFiledName(name2)
                            .setTypeName(tableFieldMap.get(name2).getFieldType())
                            .setType(tableFieldMap.get(name2).getType());
                    searchFields.add(value2);
                    extTableFields.put(name2, field);
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

                    extTableFields.put(name, field);

                    // 关于unique的字段判断
                    if (field.getSettings().isUnique() && data.get(name) != null) {

                        uniqueFields.add(value);
                    }
                }
            }
        }

        if (CollectionUtils.isNotEmpty(uniqueFields)) {
            for (DatasourceRequest.TableFieldWithValue uniqueField : uniqueFields) {

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
                    DataEaseException.throwException(extTableFields.get(uniqueField.getFiledName()).getSettings().getName() + " 值不能重复");
                }
            }
        }

        String sql = insert ?
                extDDLProvider.insertDataSql(dataFillForm.getTableName(), searchFields) :
                extDDLProvider.updateDataByIdSql(dataFillForm.getTableName(), searchFields, pk);
        datasourceRequest.setQuery(sql);

        boolean skip = false;
        if (!insert && CollectionUtils.isEmpty(searchFields)) {
            //DataEaseException.throwException("没有修改");
            skip = true;
        }

        JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);

        // 主键在最后传进去
        if (!insert) {
            searchFields.add(pk);
        }
        datasourceRequest.setTableFieldWithValues(searchFields);

        if (!skip) {
            int result = jdbcProvider.execWithPreparedStatement(datasourceRequest);
        }

        if (insert) {
            dataFillLogService.saveCommitOperation(DataFillLogService.COMMIT_OPERATE_INSERT, dataFillForm.getId(), rowId);
        } else {
            dataFillLogService.saveCommitOperation(DataFillLogService.COMMIT_OPERATE_UPDATE, dataFillForm.getId(), rowId);
        }

        return rowId;
    }

}
