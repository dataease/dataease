package io.dataease.dataset.manage;

import io.dataease.api.dataset.union.*;
import io.dataease.api.permissions.auth.dto.BusiPerCheckDTO;
import io.dataease.commons.utils.SqlparserUtils;
import io.dataease.constant.AuthEnum;
import io.dataease.dataset.constant.DatasetTableType;
import io.dataease.dataset.utils.DatasetTableTypeConstants;
import io.dataease.dataset.utils.SqlUtils;
import io.dataease.dataset.utils.TableUtils;
import io.dataease.datasource.dao.auto.entity.CoreDatasource;
import io.dataease.datasource.dao.auto.mapper.CoreDatasourceMapper;
import io.dataease.datasource.manage.EngineManage;
import io.dataease.engine.constant.ExtFieldConstant;
import io.dataease.engine.constant.SQLConstants;
import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.api.PluginManageApi;
import io.dataease.extensions.datasource.dto.DatasetTableDTO;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.extensions.datasource.dto.DatasourceSchemaDTO;
import io.dataease.extensions.datasource.dto.DsTypeDTO;
import io.dataease.extensions.datasource.factory.ProviderFactory;
import io.dataease.extensions.datasource.model.SQLObj;
import io.dataease.extensions.datasource.provider.Provider;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
import io.dataease.extensions.datasource.vo.XpackPluginsDatasourceVO;
import io.dataease.extensions.view.dto.ChartExtFilterDTO;
import io.dataease.extensions.view.dto.ChartExtRequest;
import io.dataease.extensions.view.dto.SqlVariableDetails;
import io.dataease.i18n.Translator;
import io.dataease.license.utils.LicenseUtil;
import io.dataease.system.manage.CorePermissionManage;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.JsonUtil;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author Junjun
 */
@Component
public class DatasetSQLManage {

    @Resource
    private CoreDatasourceMapper coreDatasourceMapper;
    @Resource
    private EngineManage engineManage;

    @Resource
    private CorePermissionManage corePermissionManage;

    @Autowired(required = false)
    private PluginManageApi pluginManage;

    private static Logger logger = LoggerFactory.getLogger(DatasetSQLManage.class);

    private List<SqlVariableDetails> filterParameters(ChartExtRequest chartExtRequest, Long datasetTableId) {
        List<SqlVariableDetails> parameters = new ArrayList<>();
        if (chartExtRequest != null && ObjectUtils.isNotEmpty(chartExtRequest.getOuterParamsFilters())) {
            for (ChartExtFilterDTO filterDTO : chartExtRequest.getOuterParamsFilters()) {
                if (CollectionUtils.isEmpty(filterDTO.getValue())) {
                    continue;
                }
                filterParametersAdaptor(parameters,filterDTO,datasetTableId);
            }
        }
        if (chartExtRequest != null && ObjectUtils.isNotEmpty(chartExtRequest.getFilter())) {
            for (ChartExtFilterDTO filterDTO : chartExtRequest.getFilter()) {
                if (CollectionUtils.isEmpty(filterDTO.getValue())) {
                    continue;
                }
                filterParametersAdaptor(parameters,filterDTO,datasetTableId);
            }
        }
        return parameters;
    }

    private void filterParametersAdaptor(List<SqlVariableDetails> parameters,ChartExtFilterDTO filterDTO,Long datasetTableId){
        if (ObjectUtils.isNotEmpty(filterDTO.getParameters())) {
            for (SqlVariableDetails parameter : filterDTO.getParameters()) {
                if (parameter.getDatasetTableId().equals(datasetTableId)) {
                    parameter.setValue(filterDTO.getValue());
                    parameter.setOperator(filterDTO.getOperator());
                    parameters.add(parameter);
                }
            }
        }
    }

    public Map<String, Object> getUnionSQLForEdit(DatasetGroupInfoDTO dataTableInfoDTO, ChartExtRequest chartExtRequest) throws Exception {
        Map<Long, DatasourceSchemaDTO> dsMap = new LinkedHashMap<>();
        List<UnionDTO> union = dataTableInfoDTO.getUnion();
        // 所有选中的字段，即select后的查询字段
        Map<String, String[]> checkedInfo = new LinkedHashMap<>();
        List<UnionParamDTO> unionList = new ArrayList<>();
        List<DatasetTableFieldDTO> checkedFields = new ArrayList<>();
        String sql = "";

        if (ObjectUtils.isEmpty(union)) {
            return null;
        }
        Set<Long> allDs = getAllDs(union);
        boolean isCross = allDs.size() > 1;

        DatasetTableDTO currentDs = union.get(0).getCurrentDs();

        // get datasource and schema,put map
        String tableSchema = putObj2Map(dsMap, currentDs, isCross);
        // get table
        DatasetTableInfoDTO infoDTO = JsonUtil.parseObject(currentDs.getInfo(), DatasetTableInfoDTO.class);

        SQLObj tableName = getUnionTable(currentDs, infoDTO, tableSchema, 0, filterParameters(chartExtRequest, currentDs.getId()), chartExtRequest == null, isCross, dsMap);

        for (int i = 0; i < union.size(); i++) {
            UnionDTO unionDTO = union.get(i);
            DatasetTableDTO datasetTable = unionDTO.getCurrentDs();
            DatasetTableInfoDTO tableInfo = JsonUtil.parseObject(datasetTable.getInfo(), DatasetTableInfoDTO.class);

            String schema;
            if (dsMap.containsKey(datasetTable.getDatasourceId())) {
                schema = dsMap.get(datasetTable.getDatasourceId()).getSchemaAlias();
            } else {
                schema = putObj2Map(dsMap, datasetTable, isCross);
            }
            SQLObj table = getUnionTable(datasetTable, tableInfo, schema, i, filterParameters(chartExtRequest, currentDs.getId()), chartExtRequest == null, isCross, dsMap);

            // 获取前端传过来选中的字段
            List<DatasetTableFieldDTO> fields = unionDTO.getCurrentDsFields();
            fields = fields.stream().filter(DatasetTableFieldDTO::getChecked).collect(Collectors.toList());

            String[] array = fields.stream()
                    .map(f -> {
                        String alias;
                        if (StringUtils.isEmpty(f.getDataeaseName())) {
                            alias = TableUtils.fieldNameShort(table.getTableAlias() + "_" + f.getOriginName());
                        } else {
                            alias = f.getDataeaseName();
                        }

                        f.setFieldShortName(alias);
                        f.setDataeaseName(f.getFieldShortName());
                        f.setDatasetTableId(datasetTable.getId());
                        String prefix = "";
                        String suffix = "";
                        if (Objects.equals(f.getExtField(), ExtFieldConstant.EXT_NORMAL)) {
                            if (isCross) {
                                prefix = "`";
                                suffix = "`";
                            } else {
                                DsTypeDTO datasourceType = getDatasourceType(dsMap, datasetTable.getDatasourceId());
                                prefix = datasourceType.getPrefix();
                                suffix = datasourceType.getSuffix();
                            }
                        }
                        return table.getTableAlias() + "." + prefix + f.getOriginName() + suffix + " AS " + prefix + alias + suffix;
                    })
                    .toArray(String[]::new);
            checkedInfo.put(table.getTableAlias(), array);
            checkedFields.addAll(fields);
            // 获取child的fields和union
            if (!CollectionUtils.isEmpty(unionDTO.getChildrenDs())) {
                getUnionForEdit(datasetTable, table, unionDTO.getChildrenDs(), checkedInfo, unionList, checkedFields, dsMap, chartExtRequest, isCross);
            }
        }
        // build sql
        boolean isFullJoin = false;
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
                // 如果不是全连接则需要校验连接方式
                if (!isFullJoin) {
                    if (StringUtils.equalsIgnoreCase(unionParamDTO.getUnionType(), "full")) {
                        isFullJoin = true;
                    }
                }

                SQLObj parentSQLObj = unionParamDTO.getParentSQLObj();
                SQLObj currentSQLObj = unionParamDTO.getCurrentSQLObj();
                DatasetTableDTO parentDs = unionParamDTO.getParentDs();
                DatasetTableDTO currentDs1 = unionParamDTO.getCurrentDs();

                String ts = "";
                String tablePrefix = "";
                String tableSuffix = "";
                if (ObjectUtils.isNotEmpty(currentSQLObj.getTableSchema())) {
                    ts = currentSQLObj.getTableSchema() + ".";

                    if (isCross) {
                        tablePrefix = "`";
                        tableSuffix = "`";
                    } else {
                        DsTypeDTO datasourceType = getDatasourceType(dsMap, currentDs1.getDatasourceId());
                        tablePrefix = datasourceType.getPrefix();
                        tableSuffix = datasourceType.getSuffix();
                    }
                }
                // build join
                join.append(" ").append(joinType).append(" ")
                        .append(ts)
                        .append(tablePrefix + currentSQLObj.getTableName() + tableSuffix)
                        .append(" ").append(currentSQLObj.getTableAlias()).append(" ")
                        .append(" ON ");
                if (unionParamDTO.getUnionFields().size() == 0) {
                    DEException.throwException(Translator.get("i18n_union_field_can_not_empty"));
                }
                for (int i = 0; i < unionParamDTO.getUnionFields().size(); i++) {
                    UnionItemDTO unionItemDTO = unionParamDTO.getUnionFields().get(i);
                    // 通过field id取得field详情，并且以第一组为准，寻找dataset table
                    DatasetTableFieldDTO parentField = unionItemDTO.getParentField();
                    DatasetTableFieldDTO currentField = unionItemDTO.getCurrentField();
                    String pPrefix = "";
                    String pSuffix = "";
                    if (Objects.equals(parentField.getExtField(), ExtFieldConstant.EXT_NORMAL)) {
                        if (isCross) {
                            pPrefix = "`";
                            pSuffix = "`";
                        } else {
                            DsTypeDTO datasourceType = getDatasourceType(dsMap, parentDs.getDatasourceId());
                            pPrefix = datasourceType.getPrefix();
                            pSuffix = datasourceType.getSuffix();
                        }
                    }
                    String cPrefix = "";
                    String cSuffix = "";
                    if (Objects.equals(currentField.getExtField(), ExtFieldConstant.EXT_NORMAL)) {
                        if (isCross) {
                            cPrefix = "`";
                            cSuffix = "`";
                        } else {
                            DsTypeDTO datasourceType = getDatasourceType(dsMap, currentDs1.getDatasourceId());
                            cPrefix = datasourceType.getPrefix();
                            cSuffix = datasourceType.getSuffix();
                        }
                    }
                    join.append(parentSQLObj.getTableAlias()).append(".")
                            .append(pPrefix + parentField.getOriginName() + pSuffix)
                            .append(" = ")
                            .append(currentSQLObj.getTableAlias()).append(".")
                            .append(cPrefix + currentField.getOriginName() + cSuffix);
                    if (i < unionParamDTO.getUnionFields().size() - 1) {
                        join.append(" AND ");
                    }
                }
            }
            if (StringUtils.isEmpty(f)) {
                DEException.throwException(Translator.get("i18n_union_ds_no_checked"));
            }
            sql = MessageFormat.format("SELECT {0} FROM {1}", f, TableUtils.getTableAndAlias(tableName, getDatasourceType(dsMap, currentDs.getDatasourceId()), isCross)) + join.toString();
        } else {
            String f = StringUtils.join(checkedInfo.get(tableName.getTableAlias()), ",");
            if (StringUtils.isEmpty(f)) {
                DEException.throwException(Translator.get("i18n_union_ds_no_checked"));
            }
            sql = MessageFormat.format("SELECT {0} FROM {1}", f, TableUtils.getTableAndAlias(tableName, getDatasourceType(dsMap, currentDs.getDatasourceId()), isCross));
        }
        logger.debug("calcite origin sql: " + sql);
        Map<String, Object> map = new HashMap<>();
        map.put("sql", sql);
        map.put("field", checkedFields);
        map.put("join", unionList);
        map.put("dsMap", dsMap);
        map.put("isFullJoin", isFullJoin);
        return map;
    }

    // 递归计算出所有子级的checkedFields和unionParam
    private void getUnionForEdit(DatasetTableDTO parentTable, SQLObj parentSQLObj,
                                 List<UnionDTO> childrenDs, Map<String, String[]> checkedInfo,
                                 List<UnionParamDTO> unionList, List<DatasetTableFieldDTO> checkedFields,
                                 Map<Long, DatasourceSchemaDTO> dsMap, ChartExtRequest chartExtRequest,
                                 boolean isCross) throws Exception {
        for (int i = 0; i < childrenDs.size(); i++) {
            int index = unionList.size() + 1;

            UnionDTO unionDTO = childrenDs.get(i);
            DatasetTableDTO datasetTable = unionDTO.getCurrentDs();
            DatasetTableInfoDTO tableInfo = JsonUtil.parseObject(datasetTable.getInfo(), DatasetTableInfoDTO.class);

            String schema;
            if (dsMap.containsKey(datasetTable.getDatasourceId())) {
                schema = dsMap.get(datasetTable.getDatasourceId()).getSchemaAlias();
            } else {
                schema = putObj2Map(dsMap, datasetTable, isCross);
            }
            SQLObj table = getUnionTable(datasetTable, tableInfo, schema, index, filterParameters(chartExtRequest, datasetTable.getId()), chartExtRequest == null, isCross, dsMap);

            List<DatasetTableFieldDTO> fields = unionDTO.getCurrentDsFields();
            fields = fields.stream().filter(DatasetTableFieldDTO::getChecked).collect(Collectors.toList());

            String[] array = fields.stream()
                    .map(f -> {
                        String alias;
                        if (StringUtils.isEmpty(f.getDataeaseName())) {
                            alias = TableUtils.fieldNameShort(table.getTableAlias() + "_" + f.getOriginName());
                        } else {
                            alias = f.getDataeaseName();
                        }

                        f.setFieldShortName(alias);
                        f.setDataeaseName(f.getFieldShortName());
                        f.setDatasetTableId(datasetTable.getId());
                        String prefix = "";
                        String suffix = "";
                        if (Objects.equals(f.getExtField(), ExtFieldConstant.EXT_NORMAL)) {
                            if (isCross) {
                                prefix = "`";
                                suffix = "`";
                            } else {
                                DsTypeDTO datasourceType = getDatasourceType(dsMap, datasetTable.getDatasourceId());
                                prefix = datasourceType.getPrefix();
                                suffix = datasourceType.getSuffix();
                            }
                        }
                        return table.getTableAlias() + "." + prefix + f.getOriginName() + suffix + " AS " + prefix + alias + suffix;
                    })
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
                getUnionForEdit(datasetTable, table, unionDTO.getChildrenDs(), checkedInfo, unionList, checkedFields, dsMap, chartExtRequest, isCross);
            }
        }
    }

    private Set<Long> getAllDs(List<UnionDTO> union) {
        Set<Long> set = new HashSet<>();
        for (UnionDTO unionDTO : union) {
            Long datasourceId = unionDTO.getCurrentDs().getDatasourceId();
            set.add(datasourceId);
            getChildrenDs(unionDTO.getChildrenDs(), set);
        }
        return set;
    }

    private void getChildrenDs(List<UnionDTO> childrenDs, Set<Long> set) {
        for (UnionDTO unionDTO : childrenDs) {
            set.add(unionDTO.getCurrentDs().getDatasourceId());
            if (!CollectionUtils.isEmpty(unionDTO.getChildrenDs())) {
                getChildrenDs(unionDTO.getChildrenDs(), set);
            }
        }
    }

    private DsTypeDTO getDatasourceType(Map<Long, DatasourceSchemaDTO> dsMap, Long datasourceId) {
        DatasourceSchemaDTO datasourceSchemaDTO = dsMap.get(datasourceId);
        String type;
        if (datasourceSchemaDTO == null) {
            CoreDatasource coreDatasource = coreDatasourceMapper.selectById(datasourceId);
            if (coreDatasource == null) {
                DEException.throwException(Translator.get("i18n_dataset_ds_error") + ",ID:" + datasourceId);
            }
            type = engineManage.getDeEngine().getType();
        } else {
            type = datasourceSchemaDTO.getType();
        }

        if (Arrays.stream(DatasourceConfiguration.DatasourceType.values()).map(DatasourceConfiguration.DatasourceType::getType).toList().contains(type)) {
            DatasourceConfiguration.DatasourceType datasourceType = DatasourceConfiguration.DatasourceType.valueOf(type);
            DsTypeDTO dto = new DsTypeDTO();
            BeanUtils.copyBean(dto, datasourceType);
            return dto;
        } else {
            if (LicenseUtil.licenseValid()) {
                List<XpackPluginsDatasourceVO> xpackPluginsDatasourceVOS = pluginManage.queryPluginDs();
                List<XpackPluginsDatasourceVO> list = xpackPluginsDatasourceVOS.stream().filter(ele -> StringUtils.equals(ele.getType(), type)).toList();
                if (ObjectUtils.isNotEmpty(list)) {
                    XpackPluginsDatasourceVO first = list.getFirst();
                    DsTypeDTO dto = new DsTypeDTO();
                    dto.setName(first.getName());
                    dto.setCatalog(first.getCategory());
                    dto.setType(first.getType());
                    dto.setPrefix(first.getPrefix());
                    dto.setSuffix(first.getSuffix());
                    return dto;
                } else {
                    DEException.throwException("当前数据源插件不存在");
                }
            }
            return null;
        }
    }

    public String subPrefixSuffixChar(String str) {
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

    private SQLObj getUnionTable(DatasetTableDTO currentDs, DatasetTableInfoDTO infoDTO, String tableSchema, int index, List<SqlVariableDetails> parameters, boolean isFromDataSet, boolean isCross, Map<Long, DatasourceSchemaDTO> dsMap) {
        SQLObj tableObj;
        String tableAlias = String.format(SQLConstants.TABLE_ALIAS_PREFIX, index);
        if (StringUtils.equalsIgnoreCase(currentDs.getType(), DatasetTableTypeConstants.DATASET_TABLE_DB)) {
            tableObj = SQLObj.builder().tableSchema(tableSchema).tableName(infoDTO.getTable()).tableAlias(tableAlias).build();
        } else if (StringUtils.equalsIgnoreCase(currentDs.getType(), DatasetTableTypeConstants.DATASET_TABLE_SQL)) {
            Provider provider = ProviderFactory.getProvider(dsMap.entrySet().iterator().next().getValue().getType());
            // parser sql params and replace default value
            String sql = provider.replaceComment(new String(Base64.getDecoder().decode(infoDTO.getSql())));
            sql = SqlparserUtils.handleVariableDefaultValue(sql, currentDs.getSqlVariableDetails(), false, isFromDataSet, parameters, isCross, dsMap, pluginManage);
            // add table schema
            if (isCross) {
                sql = SqlUtils.addSchema(sql, tableSchema);
            }
            tableObj = SQLObj.builder().tableSchema("").tableName("(" + sql + ")").tableAlias(tableAlias).build();
        } else {
            // excel,api
            tableObj = SQLObj.builder().tableSchema(tableSchema).tableName(infoDTO.getTable()).tableAlias(tableAlias).build();
        }
        return tableObj;
    }

    private String putObj2Map(Map<Long, DatasourceSchemaDTO> dsMap, DatasetTableDTO ds, boolean isCross) throws Exception {
        // 通过datasource id校验数据源权限
        BusiPerCheckDTO dto = new BusiPerCheckDTO();
        dto.setId(ds.getDatasourceId());
        dto.setAuthEnum(AuthEnum.READ);
        boolean checked = corePermissionManage.checkAuth(dto);
        if (!checked) {
            DEException.throwException(Translator.get("i18n_no_datasource_permission"));
        }


        String schemaAlias;
        if (StringUtils.equalsIgnoreCase(ds.getType(), DatasetTableType.DB) || StringUtils.equalsIgnoreCase(ds.getType(), DatasetTableType.SQL)) {
            CoreDatasource coreDatasource = coreDatasourceMapper.selectById(ds.getDatasourceId());
            if (coreDatasource == null) {
                DEException.throwException(Translator.get("i18n_dataset_ds_error") + ",ID:" + ds.getDatasourceId());
            }
            if (StringUtils.equalsIgnoreCase("excel", coreDatasource.getType()) || StringUtils.equalsIgnoreCase("api", coreDatasource.getType())) {
                coreDatasource = engineManage.getDeEngine();
            }

            Map map = JsonUtil.parseObject(coreDatasource.getConfiguration(), Map.class);
            if (!isCross && ObjectUtils.isNotEmpty(map.get("schema"))) {
                schemaAlias = (String) map.get("schema");
            } else {
                schemaAlias = String.format(SQLConstants.SCHEMA, coreDatasource.getId());
            }

            if (!dsMap.containsKey(coreDatasource.getId())) {
                DatasourceSchemaDTO datasourceSchemaDTO = new DatasourceSchemaDTO();
                BeanUtils.copyBean(datasourceSchemaDTO, coreDatasource);
                datasourceSchemaDTO.setSchemaAlias(schemaAlias);
                dsMap.put(coreDatasource.getId(), datasourceSchemaDTO);
            }
        } else if (StringUtils.equalsIgnoreCase(ds.getType(), DatasetTableType.Es)){
            CoreDatasource coreDatasource = coreDatasourceMapper.selectById(ds.getDatasourceId());
            schemaAlias = String.format(SQLConstants.SCHEMA, coreDatasource.getId());
            if (!dsMap.containsKey(coreDatasource.getId())) {
                DatasourceSchemaDTO datasourceSchemaDTO = new DatasourceSchemaDTO();
                BeanUtils.copyBean(datasourceSchemaDTO, coreDatasource);
                datasourceSchemaDTO.setSchemaAlias(schemaAlias);
                dsMap.put(coreDatasource.getId(), datasourceSchemaDTO);
            }
        }else {
            CoreDatasource coreDatasource = engineManage.getDeEngine();
            schemaAlias = String.format(SQLConstants.SCHEMA, coreDatasource.getId());
            if (!dsMap.containsKey(coreDatasource.getId())) {
                DatasourceSchemaDTO datasourceSchemaDTO = new DatasourceSchemaDTO();
                BeanUtils.copyBean(datasourceSchemaDTO, coreDatasource);
                datasourceSchemaDTO.setSchemaAlias(schemaAlias);
                dsMap.put(coreDatasource.getId(), datasourceSchemaDTO);
            }
        }
        return schemaAlias;
    }
}
