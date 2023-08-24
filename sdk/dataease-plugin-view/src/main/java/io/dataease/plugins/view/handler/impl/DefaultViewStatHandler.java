package io.dataease.plugins.view.handler.impl;

import io.dataease.plugins.common.constants.datasource.SQLConstants;
import io.dataease.plugins.common.request.permission.DataSetRowPermissionsTreeDTO;
import io.dataease.plugins.common.util.ConstantsUtil;
import io.dataease.plugins.view.entity.*;
import io.dataease.plugins.view.handler.PluginViewStatHandler;
import io.dataease.plugins.view.service.ViewPluginBaseService;
import io.dataease.plugins.view.service.ViewPluginService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;
import java.util.*;
import java.util.stream.Collectors;

public class DefaultViewStatHandler implements PluginViewStatHandler {

    @Override
    public String build(PluginViewParam pluginViewParam, ViewPluginService viewPluginService) {
        ViewPluginBaseService baseService = viewPluginService.getBaseService();
        PluginViewSet pluginViewSet = pluginViewParam.getPluginViewSet();
        List<DataSetRowPermissionsTreeDTO> rowPermissionsTree = pluginViewParam.getRowPermissionsTree();
        String dsType = pluginViewSet.getDsType();
        PluginViewSQL tableObj = baseService.getTableObj(pluginViewSet);

        Map<String, List<PluginSingleField>> fieldSQLMap = new HashMap<>();



        for (int i = 0; i < pluginViewParam.getPluginViewFields().size(); i++) {
            PluginViewField pluginViewField = pluginViewParam.getPluginViewFields().get(i);
            String typeKey = pluginViewField.getTypeField();
            PluginSingleField pluginSingleField = baseService.buildField(dsType, pluginViewField, tableObj, i);
            List<PluginSingleField> lists = fieldSQLMap.getOrDefault(typeKey, new ArrayList<>());
            lists.add(pluginSingleField);
            fieldSQLMap.put(typeKey, lists);
        }

        List<PluginViewSQL> xFields = fieldSQLMap.get("xAxis").stream().filter(singleField -> ObjectUtils.isNotEmpty(singleField.getField())).map(singleField -> singleField.getField()).collect(Collectors.toList());
        List<PluginViewSQL> xOrders = fieldSQLMap.get("xAxis").stream().filter(singleField -> ObjectUtils.isNotEmpty(singleField.getSort())).map(singleField -> singleField.getSort()).collect(Collectors.toList());
        // List<String> xWheres = fieldSQLMap.get("xAxis").stream().map(singleField -> singleField.getWhere()).collect(Collectors.toList());

        List<PluginViewSQL> yFields = fieldSQLMap.get("yAxis").stream().filter(singleField -> ObjectUtils.isNotEmpty(singleField.getField())).map(singleField -> singleField.getField()).collect(Collectors.toList());
        List<PluginViewSQL> yOrders = fieldSQLMap.get("yAxis").stream().filter(singleField -> ObjectUtils.isNotEmpty(singleField.getSort())).map(singleField -> singleField.getSort()).collect(Collectors.toList());
        List<String> yWheres = fieldSQLMap.get("yAxis").stream().filter(singleField -> ObjectUtils.isNotEmpty(singleField.getWhere())).map(singleField -> singleField.getWhere()).collect(Collectors.toList());


        // 处理视图中字段过滤
        String customWheres = baseService.customWhere(dsType, pluginViewParam.getPluginChartFieldCustomFilters(), tableObj);
        // 处理仪表板字段过滤
        String panelWheres = baseService.panelWhere(dsType, pluginViewParam.getPluginChartExtFilters(), tableObj);
        // 构建sql所有参数

        String permissionWhere = baseService.permissionWhere(dsType, rowPermissionsTree, tableObj);
        List<String> wheres = new ArrayList<>();
        if (customWheres != null) wheres.add(customWheres);
        if (panelWheres != null) wheres.add(panelWheres);
        if (permissionWhere != null) wheres.add(permissionWhere);
        List<PluginViewSQL> groups = new ArrayList<>();
        groups.addAll(xFields);
        // 外层再次套sql
        List<PluginViewSQL> orders = new ArrayList<>();
        orders.addAll(xOrders);
        orders.addAll(yOrders);
        List<String> aggWheres = new ArrayList<>();
        aggWheres.addAll(yWheres.stream().filter(ObjectUtils::isNotEmpty).collect(Collectors.toList()));

        STGroup stg = new STGroupFile(SQLConstants.SQL_TEMPLATE);
        ST st_sql = stg.getInstanceOf("querySql");
        if (CollectionUtils.isNotEmpty(xFields)) st_sql.add("groups", xFields);
        if (CollectionUtils.isNotEmpty(yFields)) st_sql.add("aggregators", yFields);
        if (CollectionUtils.isNotEmpty(wheres)) st_sql.add("filters", wheres);
        if (ObjectUtils.isNotEmpty(tableObj)) st_sql.add("table", tableObj);
        String sql = st_sql.render();

        String brackets = ConstantsUtil.constantsValue(dsType, "BRACKETS");
        String table_alias_prefix = ConstantsUtil.constantsValue(dsType, "TABLE_ALIAS_PREFIX");

        ST st = stg.getInstanceOf("querySql");
        PluginViewSQL tableSQL = PluginViewSQL.builder()
                .tableName(String.format(brackets, sql))
                .tableAlias(String.format(table_alias_prefix, 1))
                .build();
        if (CollectionUtils.isNotEmpty(aggWheres)) st.add("filters", aggWheres);
        if (CollectionUtils.isNotEmpty(orders)) st.add("orders", orders);
        if (ObjectUtils.isNotEmpty(tableSQL)) st.add("table", tableSQL);
        return baseService.sqlLimit(dsType, st.render(), pluginViewParam.getPluginViewLimit());
    }

}
