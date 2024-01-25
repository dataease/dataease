package io.dataease.service.chart;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.dataease.commons.model.PluginViewSetImpl;
import io.dataease.commons.utils.TableUtils;
import io.dataease.controller.request.chart.ChartExtRequest;
import io.dataease.dto.dataset.DataSetTableUnionDTO;
import io.dataease.dto.dataset.DataTableInfoDTO;
import io.dataease.plugins.common.base.domain.ChartViewWithBLOBs;
import io.dataease.plugins.common.base.domain.DatasetTableField;
import io.dataease.plugins.common.base.domain.Datasource;
import io.dataease.plugins.common.constants.DatasetType;
import io.dataease.plugins.common.constants.datasource.SQLConstants;
import io.dataease.plugins.common.dto.chart.ChartViewFieldDTO;
import io.dataease.plugins.common.dto.chart.ChartViewFieldFilterDTO;
import io.dataease.plugins.common.dto.sqlObj.SQLObj;
import io.dataease.plugins.common.request.chart.ChartExtFilterRequest;
import io.dataease.plugins.common.request.chart.filter.FilterTreeObj;
import io.dataease.plugins.common.request.permission.DataSetRowPermissionsTreeDTO;
import io.dataease.plugins.common.util.BeanUtils;
import io.dataease.plugins.common.util.ConstantsUtil;
import io.dataease.plugins.datasource.query.QueryProvider;
import io.dataease.plugins.view.entity.*;
import io.dataease.plugins.view.entity.filter.PluginFilterTreeObj;
import io.dataease.plugins.view.service.ViewPluginBaseService;
import io.dataease.provider.ProviderFactory;
import io.dataease.service.dataset.DataSetTableService;
import io.dataease.service.dataset.DataSetTableUnionService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.dataease.plugins.common.constants.datasource.SQLConstants.TABLE_ALIAS_PREFIX;

@Service
public class ViewPluginBaseServiceImpl implements ViewPluginBaseService {

    private static Gson gson = new Gson();

    @Resource
    private DataSetTableUnionService dataSetTableUnionService;


    @Resource
    private DataSetTableService dataSetTableService;

    @Resource
    private ChartViewService chartViewService;

    private static final Logger logger = LoggerFactory.getLogger(ViewPluginBaseServiceImpl.class);


    @Override
    public PluginSingleField buildField(String dsType, PluginViewField pluginViewField, PluginViewSQL tableObj, int index) {
        PluginSingleField result = new PluginSingleField();
        String FIELD_ALIAS_PREFIX = StringUtils.equals(pluginViewField.getTypeField(), "xAxis") ? SQLConstants.FIELD_ALIAS_X_PREFIX : SQLConstants.FIELD_ALIAS_Y_PREFIX;

        String originField = getOriginName(dsType, pluginViewField, tableObj);

        PluginViewSQL field;
        String where;
        String alias_fix = ConstantsUtil.constantsValue(dsType, "ALIAS_FIX");
        String fieldAlias = String.format(alias_fix, String.format(FIELD_ALIAS_PREFIX, index));

        field = getField(dsType, pluginViewField, originField, fieldAlias);
        where = getWhere(dsType, pluginViewField, originField, fieldAlias);
        PluginViewSQL sort = addSort(pluginViewField.getSort(), originField, fieldAlias);
        Optional.ofNullable(field).ifPresent(f -> result.setField(f));
        Optional.ofNullable(sort).ifPresent(s -> result.setSort(s));
        Optional.ofNullable(where).ifPresent(w -> result.setWhere(w));
        return result;
    }

    @Override
    public String customWhere(String dsType, PluginFilterTreeObj obj, PluginViewSQL pluginViewSQL) {
        QueryProvider queryProvider = ProviderFactory.getQueryProvider(dsType);
        String methodName = "transChartFilterTrees";
        SQLObj sqlObj = BeanUtils.copyBean(SQLObj.builder().build(), pluginViewSQL);
        FilterTreeObj filters = gson.fromJson(gson.toJson(obj), FilterTreeObj.class);
        Object o;
        if ((o = execProviderSuperMethod(queryProvider, methodName, sqlObj, filters)) != null) {
            return (String) o;
        }
        return null;
    }

    @Override
    public String panelWhere(String dsType, List<PluginChartExtFilter> list, PluginViewSQL pluginViewSQL) {
        QueryProvider queryProvider = ProviderFactory.getQueryProvider(dsType);
        String methodName = "transExtFilterList";
        SQLObj sqlObj = BeanUtils.copyBean(SQLObj.builder().build(), pluginViewSQL);
        List<ChartExtFilterRequest> filters = list.stream().map(item -> gson.fromJson(gson.toJson(item), ChartExtFilterRequest.class)).collect(Collectors.toList());
        Object o;
        if ((o = execProviderMethod(queryProvider, methodName, sqlObj, filters)) != null) {
            return (String) o;
        }
        return null;
    }

    @Override
    public String permissionWhere(String s, List<DataSetRowPermissionsTreeDTO> list, PluginViewSQL pluginViewSQL) {
        QueryProvider queryProvider = ProviderFactory.getQueryProvider(s);
        SQLObj sqlObj = BeanUtils.copyBean(SQLObj.builder().build(), pluginViewSQL);
        return queryProvider.transFilterTrees(sqlObj, list);
    }

    private String sqlFix(String sql) {
        if (sql.lastIndexOf(";") == (sql.length() - 1)) {
            sql = sql.substring(0, sql.length() - 1);
        }
        return sql;
    }

    @Override
    public PluginViewSQL getTableObj(PluginViewSet pluginViewSet) {
        String tableName = null;
        DataTableInfoDTO dataTableInfoDTO = new Gson().fromJson(pluginViewSet.getInfo(), DataTableInfoDTO.class);
        if (ObjectUtils.isNotEmpty(pluginViewSet.getMode()) && 1 == pluginViewSet.getMode()) {
            tableName = TableUtils.tableName(pluginViewSet.getTableId());
        } else {
            switch (DatasetType.getEnumObjByKey(pluginViewSet.getType())) {
                case DB:
                    tableName = dataTableInfoDTO.getTable();
                    break;
                case SQL:
                    String sql = dataTableInfoDTO.isBase64Encryption() ? new String(java.util.Base64.getDecoder().decode(dataTableInfoDTO.getSql())) : dataTableInfoDTO.getSql();
                    if (StringUtils.isNotBlank(pluginViewSet.getChartExtRequest())) {
                        ChartExtRequest chartExtRequest = gson.fromJson(pluginViewSet.getChartExtRequest(), ChartExtRequest.class);
                        sql = chartViewService.preHandleVariable(sql, chartExtRequest, ProviderFactory.getQueryProvider(pluginViewSet.getDsType()), dataSetTableService.getWithPermission(pluginViewSet.getTableId(), chartExtRequest.getUser()));
                    }
                    tableName = dataSetTableService.handleVariableDefaultValue(sql, null, pluginViewSet.getDsType(), false);
                    tableName = "(" + sqlFix(tableName) + ")";
                    break;
                case CUSTOM:
                    List<DataSetTableUnionDTO> list = dataSetTableUnionService.listByTableId(dataTableInfoDTO.getList().get(0).getTableId());
                    Datasource ds = new Datasource();
                    ds.setType(pluginViewSet.getDsType());
                    tableName = dataSetTableService.getCustomSQLDatasource(dataTableInfoDTO, list, ds);
                    break;
                case UNION:
                    Datasource datasource = ((PluginViewSetImpl) pluginViewSet).getDs();
                    Map<String, Object> sqlMap = dataSetTableService.getUnionSQLDatasource(dataTableInfoDTO, datasource);
                    tableName = "(" + ((String) sqlMap.get("sql")) + ")";
                    break;
                default:
                    tableName = dataTableInfoDTO.getTable();
                    break;
            }
        }
        String keyword = ConstantsUtil.constantsValue(pluginViewSet.getDsType(), "KEYWORD_TABLE");
        String realTableName = (tableName.startsWith("(") && tableName.endsWith(")")) ? tableName : String.format(keyword, tableName);
        String tableAlias = String.format(TABLE_ALIAS_PREFIX, 0);
        PluginViewSQL tableObj = PluginViewSQL.builder().tableName(realTableName).tableAlias(tableAlias).build();
        QueryProvider queryProvider = ProviderFactory.getQueryProvider(pluginViewSet.getDsType());
        SQLObj sqlObj = SQLObj.builder().tableName(realTableName).tableAlias(tableAlias).build();
        PluginViewSetImpl child = (PluginViewSetImpl) pluginViewSet;
        queryProvider.setSchema(sqlObj, child.getDs());
        tableObj.setTableName(sqlObj.getTableName());
        tableObj.setTableAlias(sqlObj.getTableAlias());
        return tableObj;
    }

    private String getOriginName(String dsType, PluginViewField pluginViewField, PluginViewSQL tableObj) {
        String keyword_fix = ConstantsUtil.constantsValue(dsType, "KEYWORD_FIX");
        String originField;
        String reflectField = reflectFieldName(dsType, pluginViewField);
        if (ObjectUtils.isNotEmpty(pluginViewField.getExtField()) && pluginViewField.getExtField() == 2) {
            originField = calcFieldRegex(dsType, pluginViewField.getOriginName(), tableObj);
        } else if (ObjectUtils.isNotEmpty(pluginViewField.getExtField()) && pluginViewField.getExtField() == 1) {
            originField = String.format(keyword_fix, tableObj.getTableAlias(), StringUtils.isNotBlank(reflectField) ? reflectField : pluginViewField.getOriginName());
        } else {
            originField = String.format(keyword_fix, tableObj.getTableAlias(), StringUtils.isNotBlank(reflectField) ? reflectField : pluginViewField.getOriginName());
        }
        return originField;
    }

    private String calcFieldRegex(String dsType, String originField, PluginViewSQL pluginViewSQL) {
        QueryProvider queryProvider = ProviderFactory.getQueryProvider(dsType);
        String methodName = "calcFieldRegex";
        SQLObj sqlObj = BeanUtils.copyBean(SQLObj.builder().build(), pluginViewSQL);
        Object o;
        if ((o = execProviderMethod(queryProvider, methodName, originField, sqlObj)) != null) {
            return (String) o;
        }
        return null;
    }

    private String reflectFieldName(String dsType, PluginViewField pluginViewField) {
        QueryProvider queryProvider = ProviderFactory.getQueryProvider(dsType);
        String methodName = "reflectFieldName";
        DatasetTableField field = BeanUtils.copyBean(new DatasetTableField(), pluginViewField);
        Object o;
        if ((o = execProviderMethod(queryProvider, methodName, field)) != null) {
            return (String) o;
        }
        return null;
    }


    private PluginViewSQL getField(String dsType, PluginViewField field, String originField, String fieldAlias) {
        QueryProvider queryProvider = ProviderFactory.getQueryProvider(dsType);
        String methodName;
        if (StringUtils.equals(field.getTypeField(), "xAxis") || StringUtils.equals(field.getTypeField(), "extStack")) {
            methodName = "getXFields";
        } else {
            methodName = "getYFields";
        }
        ChartViewFieldDTO chartViewFieldDTO = BeanUtils.copyBean(new ChartViewFieldDTO(), field);
        Object execResult;
        if ((execResult = execProviderMethod(queryProvider, methodName, chartViewFieldDTO, originField, fieldAlias)) != null) {
            SQLObj sqlObj = (SQLObj) execResult;
            PluginViewSQL result = PluginViewSQL.builder().build();
            return BeanUtils.copyBean(result, sqlObj);
        }
        return null;
    }

    private Object execProviderSuperMethod(QueryProvider queryProvider, String methodName, Object... args) {
        Method[] declaredMethods = queryProvider.getClass().getMethods();
        for (int i = 0; i < declaredMethods.length; i++) {
            Method method = declaredMethods[i];
            if (StringUtils.equals(method.getName(), methodName)) {
                method.setAccessible(true);
                return ReflectionUtils.invokeMethod(method, queryProvider, args);
            }
        }
        return null;
    }

    private Object execProviderMethod(QueryProvider queryProvider, String methodName, Object... args) {
        Method[] declaredMethods = queryProvider.getClass().getDeclaredMethods();
        for (Method method : declaredMethods) {
            if (StringUtils.equals(method.getName(), methodName)) {
                method.setAccessible(true);
                return ReflectionUtils.invokeMethod(method, queryProvider, args);
            }
        }
        return null;
    }


    private PluginViewSQL addSort(String sort, String originField, String fieldAlias) {
        if (StringUtils.isNotEmpty(sort) && !StringUtils.equalsIgnoreCase(sort, "none")) {
            return PluginViewSQL.builder().orderField(originField).orderAlias(fieldAlias).orderDirection(sort).build();
        }
        return null;
    }

    private String getWhere(String dsType, PluginViewField field, String originField, String fieldAlias) {
        QueryProvider queryProvider = ProviderFactory.getQueryProvider(dsType);
        String methodName;
        if (StringUtils.equals(field.getTypeField(), "xAxis") || StringUtils.equals(field.getTypeField(), "extStack")) {
            return null;

        } else {
            methodName = "getYWheres";
        }
        ChartViewFieldDTO chartViewFieldDTO = BeanUtils.copyBean(new ChartViewFieldDTO(), field);
        chartViewFieldDTO.setFilter(gson.fromJson(gson.toJson(field.getFilter()), new TypeToken<List<ChartViewFieldFilterDTO>>() {
        }.getType()));

        Object execResult;
        if ((execResult = execProviderMethod(queryProvider, methodName, chartViewFieldDTO, originField, fieldAlias)) != null) {
            String where = (String) execResult;
            return where;
        }
        return null;
    }

    @Override
    public String sqlLimit(String dsType, String sql, PluginViewLimit pluginViewLimit) {
        QueryProvider queryProvider = ProviderFactory.getQueryProvider(dsType);
        String methodName = "sqlLimit";
        ChartViewWithBLOBs chartView = new ChartViewWithBLOBs();
        chartView.setResultMode(pluginViewLimit.getResultMode());
        chartView.setResultCount(pluginViewLimit.getResultCount());
        Object result;
        if ((result = execProviderMethod(queryProvider, methodName, sql, chartView)) != null) {
            return result.toString();
        }
        return sql;
    }
}
