package io.dataease.service.dataset.impl.direct;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReflectUtil;
import com.google.gson.Gson;
import io.dataease.commons.exception.DEException;
import io.dataease.commons.model.BaseTreeNode;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.commons.utils.LogUtil;
import io.dataease.commons.utils.TreeUtils;
import io.dataease.dto.dataset.DataSetTableUnionDTO;
import io.dataease.dto.dataset.DataTableInfoDTO;
import io.dataease.dto.dataset.DeSortDTO;
import io.dataease.i18n.Translator;
import io.dataease.plugins.common.base.domain.ChartViewWithBLOBs;
import io.dataease.plugins.common.base.domain.DatasetTable;
import io.dataease.plugins.common.base.domain.DatasetTableField;
import io.dataease.plugins.common.base.domain.Datasource;
import io.dataease.plugins.common.constants.DatasetType;
import io.dataease.plugins.common.dto.chart.ChartFieldCustomFilterDTO;
import io.dataease.plugins.common.dto.datasource.DeSortField;
import io.dataease.plugins.common.request.datasource.DatasourceRequest;
import io.dataease.plugins.common.request.permission.DataSetRowPermissionsTreeDTO;
import io.dataease.plugins.datasource.provider.Provider;
import io.dataease.plugins.datasource.query.QueryProvider;
import io.dataease.plugins.xpack.auth.dto.request.ColumnPermissionItem;
import io.dataease.provider.ProviderFactory;
import io.dataease.service.dataset.*;
import io.dataease.service.datasource.DatasourceService;
import io.dataease.service.engine.EngineService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.Collator;
import java.util.*;
import java.util.stream.Collectors;


@Service("directDataSetFieldService")
public class DirectFieldService implements DataSetFieldService {
    @Resource
    private DataSetTableFieldsService dataSetTableFieldsService;
    @Resource
    private DataSetTableService dataSetTableService;
    @Resource
    private DatasourceService datasourceService;
    @Resource
    private DataSetTableUnionService dataSetTableUnionService;
    @Resource
    private PermissionService permissionService;
    @Resource
    private EngineService engineService;
    @Resource
    private PermissionsTreeService permissionsTreeService;

    @Override
    public List<Object> fieldValues(String fieldId, Long userId, Boolean userPermissions, Boolean rowAndColumnMgm) throws Exception {
        List<String> fieldIds = new ArrayList<>();
        fieldIds.add(fieldId);
        return fieldValues(fieldIds, null, userId, userPermissions, false, rowAndColumnMgm, null);
    }

    @Override
    public List<Object> fieldValues(String fieldId, DeSortDTO sortDTO, Long userId, Boolean userPermissions, Boolean rowAndColumnMgm, String keyword) throws Exception {
        List<String> fieldIds = new ArrayList<>();
        fieldIds.add(fieldId);
        return fieldValues(fieldIds, sortDTO, userId, userPermissions, false, rowAndColumnMgm, keyword);
    }

    public List<DeSortField> buildSorts(List<DatasetTableField> allFields, DeSortDTO sortDTO) {
        if (ObjectUtils.isEmpty(sortDTO) || StringUtils.isBlank(sortDTO.getId()) || StringUtils.isBlank(sortDTO.getSort()))
            return null;
        return allFields.stream().filter(field -> StringUtils.equals(sortDTO.getId(), field.getId())).map(field -> {
            DeSortField deSortField = BeanUtils.copyBean(new DeSortField(), field);
            deSortField.setOrderDirection(sortDTO.getSort());
            return deSortField;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Object> chineseSort(List<Object> list, DeSortDTO sortDTO) throws Exception {
        if (ObjectUtils.isEmpty(sortDTO) || CollectionUtil.isEmpty(list)) return list;
        String sort = sortDTO.getSort();
        if (!StringUtils.equals(sort, "chinese")) {
            return list;
        }
        String id = sortDTO.getId();
        String sortStr = StringUtils.equalsIgnoreCase("chineseDesc", id) ? "desc" : "asc";

        return CollectionUtil.sort(list, (v1, v2) -> {
            Collator instance = Collator.getInstance(Locale.CHINESE);
            if (StringUtils.equals("desc", sortStr)) {
                return instance.compare(v2, v1);
            }
            return instance.compare(v1, v2);
        });
    }

    private String formatTableByKeyword(String keyword, String originTable, List<DatasetTableField> fields, boolean useDataEaseName) {
        if (StringUtils.isBlank(keyword)) return originTable;
        List<String> fieldNames = fields.stream().map(f -> useDataEaseName ? f.getDataeaseName() : f.getOriginName()).collect(Collectors.toList());
        String whereSql = fieldNames.stream().map(fieldName -> " " + fieldName + " like '%" + keyword + "%'").collect(Collectors.joining(" or "));
        return "( select * from " + originTable + " where (" + whereSql + ") )";
    }

    @Override
    public List<Object> fieldValues(List<String> fieldIds, DeSortDTO sortDTO, Long userId, Boolean userPermissions, Boolean needMapping, Boolean rowAndColumnMgm, String keyword) throws Exception {
        String fieldId = fieldIds.get(0);
        DatasetTableField field = dataSetTableFieldsService.selectByPrimaryKey(fieldId);
        if (field == null || StringUtils.isEmpty(field.getTableId())) return null;

        DatasetTable datasetTable = dataSetTableService.get(field.getTableId());
        if (ObjectUtils.isEmpty(datasetTable) || StringUtils.isEmpty(datasetTable.getName())) return null;

        DatasetTableField datasetTableField = DatasetTableField.builder().tableId(field.getTableId()).checked(Boolean.TRUE).build();
        List<DatasetTableField> fields = dataSetTableFieldsService.list(datasetTableField);

        List<DeSortField> deSortFields = buildSorts(fields, sortDTO);

        boolean needSort = CollectionUtils.isNotEmpty(deSortFields);

        final List<String> allTableFieldIds = fields.stream().map(DatasetTableField::getId).collect(Collectors.toList());
        boolean multi = fieldIds.stream().anyMatch(item -> !allTableFieldIds.contains(item));
        if (multi && needMapping) {
            DEException.throwException("Cross multiple dataset is not supported");
        }

        List<DatasetTableField> permissionFields = fields;
        List<ChartFieldCustomFilterDTO> customFilter = new ArrayList<>();
        List<DataSetRowPermissionsTreeDTO> rowPermissionsTree = new ArrayList<>();
        if (userPermissions) {
            //列权限
            Map<String, ColumnPermissionItem> desensitizationList = new HashMap<>();
            fields = permissionService.filterColumnPermissions(fields, desensitizationList, datasetTable.getId(), userId);
            Map<String, DatasetTableField> fieldMap = fields.stream().collect(Collectors.toMap(DatasetTableField::getId, node -> node));
            permissionFields = fieldIds.stream().map(fieldMap::get).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(permissionFields) || permissionFields.get(0) == null) {
                return new ArrayList<>();
            }
            if (CollectionUtils.isNotEmpty(desensitizationList.keySet()) && desensitizationList.keySet().contains(field.getDataeaseName())) {
                List<Object> results = new ArrayList<>();
                results.add(ColumnPermissionItem.CompleteDesensitization);
                return results;
            }
            //行权限
            rowPermissionsTree = permissionsTreeService.getRowPermissionsTree(fields, datasetTable, userId);
        }
        if (rowAndColumnMgm) {
            Map<String, DatasetTableField> fieldMap = fields.stream().collect(Collectors.toMap(DatasetTableField::getId, node -> node));
            permissionFields = fieldIds.stream().map(fieldMap::get).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(permissionFields)) {
                return new ArrayList<>();
            }
        }

        DatasourceRequest datasourceRequest = new DatasourceRequest();
        Provider datasourceProvider = null;
        String createSQL = null;
        ChartViewWithBLOBs view = new ChartViewWithBLOBs();
        view.setResultCount(1000);
        view.setResultMode("custom");
        if (datasetTable.getMode() == 0) {// 直连
            if (StringUtils.isEmpty(datasetTable.getDataSourceId())) return null;
            Datasource ds = datasourceService.get(datasetTable.getDataSourceId());
            if (StringUtils.isNotEmpty(ds.getStatus()) && ds.getStatus().equalsIgnoreCase("Error")) {
                throw new Exception(Translator.get("i18n_invalid_ds"));
            }
            datasourceProvider = ProviderFactory.getProvider(ds.getType());
            datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(ds);
            DataTableInfoDTO dataTableInfoDTO = new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class);
            QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
            if (StringUtils.equalsIgnoreCase(datasetTable.getType(), DatasetType.DB.toString())) {
                datasourceRequest.setTable(dataTableInfoDTO.getTable());
                String formatSql = formatTableByKeyword(keyword, dataTableInfoDTO.getTable(), permissionFields, false);
                createSQL = qp.createQuerySQLWithLimit(formatSql, permissionFields, !needSort, ds, customFilter, rowPermissionsTree, deSortFields, 1000L);
            } else if (StringUtils.equalsIgnoreCase(datasetTable.getType(), DatasetType.SQL.toString())) {
                String sql = dataTableInfoDTO.getSql();
                if (dataTableInfoDTO.isBase64Encryption()) {
                    sql = new String(java.util.Base64.getDecoder().decode(sql));
                }
                sql = dataSetTableService.handleVariableDefaultValue(sql, null, ds.getType(), false);
                if (StringUtils.isNotBlank(keyword)) {
                    sql = formatTableByKeyword(keyword, " (" + sql + ") " + "inner_like_temp ", permissionFields, false);
                }
                createSQL = qp.createQuerySQLAsTmpWithLimit(sql, permissionFields, !needSort, customFilter, rowPermissionsTree, deSortFields, 1000L);
            } else if (StringUtils.equalsIgnoreCase(datasetTable.getType(), DatasetType.CUSTOM.toString())) {
                DataTableInfoDTO dt = new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class);
                List<DataSetTableUnionDTO> listUnion = dataSetTableUnionService.listByTableId(dt.getList().get(0).getTableId());
                String sql = dataSetTableService.getCustomSQLDatasource(dt, listUnion, ds);
                if (StringUtils.isNotBlank(keyword)) {
                    sql = formatTableByKeyword(keyword, " (" + sql + ") " + "inner_like_temp ", permissionFields, false);
                }
                createSQL = qp.createQuerySQLAsTmpWithLimit(sql, permissionFields, !needSort, customFilter, rowPermissionsTree, deSortFields, 1000L);
            } else if (StringUtils.equalsIgnoreCase(datasetTable.getType(), DatasetType.UNION.toString())) {
                DataTableInfoDTO dt = new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class);
                String sql = (String) dataSetTableService.getUnionSQLDatasource(dt, ds).get("sql");
                if (StringUtils.isNotBlank(keyword)) {
                    sql = formatTableByKeyword(keyword, " (" + sql + ") " + "inner_like_temp ", permissionFields, false);
                }
                createSQL = qp.createQuerySQLAsTmpWithLimit(sql, permissionFields, !needSort, customFilter, rowPermissionsTree, deSortFields, 1000L);
            }
            if (StringUtils.equalsAny(ds.getType(), "ds_doris", "mysql")) {
                Object[] args = new Object[]{createSQL, view};
                createSQL = ReflectUtil.invoke(qp, "sqlLimit", args);
                datasourceRequest.setQuery(createSQL);
            } else {
                datasourceRequest.setQuery(qp.createSQLPreview(createSQL, null));
            }

        } else if (datasetTable.getMode() == 1) {// 抽取
            // 连接doris，构建doris数据源查询
            Datasource ds = engineService.getDeEngine();
            datasourceProvider = ProviderFactory.getProvider(ds.getType());
            datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(ds);
            String tableName = "ds_" + datasetTable.getId().replaceAll("-", "_");
            datasourceRequest.setTable(tableName);
            QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
            String formatSql = formatTableByKeyword(keyword, tableName, permissionFields, true);
            createSQL = qp.createQuerySQL(formatSql, permissionFields, !needSort, null, customFilter, rowPermissionsTree, deSortFields);
            Object[] args = new Object[]{createSQL, view};
            createSQL = ReflectUtil.invoke(qp, "sqlLimit", args);
            datasourceRequest.setQuery(createSQL);
        }

        int originSize = permissionFields.size();
        boolean existExtSortField = false;
        if (CollectionUtils.isNotEmpty(deSortFields)) {
            List<String> fieldList = permissionFields.stream().map(DatasetTableField::getId).collect(Collectors.toList());
            for (DeSortField sField : deSortFields) {
                int indexOf = fieldList.indexOf(sField.getId());
                if (indexOf == -1) {
                    existExtSortField = true;
                    permissionFields.add(sField);
                } else {
                    permissionFields.set(indexOf, sField);
                }
            }
        }

        LogUtil.info(datasourceRequest.getQuery());
        datasourceRequest.setPermissionFields(permissionFields);
        assert datasourceProvider != null;
        List<String[]> rows = datasourceProvider.getData(datasourceRequest);
        if (!needMapping) {
            return rows.stream().map(row -> row[0]).distinct().collect(Collectors.toList());
        }
        Set<String> pkSet = new HashSet<>();
        if (CollectionUtils.isNotEmpty(rows) && existExtSortField && originSize > 0) {
            rows = rows.stream().map(row -> ArrayUtil.sub(row, 0, originSize)).collect(Collectors.toList());
        }
        List<BaseTreeNode> treeNodes = rows.stream().map(row -> buildTreeNode(row, pkSet)).flatMap(Collection::stream).collect(Collectors.toList());
        List tree = TreeUtils.mergeDuplicateTree(treeNodes, TreeUtils.DEFAULT_ROOT);
        return tree;

    }

    private List<BaseTreeNode> buildTreeNode(String[] row, Set<String> pkSet) {
        List<BaseTreeNode> nodes = new ArrayList<>();
        List<String> parentPkList = new ArrayList<>();
        for (int i = 0; i < row.length; i++) {
            String text = row[i];

            parentPkList.add(text);
            String val = parentPkList.stream().collect(Collectors.joining(TreeUtils.SEPARATOR));
            String parentVal = i == 0 ? TreeUtils.DEFAULT_ROOT : row[i - 1];
            String pk = parentPkList.stream().collect(Collectors.joining(TreeUtils.SEPARATOR));
            if (pkSet.contains(pk)) continue;
            pkSet.add(pk);
            BaseTreeNode node = new BaseTreeNode(val, parentVal, text, pk + TreeUtils.SEPARATOR + i);
            nodes.add(node);
        }
        return nodes;

    }

}
