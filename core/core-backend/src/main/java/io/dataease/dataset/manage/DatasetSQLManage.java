package io.dataease.dataset.manage;

import io.dataease.api.dataset.dto.DatasetTableDTO;
import io.dataease.api.dataset.dto.DatasetTableFieldDTO;
import io.dataease.api.dataset.union.*;
import io.dataease.api.dataset.union.model.SQLObj;
import io.dataease.dataset.dao.auto.entity.CoreDatasetTable;
import io.dataease.dataset.dao.auto.entity.CoreDatasetTableField;
import io.dataease.dataset.dao.auto.mapper.CoreDatasetTableMapper;
import io.dataease.dataset.dto.DatasourceSchemaDTO;
import io.dataease.dataset.utils.DatasetTableTypeConstants;
import io.dataease.dataset.utils.TableUtils;
import io.dataease.datasource.dao.auto.entity.CoreDatasource;
import io.dataease.datasource.dao.auto.mapper.CoreDatasourceMapper;
import io.dataease.engine.constant.SQLConstants;
import io.dataease.exception.DEException;
import io.dataease.i18n.Translator;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.JsonUtil;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.text.MessageFormat;
import java.util.*;

/**
 * @Author Junjun
 */
@Component
public class DatasetSQLManage {
    @Resource
    private CoreDatasetTableMapper coreDatasetTableMapper;
    @Resource
    private DatasetTableFieldManage datasetTableFieldManage;
    @Resource
    private CoreDatasourceMapper coreDatasourceMapper;

    // 编辑模式下使用
    public Map<String, Object> getUnionSQLForEdit(DatasetGroupInfoDTO dataTableInfoDTO) {
        Map<Long, DatasourceSchemaDTO> dsMap = new LinkedHashMap<>();
        List<UnionDTO> union = dataTableInfoDTO.getUnion();
        // 所有选中的字段，即select后的查询字段
        Map<String, String[]> checkedInfo = new LinkedHashMap<>();
        List<UnionParamDTO> unionList = new ArrayList<>();
        List<DatasetTableFieldDTO> checkedFields = new ArrayList<>();
        String sql = "";

        // get table
        String tableSchema = String.format(SQLConstants.SCHEMA, 0);

        DatasetTableDTO currentDs = union.get(0).getCurrentDs();
        DatasetTableInfoDTO infoDTO = JsonUtil.parseObject(currentDs.getInfo(), DatasetTableInfoDTO.class);
        SQLObj tableName = getUnionTable(currentDs, infoDTO, tableSchema, 0);
        // get datasource and schema,put map
        putObj2Map(dsMap, currentDs.getDatasourceId(), tableSchema);

        for (int i = 0; i < union.size(); i++) {
            String schema = String.format(SQLConstants.SCHEMA, i);

            UnionDTO unionDTO = union.get(i);
            DatasetTableDTO datasetTable = unionDTO.getCurrentDs();
            DatasetTableInfoDTO tableInfo = JsonUtil.parseObject(datasetTable.getInfo(), DatasetTableInfoDTO.class);
            SQLObj table = getUnionTable(datasetTable, tableInfo, schema, i);
            putObj2Map(dsMap, datasetTable.getDatasourceId(), schema);

            // 获取前端传过来选中的字段
            List<DatasetTableFieldDTO> fields = unionDTO.getCurrentDsFields();

            String[] array = fields.stream()
                    .map(f -> table.getTableAlias() + "." + f.getOriginName() + " AS "
                            + TableUtils.fieldNameShort(table.getTableAlias() + "_" + f.getOriginName()))
                    .toArray(String[]::new);
            checkedInfo.put(table.getTableAlias(), array);
            checkedFields.addAll(fields);
            // 获取child的fields和union
            if (!CollectionUtils.isEmpty(unionDTO.getChildrenDs())) {
                getUnionForEdit(datasetTable, table, unionDTO.getChildrenDs(), checkedInfo, unionList, checkedFields, union.size(), dsMap);
            }
        }
        // build sql
        if (!CollectionUtils.isEmpty(unionList)) {
            // field
            StringBuilder field = new StringBuilder();
            for (Map.Entry<String, String[]> next : checkedInfo.entrySet()) {
                if (next.getValue().length > 0) {
                    field.append(StringUtils.join(next.getValue(), ",")).append(",");
                }
            }
            String f = subPrefixSuffixChar(field.toString());
            // join
            StringBuilder join = new StringBuilder();
            for (UnionParamDTO unionParamDTO : unionList) {
                // get join type
                String joinType = convertUnionTypeToSQL(unionParamDTO.getUnionType());

                SQLObj parentSQLObj = unionParamDTO.getParentSQLObj();
                SQLObj currentSQLObj = unionParamDTO.getCurrentSQLObj();

                String ts = "";
                if (ObjectUtils.isNotEmpty(currentSQLObj.getTableSchema())) {
                    ts = currentSQLObj.getTableSchema() + ".";
                }
                // build join
                join.append(" ").append(joinType).append(" ")
                        .append(ts)
                        .append(currentSQLObj.getTableName())
                        .append(" ").append(currentSQLObj.getTableAlias()).append(" ")
                        .append(" ON ");
                for (int i = 0; i < unionParamDTO.getUnionFields().size(); i++) {
                    UnionItemDTO unionItemDTO = unionParamDTO.getUnionFields().get(i);
                    // 通过field id取得field详情，并且以第一组为准，寻找dataset table
                    DatasetTableFieldDTO parentField = unionItemDTO.getParentField();
                    DatasetTableFieldDTO currentField = unionItemDTO.getCurrentField();

                    join.append(parentSQLObj.getTableAlias()).append(".")
                            .append(parentField.getOriginName())
                            .append(" = ")
                            .append(currentSQLObj.getTableAlias()).append(".")
                            .append(currentField.getOriginName());
                    if (i < unionParamDTO.getUnionFields().size() - 1) {
                        join.append(" AND ");
                    }
                }
            }
            if (StringUtils.isEmpty(f)) {
                DEException.throwException(Translator.get("i18n_union_ds_no_checked"));
            }
            sql = MessageFormat.format("SELECT {0} FROM {1}", f, TableUtils.getTableAndAlias(tableName)) + join.toString();
        } else {
            String f = StringUtils.join(checkedInfo.get(tableName.getTableAlias()), ",");
            if (StringUtils.isEmpty(f)) {
                throw new RuntimeException(Translator.get("i18n_union_ds_no_checked"));
            }
            sql = MessageFormat.format("SELECT {0} FROM {1}", f, TableUtils.getTableAndAlias(tableName));
        }
        Map<String, Object> map = new HashMap<>();
        map.put("sql", sql);
        map.put("field", checkedFields);
        map.put("join", unionList);
        map.put("dsMap", dsMap);
        return map;
    }

    // 递归计算出所有子级的checkedFields和unionParam
    private void getUnionForEdit(DatasetTableDTO parentTable, SQLObj parentSQLObj,
                                 List<UnionDTO> childrenDs, Map<String, String[]> checkedInfo,
                                 List<UnionParamDTO> unionList, List<DatasetTableFieldDTO> checkedFields, int indexPre,
                                 Map<Long, DatasourceSchemaDTO> dsMap) {
        for (int i = 0; i < childrenDs.size(); i++) {
            int index = i + indexPre;
            String tableSchema = String.format(SQLConstants.SCHEMA, index);

            UnionDTO unionDTO = childrenDs.get(i);
            DatasetTableDTO datasetTable = unionDTO.getCurrentDs();
            DatasetTableInfoDTO tableInfo = JsonUtil.parseObject(datasetTable.getInfo(), DatasetTableInfoDTO.class);
            SQLObj table = getUnionTable(datasetTable, tableInfo, tableSchema, index);
            putObj2Map(dsMap, datasetTable.getDatasourceId(), tableSchema);

            List<DatasetTableFieldDTO> fields = unionDTO.getCurrentDsFields();

            String[] array = fields.stream()
                    .map(f -> table.getTableAlias() + "." + f.getOriginName() + " AS "
                            + TableUtils.fieldNameShort(table.getTableAlias() + "_" + f.getOriginName()))
                    .toArray(String[]::new);
            checkedInfo.put(table.getTableAlias(), array);
            checkedFields.addAll(fields);

            UnionParamDTO unionToParent = unionDTO.getUnionToParent();
            // 设置关联关系中，两个table信息
            unionToParent.setParentDs(parentTable);
            unionToParent.setParentSQLObj(parentSQLObj);
            unionToParent.setCurrentDs(datasetTable);
            unionToParent.setCurrentSQLObj(table);
            unionList.add(unionToParent);
            if (!CollectionUtils.isEmpty(unionDTO.getChildrenDs())) {
                getUnionForEdit(datasetTable, table, unionDTO.getChildrenDs(), checkedInfo, unionList, checkedFields, indexPre + childrenDs.size(), dsMap);
            }
        }
    }

    // 非编辑模式下使用
    public Map<String, Object> getUnionSQLForQuery(DatasetGroupInfoDTO dataTableInfoDTO) {
        List<UnionDTO> union = dataTableInfoDTO.getUnion();
        // 所有选中的字段，即select后的查询字段
        Map<String, String[]> checkedInfo = new LinkedHashMap<>();
        List<UnionParamDTO> unionList = new ArrayList<>();
        List<CoreDatasetTableField> checkedFields = new ArrayList<>();
        String sql = "";
        String tableName = JsonUtil.parseObject(coreDatasetTableMapper.selectById(union.get(0).getCurrentDs().getId()).getInfo(),
                DatasetTableInfoDTO.class).getTable();
        for (UnionDTO unionDTO : union) {
            CoreDatasetTable datasetTable = coreDatasetTableMapper.selectById(unionDTO.getCurrentDs().getId());
            String table = JsonUtil.parseObject(datasetTable.getInfo(), DatasetTableInfoDTO.class).getTable();
            Long tableId = unionDTO.getCurrentDs().getId();
            if (ObjectUtils.isEmpty(datasetTable)) {
                DEException.throwException(
                        Translator.get("i18n_custom_ds_delete") + String.format(":table id [%s]", tableId));
            }
            /* 用户以及权限校验
            CurrentUserDto user = AuthUtils.getUser();
            if (user != null && !user.getIsAdmin()) {
                DataSetTableDTO withPermission = getWithPermission(datasetTable.getId(), user.getUserId());
                if (ObjectUtils.isEmpty(withPermission.getPrivileges()) || !withPermission.getPrivileges().contains("use")) {
                    DEException.throwException(
                            Translator.get("i18n_dataset_no_permission") + String.format(":table name [%s]", withPermission.getName()));
                }
            }
            List<DatasetTableField> fields = dataSetTableFieldsService.getListByIdsEach(unionDTO.getCurrentDsField());
            */
            // TODO permission
            List<CoreDatasetTableField> fields = datasetTableFieldManage.selectByFieldIds(unionDTO.getCurrentDsField());

            String[] array = fields.stream()
                    .map(f -> table + "." + f.getOriginName() + " AS "
                            + TableUtils.fieldNameShort(tableId + "_" + f.getOriginName()))
                    .toArray(String[]::new);
            checkedInfo.put(table, array);
            checkedFields.addAll(fields);
            // 获取child的fields和union
            if (!CollectionUtils.isEmpty(unionDTO.getChildrenDs())) {
                getUnionForQuery(unionDTO.getChildrenDs(), checkedInfo, unionList, checkedFields);
            }
        }
        // build sql
        if (!CollectionUtils.isEmpty(unionList)) {
            // field
            StringBuilder field = new StringBuilder();
            for (Map.Entry<String, String[]> next : checkedInfo.entrySet()) {
                if (next.getValue().length > 0) {
                    field.append(StringUtils.join(next.getValue(), ",")).append(",");
                }
            }
            String f = subPrefixSuffixChar(field.toString());
            // join
            StringBuilder join = new StringBuilder();
            for (UnionParamDTO unionParamDTO : unionList) {
                String joinType = convertUnionTypeToSQL(unionParamDTO.getUnionType());
                UnionItemDTO u = unionParamDTO.getUnionFields().get(0);
                CoreDatasetTableField pField = datasetTableFieldManage.selectById(u.getParentField().getId());
                CoreDatasetTableField cField = datasetTableFieldManage.selectById(u.getCurrentField().getId());
                if (ObjectUtils.isEmpty(pField) || ObjectUtils.isEmpty(cField)) {
                    DEException.throwException(Translator.get("i18n_dataset_field_delete"));
                }
                CoreDatasetTable parentTable = coreDatasetTableMapper.selectById(pField.getDatasetTableId());
                String parentTableName = JsonUtil.parseObject(parentTable.getInfo(), DatasetTableInfoDTO.class).getTable();
                CoreDatasetTable currentTable = coreDatasetTableMapper.selectById(cField.getDatasetTableId());
                String currentTableName = JsonUtil.parseObject(currentTable.getInfo(), DatasetTableInfoDTO.class)
                        .getTable();

                // todo
                join.append(" ").append(joinType).append(" ").append("calciteSchema.").append(currentTableName)
                        .append(" ON ");
                for (int i = 0; i < unionParamDTO.getUnionFields().size(); i++) {
                    UnionItemDTO unionItemDTO = unionParamDTO.getUnionFields().get(i);
                    // 通过field id取得field详情，并且以第一组为准，寻找dataset table
                    CoreDatasetTableField parentField = datasetTableFieldManage
                            .selectById(unionItemDTO.getParentField().getId());
                    CoreDatasetTableField currentField = datasetTableFieldManage
                            .selectById(unionItemDTO.getCurrentField().getId());

                    join.append(parentTableName).append(".")
                            .append(parentField.getOriginName())
                            .append(" = ")
                            .append(currentTableName).append(".")
                            .append(currentField.getOriginName());
                    if (i < unionParamDTO.getUnionFields().size() - 1) {
                        join.append(" AND ");
                    }
                }
            }
            if (StringUtils.isEmpty(f)) {
                DEException.throwException(Translator.get("i18n_union_ds_no_checked"));
            }
            sql = MessageFormat.format("SELECT {0} FROM {1}", f, tableName) + join.toString();
        } else {
            String f = StringUtils.join(checkedInfo.get(tableName), ",");
            if (StringUtils.isEmpty(f)) {
                throw new RuntimeException(Translator.get("i18n_union_ds_no_checked"));
            }
            sql = MessageFormat.format("SELECT {0} FROM {1}", f, tableName);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("sql", sql);
        map.put("field", checkedFields);
        map.put("join", unionList);
        return map;
    }

    // 递归计算出所有子级的checkedFields和unionParam
    private void getUnionForQuery(List<UnionDTO> childrenDs, Map<String, String[]> checkedInfo,
                                  List<UnionParamDTO> unionList, List<CoreDatasetTableField> checkedFields) {
        for (UnionDTO unionDTO : childrenDs) {
            CoreDatasetTable datasetTable = coreDatasetTableMapper.selectById(unionDTO.getCurrentDs().getId());
            Long tableId = unionDTO.getCurrentDs().getId();
            if (ObjectUtils.isEmpty(datasetTable)) {
                DEException.throwException(
                        Translator.get("i18n_custom_ds_delete") + String.format(":table id [%s]", tableId));
            }
            String table = JsonUtil.parseObject(datasetTable.getInfo(), DatasetTableInfoDTO.class).getTable();

            List<CoreDatasetTableField> fields = datasetTableFieldManage.selectByFieldIds(unionDTO.getCurrentDsField());

            String[] array = fields.stream()
                    .map(f -> table + "." + f.getOriginName() + " AS "
                            + TableUtils.fieldNameShort(tableId + "_" + f.getOriginName()))
                    .toArray(String[]::new);
            checkedInfo.put(table, array);
            checkedFields.addAll(fields);

            unionList.add(unionDTO.getUnionToParent());
            if (!CollectionUtils.isEmpty(unionDTO.getChildrenDs())) {
                getUnionForQuery(unionDTO.getChildrenDs(), checkedInfo, unionList, checkedFields);
            }
        }
    }

    private String subPrefixSuffixChar(String str) {
        while (StringUtils.startsWith(str, ",")) {
            str = str.substring(1, str.length());
        }
        while (StringUtils.endsWith(str, ",")) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    private String convertUnionTypeToSQL(String unionType) {
        switch (unionType) {
            case "1:1":
            case "inner":
                return " INNER JOIN ";
            case "1:N":
            case "left":
                return " LEFT JOIN ";
            case "N:1":
            case "right":
                return " RIGHT JOIN ";
            case "N:N":
            case "full":
                return " FULL JOIN ";
            default:
                return " INNER JOIN ";
        }
    }

    private SQLObj getUnionTable(DatasetTableDTO currentDs, DatasetTableInfoDTO infoDTO, String tableSchema, int index) {
        SQLObj tableObj;
        String tableAlias = String.format(SQLConstants.TABLE_ALIAS_PREFIX, index);
        if (StringUtils.equalsIgnoreCase(currentDs.getType(), DatasetTableTypeConstants.DATASET_TABLE_DB)) {
            tableObj = SQLObj.builder().tableSchema(tableSchema).tableName(infoDTO.getTable()).tableAlias(tableAlias).build();
        } else if (StringUtils.equalsIgnoreCase(currentDs.getType(), DatasetTableTypeConstants.DATASET_TABLE_SQL)) {
            tableObj = SQLObj.builder().tableSchema("").tableName("(" + new String(Base64.getDecoder().decode(infoDTO.getSql())) + ")").tableAlias(tableAlias).build();
        } else {
            // todo excel,api
            tableObj = SQLObj.builder().tableSchema(tableSchema).tableName(infoDTO.getTable()).tableAlias(tableAlias).build();
        }
        return tableObj;
    }

    private void putObj2Map(Map<Long, DatasourceSchemaDTO> dsMap, Long datasourceId, String schemaAlias) {
        if (!dsMap.containsKey(datasourceId)) {
            CoreDatasource coreDatasource = coreDatasourceMapper.selectById(datasourceId);
            DatasourceSchemaDTO datasourceSchemaDTO = new DatasourceSchemaDTO();
            BeanUtils.copyBean(datasourceSchemaDTO, coreDatasource);
            datasourceSchemaDTO.setSchemaAlias(schemaAlias);
            dsMap.put(datasourceId, datasourceSchemaDTO);
        }
    }
}
