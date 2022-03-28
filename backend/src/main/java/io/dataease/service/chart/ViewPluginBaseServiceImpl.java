package io.dataease.service.chart;

import cn.hutool.core.util.ReflectUtil;
import com.google.gson.Gson;
import io.dataease.base.domain.DatasetTableField;
import io.dataease.base.domain.Datasource;
import io.dataease.controller.request.chart.ChartExtFilterRequest;
import io.dataease.dto.chart.ChartFieldCustomFilterDTO;
import io.dataease.dto.chart.ChartViewFieldDTO;
import io.dataease.dto.dataset.DataSetTableUnionDTO;
import io.dataease.dto.dataset.DataTableInfoDTO;
import io.dataease.dto.sqlObj.SQLObj;
import io.dataease.plugins.common.constants.SQLConstants;
import io.dataease.plugins.common.util.BeanUtils;
import io.dataease.plugins.common.util.ConstantsUtil;
import io.dataease.plugins.view.entity.*;
import io.dataease.plugins.view.service.ViewPluginBaseService;
import io.dataease.provider.ProviderFactory;
import io.dataease.provider.QueryProvider;
import io.dataease.service.dataset.DataSetTableService;
import io.dataease.service.dataset.DataSetTableUnionService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static io.dataease.plugins.common.constants.SQLConstants.TABLE_ALIAS_PREFIX;

@Service
public class ViewPluginBaseServiceImpl implements ViewPluginBaseService {

    private static Gson gson = new Gson();

    @Resource
    private DataSetTableUnionService dataSetTableUnionService;


    @Resource
    private DataSetTableService dataSetTableService;


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
    public String customWhere(String dsType, List<PluginChartFieldCustomFilter> list, PluginViewSQL pluginViewSQL) {
        QueryProvider queryProvider = ProviderFactory.getQueryProvider(dsType);
        String methodName = "transCustomFilterList";
        SQLObj sqlObj = BeanUtils.copyBean(SQLObj.builder().build(), pluginViewSQL);
        List<ChartFieldCustomFilterDTO> filters = list.stream().map(item -> gson.fromJson(gson.toJson(item), ChartFieldCustomFilterDTO.class)).collect(Collectors.toList());
        Object o ;
        if ((o = execProviderMethod(queryProvider, methodName, sqlObj, filters)) != null) {
            return (String)o;
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
            return (String)o;
        }
        return null;
    }

    @Override
    public PluginViewSQL getTableObj(PluginViewSet pluginViewSet) {
        String tableName = null;
        DataTableInfoDTO dataTableInfoDTO = new Gson().fromJson(pluginViewSet.getInfo(), DataTableInfoDTO.class);
        if (ObjectUtils.isNotEmpty(pluginViewSet.getMode()) && 1 == pluginViewSet.getMode()) {

            tableName = "ds_" + pluginViewSet.getTabelId().replaceAll("-", "_");

        }else {
            switch (pluginViewSet.getType()) {
                case "db":
                    tableName = dataTableInfoDTO.getTable();
                    break;
                case "sql":
                    tableName = dataTableInfoDTO.getSql();
                    break;
                case "custom":
                    List<DataSetTableUnionDTO> list = dataSetTableUnionService.listByTableId(dataTableInfoDTO.getList().get(0).getTableId());
                    Datasource ds = new Datasource();
                    ds.setType(pluginViewSet.getDsType());
                    tableName = dataSetTableService.getCustomSQLDatasource(dataTableInfoDTO, list, ds);
                    break;
                case "union":
                    Datasource datasource = new Datasource();
                    datasource.setType(pluginViewSet.getDsType());
                    Map<String, Object> sqlMap = dataSetTableService.getUnionSQLDatasource(dataTableInfoDTO, datasource);
                    tableName = (String) sqlMap.get("sql");
                    break;
                default:
                    tableName = dataTableInfoDTO.getTable();
                    break;
            }
        }
        String keyword = ConstantsUtil.constantsValue(pluginViewSet.getDsType(), "KEYWORD_TABLE");
        String tabelName = (tableName.startsWith("(") && tableName.endsWith(")")) ? tableName : String.format(keyword, tableName);
        String tabelAlias = String.format(TABLE_ALIAS_PREFIX, 0);
        PluginViewSQL tableObj = PluginViewSQL.builder().tableName(tabelName).tableAlias(tabelAlias).build();
        return tableObj;
    }

    private String getOriginName(String dsType, PluginViewField pluginViewField, PluginViewSQL tableObj) {
        String keyword_fix = ConstantsUtil.constantsValue(dsType, "KEYWORD_FIX");
        String originField;
        String reflectField =  reflectFieldName(dsType, pluginViewField);
        if (ObjectUtils.isNotEmpty(pluginViewField.getExtField()) && pluginViewField.getExtField() == 2) {
            originField = calcFieldRegex(dsType,pluginViewField.getOriginName(), tableObj);
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
        Object o ;
        if ((o = execProviderMethod(queryProvider, methodName, originField, sqlObj)) != null) {
            return (String)o;
        }
        return null;
    }

    private String reflectFieldName(String dsType, PluginViewField pluginViewField ) {
        QueryProvider queryProvider = ProviderFactory.getQueryProvider(dsType);
        String methodName = "reflectFieldName";
        DatasetTableField field = BeanUtils.copyBean(new DatasetTableField(), pluginViewField);;
        Object o ;
        if ((o = execProviderMethod(queryProvider, methodName, field)) != null) {
            return (String)o;
        }
        return null;
    }



    private PluginViewSQL getField(String dsType, PluginViewField field, String originField, String fieldAlias) {
        QueryProvider queryProvider = ProviderFactory.getQueryProvider(dsType);
        String methodName;
        if (StringUtils.equals(field.getTypeField(), "xAxis") || StringUtils.equals(field.getTypeField(), "extStack")) {
            methodName = "getXFields";
        }else {
            methodName = "getYFields";
        }
        ChartViewFieldDTO chartViewFieldDTO = BeanUtils.copyBean(new ChartViewFieldDTO(), field);
        Object execResult;
        if ((execResult = execProviderMethod(queryProvider, methodName, chartViewFieldDTO, originField, fieldAlias)) != null){
            SQLObj sqlObj = (SQLObj)execResult;
            PluginViewSQL result = PluginViewSQL.builder().build();
            return BeanUtils.copyBean(result, sqlObj);
        }
        return null;
    }

    private Object execProviderMethod(QueryProvider queryProvider, String methodName, Object... args) {
        Method[] declaredMethods = queryProvider.getClass().getDeclaredMethods();
        for (int i = 0; i < declaredMethods.length; i++) {
            Method method = declaredMethods[i];
            if (StringUtils.equals(method.getName(), methodName)) {
                method.setAccessible(true);
                return ReflectUtil.invoke(queryProvider, method, args);
            }
        }
        return null;
    }



    private PluginViewSQL addSort(String sort, String originField, String  fieldAlias) {
        if (StringUtils.isNotEmpty(sort) && !StringUtils.equalsIgnoreCase(sort, "none")) {
            return PluginViewSQL.builder().orderField(originField).orderAlias(fieldAlias).orderDirection(sort).build();
        }
        return null;
    }

    private String getWhere(String dsType, PluginViewField field,String originField,String fieldAlias) {
        QueryProvider queryProvider = ProviderFactory.getQueryProvider(dsType);
        String methodName;
        if (StringUtils.equals(field.getTypeField(), "xAxis") || StringUtils.equals(field.getTypeField(), "extStack")) {
            return null;

        }else {
            methodName = "getYWheres";
        }
        ChartViewFieldDTO chartViewFieldDTO = BeanUtils.copyBean(new ChartViewFieldDTO(), field);
        Object execResult;
        if ((execResult = execProviderMethod(queryProvider, methodName, chartViewFieldDTO, originField, fieldAlias)) != null){
            String where = (String)execResult;
            return where;
        }
        return null;
    }

}
