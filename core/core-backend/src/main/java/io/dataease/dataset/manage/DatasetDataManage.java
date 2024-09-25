package io.dataease.dataset.manage;

import io.dataease.api.chart.dto.DeSortField;
import io.dataease.api.dataset.dto.*;
import io.dataease.api.dataset.union.DatasetGroupInfoDTO;
import io.dataease.api.dataset.union.DatasetTableInfoDTO;
import io.dataease.api.permissions.auth.dto.BusiPerCheckDTO;
import io.dataease.api.permissions.dataset.dto.DataSetRowPermissionsTreeDTO;
import io.dataease.auth.bo.TokenUserBO;
import io.dataease.chart.utils.ChartDataBuild;
import io.dataease.commons.utils.SqlparserUtils;
import io.dataease.constant.AuthEnum;
import io.dataease.dataset.constant.DatasetTableType;
import io.dataease.dataset.utils.DatasetUtils;
import io.dataease.dataset.utils.FieldUtils;
import io.dataease.dataset.utils.TableUtils;
import io.dataease.datasource.dao.auto.entity.CoreDatasource;
import io.dataease.datasource.dao.auto.mapper.CoreDatasourceMapper;
import io.dataease.datasource.manage.EngineManage;
import io.dataease.datasource.utils.DatasourceUtils;
import io.dataease.engine.constant.ExtFieldConstant;
import io.dataease.engine.constant.SQLConstants;
import io.dataease.engine.sql.SQLProvider;
import io.dataease.engine.trans.*;
import io.dataease.engine.utils.SQLUtils;
import io.dataease.engine.utils.Utils;
import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.api.PluginManageApi;
import io.dataease.extensions.datasource.constant.SqlPlaceholderConstants;
import io.dataease.extensions.datasource.dto.*;
import io.dataease.extensions.datasource.factory.ProviderFactory;
import io.dataease.extensions.datasource.model.SQLMeta;
import io.dataease.extensions.datasource.provider.Provider;
import io.dataease.extensions.view.dto.ChartExtFilterDTO;
import io.dataease.extensions.view.dto.ChartExtRequest;
import io.dataease.extensions.view.dto.ColumnPermissionItem;
import io.dataease.extensions.view.dto.SqlVariableDetails;
import io.dataease.i18n.Translator;
import io.dataease.system.manage.CorePermissionManage;
import io.dataease.utils.AuthUtils;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.JsonUtil;
import io.dataease.utils.TreeUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static io.dataease.chart.manage.ChartDataManage.START_END_SEPARATOR;

/**
 * @Author Junjun
 */
@Component
public class DatasetDataManage {
    @Resource
    private DatasetSQLManage datasetSQLManage;
    @Resource
    private CoreDatasourceMapper coreDatasourceMapper;
    @Resource
    private DatasetTableFieldManage datasetTableFieldManage;
    @Resource
    private EngineManage engineManage;
    @Resource
    private DatasetGroupManage datasetGroupManage;
    @Resource
    private PermissionManage permissionManage;
    @Resource
    private DatasetTableSqlLogManage datasetTableSqlLogManage;
    @Autowired(required = false)
    private PluginManageApi pluginManage;
    @Resource
    private CorePermissionManage corePermissionManage;

    private static Logger logger = LoggerFactory.getLogger(DatasetDataManage.class);

    public static final List<String> notFullDs = List.of("mysql", "mariadb", "Excel", "API");

    public List<DatasetTableFieldDTO> getTableFields(DatasetTableDTO datasetTableDTO) throws Exception {
        List<DatasetTableFieldDTO> list = null;
        List<TableField> tableFields = null;
        String type = datasetTableDTO.getType();
        DatasetTableInfoDTO tableInfoDTO = JsonUtil.parseObject(datasetTableDTO.getInfo(), DatasetTableInfoDTO.class);
        if (StringUtils.equalsIgnoreCase(type, DatasetTableType.DB) || StringUtils.equalsIgnoreCase(type, DatasetTableType.SQL)) {
            CoreDatasource coreDatasource = coreDatasourceMapper.selectById(datasetTableDTO.getDatasourceId());
            DatasourceSchemaDTO datasourceSchemaDTO = new DatasourceSchemaDTO();
            if (StringUtils.equalsIgnoreCase("excel", coreDatasource.getType()) || StringUtils.equalsIgnoreCase("api", coreDatasource.getType())) {
                coreDatasource = engineManage.getDeEngine();
            }
            if (StringUtils.isNotEmpty(coreDatasource.getStatus()) && "Error".equalsIgnoreCase(coreDatasource.getStatus())) {
                DEException.throwException(Translator.get("i18n_invalid_ds"));
            }
            BeanUtils.copyBean(datasourceSchemaDTO, coreDatasource);
            datasourceSchemaDTO.setSchemaAlias(String.format(SQLConstants.SCHEMA, datasourceSchemaDTO.getId()));
            Provider provider = ProviderFactory.getProvider(coreDatasource.getType());

            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDsList(Map.of(datasourceSchemaDTO.getId(), datasourceSchemaDTO));
            String sql;
            if (StringUtils.equalsIgnoreCase(type, DatasetTableType.DB)) {
                // add table schema
                sql = TableUtils.tableName2Sql(datasourceSchemaDTO, tableInfoDTO.getTable()) + " LIMIT 0 OFFSET 0";
                // replace schema alias, trans dialect
                sql = Utils.replaceSchemaAlias(sql, datasourceRequest.getDsList());
                sql = provider.transSqlDialect(sql, datasourceRequest.getDsList());
            } else {
                // parser sql params and replace default value
                String originSql = provider.replaceComment(new String(Base64.getDecoder().decode(tableInfoDTO.getSql())));
                originSql = SqlparserUtils.handleVariableDefaultValue(originSql, datasetTableDTO.getSqlVariableDetails(), false, false, null, false, datasourceRequest.getDsList(), pluginManage);
                // add sql table schema

                sql = SQLUtils.buildOriginPreviewSql(SqlPlaceholderConstants.TABLE_PLACEHOLDER, 0, 0);
                sql = provider.transSqlDialect(sql, datasourceRequest.getDsList());
                // replace placeholder
                sql = provider.replaceTablePlaceHolder(sql, originSql);
            }
            datasourceRequest.setQuery(sql.replaceAll("\r\n", " ")
                    .replaceAll("\n", " "));
            logger.debug("calcite data table field sql: " + datasourceRequest.getQuery());
            // 获取数据源表的原始字段
            if (StringUtils.equalsIgnoreCase(type, DatasetTableType.DB)) {
                datasourceRequest.setTable(tableInfoDTO.getTable());
            }

            tableFields = provider.fetchTableField(datasourceRequest);
        } else if (StringUtils.equalsIgnoreCase(type, DatasetTableType.Es)) {
            CoreDatasource coreDatasource = coreDatasourceMapper.selectById(datasetTableDTO.getDatasourceId());
            Provider provider = ProviderFactory.getProvider(type);
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            DatasourceSchemaDTO datasourceSchemaDTO = new DatasourceSchemaDTO();
            BeanUtils.copyBean(datasourceSchemaDTO, coreDatasource);
            datasourceRequest.setDatasource(datasourceSchemaDTO);
            datasourceRequest.setTable(datasetTableDTO.getTableName());
            tableFields = provider.fetchTableField(datasourceRequest);
        } else {
            // excel,api
            CoreDatasource coreDatasource = engineManage.getDeEngine();
            DatasourceSchemaDTO datasourceSchemaDTO = new DatasourceSchemaDTO();
            BeanUtils.copyBean(datasourceSchemaDTO, coreDatasource);
            datasourceSchemaDTO.setSchemaAlias(String.format(SQLConstants.SCHEMA, datasourceSchemaDTO.getId()));
            Provider provider = ProviderFactory.getDefaultProvider();

            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDsList(Map.of(datasourceSchemaDTO.getId(), datasourceSchemaDTO));
            String sql = TableUtils.tableName2Sql(datasourceSchemaDTO, tableInfoDTO.getTable()) + " LIMIT 0 OFFSET 0";
            // replace schema alias, trans dialect
            sql = Utils.replaceSchemaAlias(sql, datasourceRequest.getDsList());
            sql = provider.transSqlDialect(sql, datasourceRequest.getDsList());
            datasourceRequest.setQuery(sql);
            logger.debug("calcite data table field sql: " + datasourceRequest.getQuery());
            tableFields = provider.fetchTableField(datasourceRequest);
        }
        return transFields(tableFields, true);
    }

    public List<DatasetTableFieldDTO> transFields(List<TableField> tableFields, boolean defaultStatus) {
        return tableFields.stream().map(ele -> {
            DatasetTableFieldDTO dto = new DatasetTableFieldDTO();
            dto.setName(StringUtils.isNotEmpty(ele.getName()) ? ele.getName() : ele.getOriginName());
            dto.setOriginName(ele.getOriginName());
            dto.setChecked(defaultStatus);
            dto.setType(ele.getType());
            int deType = FieldUtils.transType2DeType(ele.getType());
            dto.setDeExtractType(ObjectUtils.isEmpty(ele.getDeExtractType()) ? deType : ele.getDeExtractType());
            dto.setDeType(ObjectUtils.isEmpty(ele.getDeType()) ? deType : ele.getDeType());
            dto.setGroupType(FieldUtils.transDeType2DQ(dto.getDeType()));
            dto.setExtField(0);
            dto.setDescription(StringUtils.isNotEmpty(ele.getName()) ? ele.getName() : null);
            return dto;
        }).collect(Collectors.toList());
    }

    public Map<String, Object> previewDataWithLimit(DatasetGroupInfoDTO datasetGroupInfoDTO, Integer start, Integer count, boolean checkPermission) throws Exception {
        Map<String, Object> sqlMap = datasetSQLManage.getUnionSQLForEdit(datasetGroupInfoDTO, null);
        String sql = (String) sqlMap.get("sql");

        // 获取allFields
        List<DatasetTableFieldDTO> fields = datasetGroupInfoDTO.getAllFields();
        if (ObjectUtils.isEmpty(fields)) {
            DEException.throwException(Translator.get("i18n_no_fields"));
        }

        Map<String, ColumnPermissionItem> desensitizationList = new HashMap<>();
        if (checkPermission) {
            fields = permissionManage.filterColumnPermissions(fields, desensitizationList, datasetGroupInfoDTO.getId(), null);
            if (ObjectUtils.isEmpty(fields)) {
                DEException.throwException(Translator.get("i18n_no_column_permission"));
            }
        }
        buildFieldName(sqlMap, fields);
        Map<Long, DatasourceSchemaDTO> dsMap = (Map<Long, DatasourceSchemaDTO>) sqlMap.get("dsMap");
        DatasourceUtils.checkDsStatus(dsMap);
        List<String> dsList = new ArrayList<>();
        for (Map.Entry<Long, DatasourceSchemaDTO> next : dsMap.entrySet()) {
            dsList.add(next.getValue().getType());
        }
        boolean needOrder = Utils.isNeedOrder(dsList);
        boolean crossDs = Utils.isCrossDs(dsMap);
        if (!crossDs) {
            if (notFullDs.contains(dsMap.entrySet().iterator().next().getValue().getType()) && (boolean) sqlMap.get("isFullJoin")) {
                DEException.throwException(Translator.get("i18n_not_full"));
            }
            sql = Utils.replaceSchemaAlias(sql, dsMap);
        }
        List<DataSetRowPermissionsTreeDTO> rowPermissionsTree = new ArrayList<>();
        TokenUserBO user = AuthUtils.getUser();
        if (user != null && checkPermission) {
            rowPermissionsTree = permissionManage.getRowPermissionsTree(datasetGroupInfoDTO.getId(), user.getUserId());
        }
        Provider provider;
        if (crossDs) {
            provider = ProviderFactory.getDefaultProvider();
        } else {
            provider = ProviderFactory.getProvider(dsList.getFirst());
        }

        // build query sql
        SQLMeta sqlMeta = new SQLMeta();
        Table2SQLObj.table2sqlobj(sqlMeta, null, "(" + sql + ")", crossDs);
        Field2SQLObj.field2sqlObj(sqlMeta, fields, fields, crossDs, dsMap, Utils.getParams(fields), null, pluginManage);
        WhereTree2Str.transFilterTrees(sqlMeta, rowPermissionsTree, fields, crossDs, dsMap, Utils.getParams(fields), null, pluginManage);
        Order2SQLObj.getOrders(sqlMeta, datasetGroupInfoDTO.getSortFields(), fields, crossDs, dsMap, Utils.getParams(fields), null, pluginManage);
        String querySQL;
        if (start == null || count == null) {
            querySQL = SQLProvider.createQuerySQL(sqlMeta, false, needOrder, false);
        } else {
            querySQL = SQLProvider.createQuerySQLWithLimit(sqlMeta, false, needOrder, false, start, count);
        }
        querySQL = provider.rebuildSQL(querySQL, sqlMeta, crossDs, dsMap);
        logger.debug("calcite data preview sql: " + querySQL);

        // 通过数据源请求数据
        // 调用数据源的calcite获得data
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setQuery(querySQL);
        datasourceRequest.setDsList(dsMap);
        Map<String, Object> data = provider.fetchResultField(datasourceRequest);

        Map<String, Object> map = new LinkedHashMap<>();
        // 重新构造data
        Map<String, Object> previewData = buildPreviewData(data, fields, desensitizationList);
        map.put("data", previewData);
        if (ObjectUtils.isEmpty(datasetGroupInfoDTO.getId())) {
            map.put("allFields", fields);
        } else {
            List<DatasetTableFieldDTO> fieldList = datasetTableFieldManage.selectByDatasetGroupId(datasetGroupInfoDTO.getId());
            map.put("allFields", fieldList);
        }
        map.put("sql", Base64.getEncoder().encodeToString(querySQL.getBytes()));
        return map;
    }

    public Long getDatasetTotal(Long datasetGroupId) throws Exception {
        DatasetGroupInfoDTO dto = datasetGroupManage.getForCount(datasetGroupId);
        if (StringUtils.equalsIgnoreCase(dto.getNodeType(), "dataset")) {
            return getDatasetTotal(dto, null, new ChartExtRequest());
        }
        return 0L;
    }

    public Long getDatasetCountWithWhere(Long datasetGroupId) throws Exception {
        DatasetGroupInfoDTO datasetGroupInfoDTO = datasetGroupManage.getForCount(datasetGroupId);
        Map<String, Object> sqlMap = datasetSQLManage.getUnionSQLForEdit(datasetGroupInfoDTO, null);
        String sql = (String) sqlMap.get("sql");

        // 获取allFields
        List<DatasetTableFieldDTO> fields = datasetGroupInfoDTO.getAllFields();
        if (ObjectUtils.isEmpty(fields)) {
            DEException.throwException(Translator.get("i18n_no_fields"));
        }

        buildFieldName(sqlMap, fields);

        Map<Long, DatasourceSchemaDTO> dsMap = (Map<Long, DatasourceSchemaDTO>) sqlMap.get("dsMap");
        DatasourceUtils.checkDsStatus(dsMap);
        List<String> dsList = new ArrayList<>();
        for (Map.Entry<Long, DatasourceSchemaDTO> next : dsMap.entrySet()) {
            dsList.add(next.getValue().getType());
        }
        boolean crossDs = Utils.isCrossDs(dsMap);
        if (!crossDs) {
            if (notFullDs.contains(dsMap.entrySet().iterator().next().getValue().getType()) && (boolean) sqlMap.get("isFullJoin")) {
                DEException.throwException(Translator.get("i18n_not_full"));
            }
            sql = Utils.replaceSchemaAlias(sql, dsMap);
        }

        List<DataSetRowPermissionsTreeDTO> rowPermissionsTree = new ArrayList<>();
        TokenUserBO user = AuthUtils.getUser();
        if (user != null) {
            rowPermissionsTree = permissionManage.getRowPermissionsTree(datasetGroupInfoDTO.getId(), user.getUserId());
        }

        Provider provider;
        if (crossDs) {
            provider = ProviderFactory.getDefaultProvider();
        } else {
            provider = ProviderFactory.getProvider(dsList.getFirst());
        }

        // build query sql
        SQLMeta sqlMeta = new SQLMeta();
        Table2SQLObj.table2sqlobj(sqlMeta, null, "(" + sql + ")", crossDs);
        Field2SQLObj.field2sqlObj(sqlMeta, fields, fields, crossDs, dsMap, Utils.getParams(fields), null, pluginManage);
        WhereTree2Str.transFilterTrees(sqlMeta, rowPermissionsTree, fields, crossDs, dsMap, Utils.getParams(fields), null, pluginManage);
        Order2SQLObj.getOrders(sqlMeta, datasetGroupInfoDTO.getSortFields(), fields, crossDs, dsMap, Utils.getParams(fields), null, pluginManage);
        String replaceSql = provider.rebuildSQL(SQLProvider.createQuerySQL(sqlMeta, false, false, false), sqlMeta, crossDs, dsMap);
        return getDatasetTotal(datasetGroupInfoDTO, replaceSql, null);
    }

    public Long getDatasetTotal(DatasetGroupInfoDTO datasetGroupInfoDTO, String s, ChartExtRequest request) throws Exception {
        Map<String, Object> sqlMap = datasetSQLManage.getUnionSQLForEdit(datasetGroupInfoDTO, request);
        Map<Long, DatasourceSchemaDTO> dsMap = (Map<Long, DatasourceSchemaDTO>) sqlMap.get("dsMap");
        boolean crossDs = Utils.isCrossDs(dsMap);
        String sql;
        if (StringUtils.isEmpty(s)) {
            sql = (String) sqlMap.get("sql");
            if (!crossDs) {
                sql = Utils.replaceSchemaAlias(sql, dsMap);
            }
        } else {
            sql = s;
        }
        String querySQL = "SELECT COUNT(*) FROM (" + sql + ") t_a_0";
        logger.debug("calcite data count sql: " + querySQL);

        // 通过数据源请求数据
        // 调用数据源的calcite获得data
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setQuery(querySQL);
        datasourceRequest.setDsList(dsMap);

        Provider provider;
        if (crossDs) {
            provider = ProviderFactory.getDefaultProvider();
        } else {
            provider = ProviderFactory.getProvider(dsMap.entrySet().iterator().next().getValue().getType());
        }
        Map<String, Object> data = provider.fetchResultField(datasourceRequest);
        List<String[]> dataList = (List<String[]>) data.get("data");
        if (ObjectUtils.isNotEmpty(dataList) && ObjectUtils.isNotEmpty(dataList.get(0)) && ObjectUtils.isNotEmpty(dataList.get(0)[0])) {
            return Long.valueOf(dataList.get(0)[0]);
        }
        return 0L;
    }

    public Map<String, Object> previewSqlWithLog(PreviewSqlDTO dto) {
        if (dto == null) {
            return null;
        }
        SqlLogDTO sqlLogDTO = new SqlLogDTO();
        String sql = new String(Base64.getDecoder().decode(dto.getSql()));
        sqlLogDTO.setSql(sql);
        Map<String, Object> map = null;
        try {
            sqlLogDTO.setStartTime(System.currentTimeMillis());
            map = previewSql(dto);
            sqlLogDTO.setEndTime(System.currentTimeMillis());
            sqlLogDTO.setSpend(sqlLogDTO.getEndTime() - sqlLogDTO.getStartTime());
            sqlLogDTO.setStatus("Completed");
        } catch (Exception e) {
            sqlLogDTO.setStatus("Error");
            DEException.throwException(e.getMessage());
        } finally {
            if (ObjectUtils.isNotEmpty(dto.getTableId())) {
                sqlLogDTO.setTableId(dto.getTableId());
                datasetTableSqlLogManage.save(sqlLogDTO);
            }
        }
        return map;
    }

    public Map<String, Object> previewSql(PreviewSqlDTO dto) throws DEException {
        CoreDatasource coreDatasource = coreDatasourceMapper.selectById(dto.getDatasourceId());
        DatasourceSchemaDTO datasourceSchemaDTO = new DatasourceSchemaDTO();
        if (coreDatasource.getType().equalsIgnoreCase("API") || coreDatasource.getType().equalsIgnoreCase("Excel")) {
            BeanUtils.copyBean(datasourceSchemaDTO, engineManage.getDeEngine());
        } else {
            BeanUtils.copyBean(datasourceSchemaDTO, coreDatasource);
        }

        if (StringUtils.isNotEmpty(datasourceSchemaDTO.getStatus()) && "Error".equalsIgnoreCase(datasourceSchemaDTO.getStatus())) {
            DEException.throwException(Translator.get("i18n_invalid_ds"));
        }

        String alias = String.format(SQLConstants.SCHEMA, datasourceSchemaDTO.getId());
        datasourceSchemaDTO.setSchemaAlias(alias);

        Map<Long, DatasourceSchemaDTO> dsMap = new LinkedHashMap<>();
        dsMap.put(datasourceSchemaDTO.getId(), datasourceSchemaDTO);
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDsList(dsMap);
        Provider provider = ProviderFactory.getProvider(datasourceSchemaDTO.getType());

        // parser sql params and replace default value

        String originSql = provider.replaceComment(new String(Base64.getDecoder().decode(dto.getSql())));
        originSql = SqlparserUtils.handleVariableDefaultValue(datasetSQLManage.subPrefixSuffixChar(originSql), dto.getSqlVariableDetails(), true, true, null, false, dsMap, pluginManage);

        // sql 作为临时表，外层加上limit
        String sql;

        if (Utils.isNeedOrder(List.of(datasourceSchemaDTO.getType()))) {
            // 先根据sql获取表字段
            String sqlField = SQLUtils.buildOriginPreviewSql(SqlPlaceholderConstants.TABLE_PLACEHOLDER, 0, 0);

            sqlField = provider.transSqlDialect(sqlField, datasourceRequest.getDsList());
            // replace placeholder
            sqlField = provider.replaceTablePlaceHolder(sqlField, originSql);
            datasourceRequest.setQuery(sqlField);

            // 获取数据源表的原始字段
            List<TableField> list = provider.fetchTableField(datasourceRequest);
            if (ObjectUtils.isEmpty(list)) {
                return null;
            }
            sql = SQLUtils.buildOriginPreviewSqlWithOrderBy(SqlPlaceholderConstants.TABLE_PLACEHOLDER, 100, 0, String.format(SQLConstants.FIELD_DOT, list.get(0).getOriginName()) + " ASC ");
        } else {
            sql = SQLUtils.buildOriginPreviewSql(SqlPlaceholderConstants.TABLE_PLACEHOLDER, 100, 0);
        }
        sql = provider.transSqlDialect(sql, datasourceRequest.getDsList());
        // replace placeholder
        sql = provider.replaceTablePlaceHolder(sql, originSql);

        logger.debug("calcite data preview sql: " + sql);
        datasourceRequest.setQuery(sql);
        Map<String, Object> data = provider.fetchResultField(datasourceRequest);
        // 重新构造data
        List<TableField> fList = (List<TableField>) data.get("fields");
        List<DatasetTableFieldDTO> fields = transFields(fList, false);
        Map<String, Object> previewData = buildPreviewData(data, fields, new HashMap<>());
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("data", previewData);
        map.put("sql", Base64.getEncoder().encodeToString(sql.getBytes()));
        return map;
    }

    public Map<String, Object> buildPreviewData(Map<String, Object> data, List<DatasetTableFieldDTO> fields, Map<String, ColumnPermissionItem> desensitizationList) {
        Map<String, Object> map = new LinkedHashMap<>();
        List<String[]> dataList = (List<String[]>) data.get("data");
        List<LinkedHashMap<String, Object>> dataObjectList = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(dataList)) {
            for (int i = 0; i < dataList.size(); i++) {
                String[] row = dataList.get(i);
                LinkedHashMap<String, Object> obj = new LinkedHashMap<>();
                if (row.length > 0) {
                    for (int j = 0; j < fields.size(); j++) {
                        if (desensitizationList.keySet().contains(fields.get(j).getDataeaseName())) {
                            obj.put(fields.get(j).getDataeaseName(), ChartDataBuild.desensitizationValue(desensitizationList.get(fields.get(j).getDataeaseName()), String.valueOf(row[j])));
                        } else {
                            obj.put(ObjectUtils.isNotEmpty(fields.get(j).getDataeaseName()) ?
                                    fields.get(j).getDataeaseName() : fields.get(j).getOriginName(), row[j]);
                        }
                    }
                }
                dataObjectList.add(obj);
            }
        }

        map.put("fields", fields);
        map.put("data", dataObjectList);
        return map;
    }

    public void buildFieldName(Map<String, Object> sqlMap, List<DatasetTableFieldDTO> fields) {
        // 获取内层union sql和字段
        List<DatasetTableFieldDTO> unionFields = (List<DatasetTableFieldDTO>) sqlMap.get("field");
        for (DatasetTableFieldDTO datasetTableFieldDTO : fields) {
            DatasetTableFieldDTO dto = datasetTableFieldManage.selectById(datasetTableFieldDTO.getId());
            if (ObjectUtils.isEmpty(dto)) {
                if (Objects.equals(datasetTableFieldDTO.getExtField(), ExtFieldConstant.EXT_NORMAL)) {
                    for (DatasetTableFieldDTO fieldDTO : unionFields) {
                        if (Objects.equals(datasetTableFieldDTO.getDatasetTableId(), fieldDTO.getDatasetTableId())
                                && Objects.equals(datasetTableFieldDTO.getOriginName(), fieldDTO.getOriginName())) {
                            datasetTableFieldDTO.setDataeaseName(fieldDTO.getDataeaseName());
                            datasetTableFieldDTO.setFieldShortName(fieldDTO.getFieldShortName());
                        }
                    }
                }
                if (Objects.equals(datasetTableFieldDTO.getExtField(), ExtFieldConstant.EXT_CALC)) {
                    String dataeaseName = TableUtils.fieldNameShort(datasetTableFieldDTO.getId() + "_" + datasetTableFieldDTO.getOriginName());
                    datasetTableFieldDTO.setDataeaseName(dataeaseName);
                    datasetTableFieldDTO.setFieldShortName(dataeaseName);
                    datasetTableFieldDTO.setDeExtractType(datasetTableFieldDTO.getDeType());
                }
            } else {
                datasetTableFieldDTO.setDataeaseName(dto.getDataeaseName());
                datasetTableFieldDTO.setFieldShortName(dto.getFieldShortName());
            }
        }
    }

    public List<String> getFieldEnum(MultFieldValuesRequest multFieldValuesRequest) throws Exception {
        // 根据前端传的查询组件field ids，获取所有字段枚举值并去重合并
        List<List<String>> list = new ArrayList<>();
        for (Long id : multFieldValuesRequest.getFieldIds()) {
            DatasetTableFieldDTO field = datasetTableFieldManage.selectById(id);
            if (field == null) {
                DEException.throwException(Translator.get("i18n_no_field"));
            }
            List<DatasetTableFieldDTO> allFields = new ArrayList<>();
            // 根据图表计算字段，获取数据集
            Long datasetGroupId = field.getDatasetGroupId();

            // check permission
            BusiPerCheckDTO dto = new BusiPerCheckDTO();
            dto.setId(datasetGroupId);
            dto.setAuthEnum(AuthEnum.READ);
            boolean checked = corePermissionManage.checkAuth(dto);
            if (!checked) {
                DEException.throwException(Translator.get("i18n_no_dataset_permission"));
            }
            if (field.getChartId() != null) {
                allFields.addAll(datasetTableFieldManage.getChartCalcFields(field.getChartId()));
            }
            DatasetGroupInfoDTO datasetGroupInfoDTO = datasetGroupManage.getDatasetGroupInfoDTO(datasetGroupId, null);

            Map<String, Object> sqlMap = datasetSQLManage.getUnionSQLForEdit(datasetGroupInfoDTO, new ChartExtRequest());
            String sql = (String) sqlMap.get("sql");

            allFields.addAll(datasetGroupInfoDTO.getAllFields());

            Map<Long, DatasourceSchemaDTO> dsMap = (Map<Long, DatasourceSchemaDTO>) sqlMap.get("dsMap");
            boolean crossDs = Utils.isCrossDs(dsMap);
            if (!crossDs) {
                sql = Utils.replaceSchemaAlias(sql, dsMap);
            }

            // build query sql
            SQLMeta sqlMeta = new SQLMeta();
            Table2SQLObj.table2sqlobj(sqlMeta, null, "(" + sql + ")", crossDs);

            // 获取allFields
            List<DatasetTableFieldDTO> fields = Collections.singletonList(field);
            Map<String, ColumnPermissionItem> desensitizationList = new HashMap<>();
            fields = permissionManage.filterColumnPermissions(fields, desensitizationList, datasetGroupInfoDTO.getId(), null);
            if (ObjectUtils.isEmpty(fields)) {
                DEException.throwException(Translator.get("i18n_no_column_permission"));
            }
            buildFieldName(sqlMap, fields);

            List<String> dsList = new ArrayList<>();
            for (Map.Entry<Long, DatasourceSchemaDTO> next : dsMap.entrySet()) {
                dsList.add(next.getValue().getType());
            }
            boolean needOrder = Utils.isNeedOrder(dsList);

            List<DataSetRowPermissionsTreeDTO> rowPermissionsTree = new ArrayList<>();
            TokenUserBO user = AuthUtils.getUser();
            if (user != null) {
                rowPermissionsTree = permissionManage.getRowPermissionsTree(datasetGroupInfoDTO.getId(), user.getUserId());
            }

            Provider provider;
            if (crossDs) {
                provider = ProviderFactory.getDefaultProvider();
            } else {
                provider = ProviderFactory.getProvider(dsList.getFirst());
            }

            Field2SQLObj.field2sqlObj(sqlMeta, fields, allFields, crossDs, dsMap, Utils.getParams(allFields), null, pluginManage);
            WhereTree2Str.transFilterTrees(sqlMeta, rowPermissionsTree, allFields, crossDs, dsMap, Utils.getParams(allFields), null, pluginManage);
            Order2SQLObj.getOrders(sqlMeta, datasetGroupInfoDTO.getSortFields(), allFields, crossDs, dsMap, Utils.getParams(allFields), null, pluginManage);
            String querySQL;
            if (multFieldValuesRequest.getResultMode() == 0) {
                querySQL = SQLProvider.createQuerySQLWithLimit(sqlMeta, false, needOrder, true, 0, 1000);
            } else {
                querySQL = SQLProvider.createQuerySQL(sqlMeta, false, needOrder, true);
            }
            querySQL = provider.rebuildSQL(querySQL, sqlMeta, crossDs, dsMap);
            logger.debug("calcite data enum sql: " + querySQL);

            // 通过数据源请求数据
            // 调用数据源的calcite获得data
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setQuery(querySQL);
            datasourceRequest.setDsList(dsMap);

            Map<String, Object> data = provider.fetchResultField(datasourceRequest);
            List<String[]> dataList = (List<String[]>) data.get("data");
            dataList = dataList.stream().filter(row -> {
                boolean hasEmpty = false;
                for (String s : row) {
                    if (StringUtils.isBlank(s)) {
                        hasEmpty = true;
                        break;
                    }
                }
                return !hasEmpty;
            }).toList();
            List<String> previewData = new ArrayList<>();
            if (ObjectUtils.isNotEmpty(dataList)) {
                List<String> tmpData = dataList.stream().map(ele -> (ObjectUtils.isNotEmpty(ele) && ele.length > 0) ? ele[0] : null).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(tmpData)) {
                    if (desensitizationList.keySet().contains(field.getDataeaseName())) {
                        for (int i = 0; i < tmpData.size(); i++) {
                            previewData.add(ChartDataBuild.desensitizationValue(desensitizationList.get(field.getDataeaseName()), tmpData.get(i)));
                        }
                    } else {
                        previewData = tmpData;
                    }
                }
                list.add(previewData);
            }
        }

        // 重新构造data
        Set<String> result = new LinkedHashSet<>();
        for (List<String> l : list) {
            result.addAll(l);
        }
        return result.stream().toList();
    }

    public List<Map<String, Object>> getFieldEnumObj(EnumValueRequest request) throws Exception {
        List<Long> ids = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(request.getQueryId())) {
            ids.add(request.getQueryId());
        }
        if (ObjectUtils.isNotEmpty(request.getDisplayId())) {
            ids.add(request.getDisplayId());
        }

        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        if (ids.size() == 2 && Objects.equals(ids.get(0), ids.get(1))) {
            ids.remove(1);
        }

        SQLMeta sqlMeta = new SQLMeta();
        DatasetGroupInfoDTO datasetGroupInfoDTO = null;
        List<DatasetTableFieldDTO> fields = new ArrayList<>();
        Map<String, Object> sqlMap = null;
        boolean crossDs = false;
        Map<Long, DatasourceSchemaDTO> dsMap = null;

        if (ObjectUtils.isNotEmpty(request.getSortId())) {
            // 如果排序字段和查询字段显示字段不一致，则加入到查询列表中
            if (!request.getSortId().equals(request.getQueryId()) && !request.getSortId().equals(request.getDisplayId())) {
                ids.add(request.getSortId());
            }
        }

        List<DatasetTableFieldDTO> allFields = new ArrayList<>();

        for (Long id : ids) {
            DatasetTableFieldDTO field = datasetTableFieldManage.selectById(id);
            if (field == null) {
                DEException.throwException(Translator.get("i18n_no_field"));
            }

            // 根据图表计算字段，获取数据集
            Long datasetGroupId = field.getDatasetGroupId();

            // check permission
            BusiPerCheckDTO dto = new BusiPerCheckDTO();
            dto.setId(datasetGroupId);
            dto.setAuthEnum(AuthEnum.READ);
            boolean checked = corePermissionManage.checkAuth(dto);
            if (!checked) {
                DEException.throwException(Translator.get("i18n_no_dataset_permission"));
            }

            if (field.getChartId() != null) {
                allFields.addAll(datasetTableFieldManage.getChartCalcFields(field.getChartId()));
            }
            datasetGroupInfoDTO = datasetGroupManage.getDatasetGroupInfoDTO(datasetGroupId, null);

            sqlMap = datasetSQLManage.getUnionSQLForEdit(datasetGroupInfoDTO, new ChartExtRequest());
            String sql = (String) sqlMap.get("sql");

            allFields.addAll(datasetGroupInfoDTO.getAllFields());

            dsMap = (Map<Long, DatasourceSchemaDTO>) sqlMap.get("dsMap");
            crossDs = Utils.isCrossDs(dsMap);
            if (!crossDs) {
                sql = Utils.replaceSchemaAlias(sql, dsMap);
            }

            // build query sql
            Table2SQLObj.table2sqlobj(sqlMeta, null, "(" + sql + ")", crossDs);
            fields.add(field);
        }

        // 获取allFields
        Map<String, ColumnPermissionItem> desensitizationList = new HashMap<>();
        fields = permissionManage.filterColumnPermissions(fields, desensitizationList, datasetGroupInfoDTO.getId(), null);
        if (ObjectUtils.isEmpty(fields)) {
            DEException.throwException(Translator.get("i18n_no_column_permission"));
        }
        buildFieldName(sqlMap, fields);

        List<String> dsList = new ArrayList<>();
        for (Map.Entry<Long, DatasourceSchemaDTO> next : dsMap.entrySet()) {
            dsList.add(next.getValue().getType());
        }
        boolean needOrder = Utils.isNeedOrder(dsList);

        List<DataSetRowPermissionsTreeDTO> rowPermissionsTree = new ArrayList<>();
        TokenUserBO user = AuthUtils.getUser();
        if (user != null) {
            rowPermissionsTree = permissionManage.getRowPermissionsTree(datasetGroupInfoDTO.getId(), user.getUserId());
        }

        //组件过滤条件
        List<ChartExtFilterDTO> extFilterList = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(request.getFilter())) {
            for (ChartExtFilterDTO filterDTO : request.getFilter()) {
                // 解析多个fieldId,fieldId是一个逗号分隔的字符串
                String fieldId = filterDTO.getFieldId();
                if (filterDTO.getIsTree() == null) {
                    filterDTO.setIsTree(false);
                }

                boolean hasParameters = false;
                List<SqlVariableDetails> sqlVariables = datasetGroupManage.getSqlParams(Arrays.asList(datasetGroupInfoDTO.getId()));
                if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(sqlVariables)) {
                    for (SqlVariableDetails parameter : Optional.ofNullable(filterDTO.getParameters()).orElse(new ArrayList<>())) {
                        String parameterId = StringUtils.endsWith(parameter.getId(), START_END_SEPARATOR) ? parameter.getId().split(START_END_SEPARATOR)[0] : parameter.getId();
                        if (sqlVariables.stream().map(SqlVariableDetails::getId).collect(Collectors.toList()).contains(parameterId)) {
                            hasParameters = true;
                        }
                    }
                }

                if (hasParameters) {
                    continue;
                }

                if (StringUtils.isNotEmpty(fieldId)) {
                    List<Long> fieldIds = Arrays.stream(fieldId.split(",")).map(Long::valueOf).collect(Collectors.toList());

                    if (filterDTO.getIsTree()) {
                        ChartExtFilterDTO filterRequest = new ChartExtFilterDTO();
                        BeanUtils.copyBean(filterRequest, filterDTO);
                        filterRequest.setDatasetTableFieldList(new ArrayList<>());
                        for (Long fId : fieldIds) {
                            DatasetTableFieldDTO datasetTableField = datasetTableFieldManage.selectById(fId);
                            if (datasetTableField == null) {
                                continue;
                            }
                            if (Objects.equals(datasetTableField.getDatasetGroupId(), datasetGroupInfoDTO.getId())) {
                                filterRequest.getDatasetTableFieldList().add(datasetTableField);
                            }
                        }
                        if (ObjectUtils.isNotEmpty(filterRequest.getDatasetTableFieldList())) {
                            extFilterList.add(filterRequest);
                        }
                    } else {
                        for (Long fId : fieldIds) {
                            ChartExtFilterDTO filterRequest = new ChartExtFilterDTO();
                            BeanUtils.copyBean(filterRequest, filterDTO);
                            filterRequest.setFieldId(fId + "");

                            DatasetTableFieldDTO datasetTableField = datasetTableFieldManage.selectById(fId);
                            if (datasetTableField == null) {
                                continue;
                            }
                            filterRequest.setDatasetTableField(datasetTableField);
                            if (Objects.equals(datasetTableField.getDatasetGroupId(), datasetGroupInfoDTO.getId())) {
                                extFilterList.add(filterRequest);
                            }
                        }
                    }
                }
            }
        }

        // 搜索备选项
        if (StringUtils.isNotEmpty(request.getSearchText())) {
            ChartExtFilterDTO dto = new ChartExtFilterDTO();
            DatasetTableFieldDTO field = null;
            if (ids.size() == 1) {
                field = datasetTableFieldManage.selectById(ids.get(0));
            } else {
                field = datasetTableFieldManage.selectById(ids.get(1));
            }
            dto.setDatasetTableField(field);
            dto.setFieldId(field.getId() + "");
            dto.setIsTree(false);
            dto.setOperator("like");
            dto.setValue(List.of(request.getSearchText()));
            extFilterList.add(dto);
        }

        // 排序
        boolean sortDistinct = true;
        if (ObjectUtils.isNotEmpty(request.getSortId())) {
            DatasetTableFieldDTO field = datasetTableFieldManage.selectById(request.getSortId());
            if (field == null) {
                DEException.throwException(Translator.get("i18n_no_field"));
            }
            DeSortField deSortField = new DeSortField();
            BeanUtils.copyBean(deSortField, field);
            deSortField.setOrderDirection(request.getSort());
            datasetGroupInfoDTO.setSortFields(Collections.singletonList(deSortField));
            sortDistinct = false;
        }

        Provider provider;
        if (crossDs) {
            provider = ProviderFactory.getDefaultProvider();
        } else {
            provider = ProviderFactory.getProvider(dsList.getFirst());
        }

        Field2SQLObj.field2sqlObj(sqlMeta, fields, allFields, crossDs, dsMap, Utils.getParams(allFields), null, pluginManage);
        ExtWhere2Str.extWhere2sqlOjb(sqlMeta, extFilterList, allFields, crossDs, dsMap, Utils.getParams(allFields), null, pluginManage);
        WhereTree2Str.transFilterTrees(sqlMeta, rowPermissionsTree, allFields, crossDs, dsMap, Utils.getParams(allFields), null, pluginManage);
        Order2SQLObj.getOrders(sqlMeta, datasetGroupInfoDTO.getSortFields(), allFields, crossDs, dsMap, Utils.getParams(allFields), null, pluginManage);
        String querySQL;
        if (request.getResultMode() == 0) {
            querySQL = SQLProvider.createQuerySQLWithLimit(sqlMeta, false, needOrder, sortDistinct && ids.size() == 1, 0, 1000);
        } else {
            querySQL = SQLProvider.createQuerySQL(sqlMeta, false, needOrder, sortDistinct && ids.size() == 1);
        }
        querySQL = provider.rebuildSQL(querySQL, sqlMeta, crossDs, dsMap);
        logger.debug("calcite data enum sql: " + querySQL);

        // 通过数据源请求数据
        // 调用数据源的calcite获得data
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setQuery(querySQL);
        datasourceRequest.setDsList(dsMap);

        Map<String, Object> data = provider.fetchResultField(datasourceRequest);
        List<String[]> dataList = (List<String[]>) data.get("data");
        dataList = dataList.stream().filter(row -> {
            boolean hasEmpty = false;
            for (String s : row) {
                if (StringUtils.isBlank(s)) {
                    hasEmpty = true;
                    break;
                }
            }
            return !hasEmpty;
        }).toList();
        Map<String, String[]> distinctData = new LinkedHashMap<>();
        for (String[] arr : dataList) {
            String key = Arrays.toString(arr);
            if (!distinctData.containsKey(key)) {
                distinctData.put(key, arr);
            }
        }

        List<String[]> distinctDataList = new ArrayList<>();
        for (Map.Entry<String, String[]> ele : distinctData.entrySet()) {
            distinctDataList.add(ele.getValue());
        }

        List<Map<String, Object>> previewData = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(distinctDataList)) {
            for (String[] ele : distinctDataList) {
                Map<String, Object> map = new LinkedHashMap<>();
                for (int i = 0; i < fields.size(); i++) {
                    String val = ele[i];
                    DatasetTableFieldDTO field = fields.get(i);
                    if (desensitizationList.containsKey(field.getDataeaseName())) {
                        String str = ChartDataBuild.desensitizationValue(desensitizationList.get(field.getDataeaseName()), val);
                        map.put(field.getId() + "", str);
                    } else {
                        map.put(field.getId() + "", val);
                    }
                }
                previewData.add(map);
            }
        }
        return previewData;
    }

    public List<BaseTreeNodeDTO> getFieldValueTree(MultFieldValuesRequest multFieldValuesRequest) throws Exception {
        List<Long> ids = multFieldValuesRequest.getFieldIds();
        if (ids.isEmpty()) {
            DEException.throwException("no field selected.");
        }
        // 根据前端传的查询组件field ids，获取所有字段枚举值并去重合并
        List<List<String>> list = new ArrayList<>();
        List<DatasetTableFieldDTO> fields = new ArrayList<>();

        // 根据图表计算字段，获取数据集
        List<DatasetTableFieldDTO> allFields = new ArrayList<>();
        DatasetTableFieldDTO field = datasetTableFieldManage.selectById(ids.getFirst());
        Long datasetGroupId = field.getDatasetGroupId();
        if (field.getChartId() != null) {
            allFields.addAll(datasetTableFieldManage.getChartCalcFields(field.getChartId()));
        }
        DatasetGroupInfoDTO datasetGroupInfoDTO = datasetGroupManage.getDatasetGroupInfoDTO(datasetGroupId, null);

        Map<String, Object> sqlMap = datasetSQLManage.getUnionSQLForEdit(datasetGroupInfoDTO, new ChartExtRequest());
        String sql = (String) sqlMap.get("sql");

        allFields.addAll(datasetGroupInfoDTO.getAllFields());

        Map<Long, DatasourceSchemaDTO> dsMap = (Map<Long, DatasourceSchemaDTO>) sqlMap.get("dsMap");
        boolean crossDs = Utils.isCrossDs(dsMap);
        if (!crossDs) {
            sql = Utils.replaceSchemaAlias(sql, dsMap);
        }

        // build query sql
        SQLMeta sqlMeta = new SQLMeta();
        Table2SQLObj.table2sqlobj(sqlMeta, null, "(" + sql + ")", crossDs);

        for (Long id : ids) {
            DatasetTableFieldDTO f = datasetTableFieldManage.selectById(id);
            if (f == null) {
                DEException.throwException(Translator.get("i18n_no_field"));
            }
            // 获取allFields
            fields.add(f);
        }

        Map<String, ColumnPermissionItem> desensitizationList = new HashMap<>();
        fields = permissionManage.filterColumnPermissions(fields, desensitizationList, datasetGroupInfoDTO.getId(), null);
        if (ObjectUtils.isEmpty(fields)) {
            DEException.throwException(Translator.get("i18n_no_column_permission"));
        }
        buildFieldName(sqlMap, fields);

        List<String> dsList = new ArrayList<>();
        for (Map.Entry<Long, DatasourceSchemaDTO> next : dsMap.entrySet()) {
            dsList.add(next.getValue().getType());
        }
        boolean needOrder = Utils.isNeedOrder(dsList);

        List<DataSetRowPermissionsTreeDTO> rowPermissionsTree = new ArrayList<>();
        TokenUserBO user = AuthUtils.getUser();
        if (user != null) {
            rowPermissionsTree = permissionManage.getRowPermissionsTree(datasetGroupInfoDTO.getId(), user.getUserId());
        }

        Provider provider;
        if (crossDs) {
            provider = ProviderFactory.getDefaultProvider();
        } else {
            provider = ProviderFactory.getProvider(dsList.getFirst());
        }

        Field2SQLObj.field2sqlObj(sqlMeta, fields, allFields, crossDs, dsMap, Utils.getParams(allFields), null, pluginManage);
        WhereTree2Str.transFilterTrees(sqlMeta, rowPermissionsTree, allFields, crossDs, dsMap, Utils.getParams(allFields), null, pluginManage);
        Order2SQLObj.getOrders(sqlMeta, datasetGroupInfoDTO.getSortFields(), allFields, crossDs, dsMap, Utils.getParams(allFields), null, pluginManage);
        String querySQL;
        if (multFieldValuesRequest.getResultMode() == 0) {
            querySQL = SQLProvider.createQuerySQLWithLimit(sqlMeta, false, needOrder, false, 0, 1000);
        } else {
            querySQL = SQLProvider.createQuerySQL(sqlMeta, false, needOrder, false);
        }
        querySQL = provider.rebuildSQL(querySQL, sqlMeta, crossDs, dsMap);
        logger.debug("filter tree sql: " + querySQL);

        // 通过数据源请求数据
        // 调用数据源的calcite获得data
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setQuery(querySQL);
        datasourceRequest.setDsList(dsMap);

        Map<String, Object> data = provider.fetchResultField(datasourceRequest);
        List<String[]> rows = (List<String[]>) data.get("data");

        // 重新构造data
        Set<String> pkSet = new HashSet<>();
        rows = rows.stream().filter(row -> {
            boolean hasEmpty = false;
            for (String s : row) {
                if (StringUtils.isBlank(s)) {
                    hasEmpty = true;
                    break;
                }
            }
            return !hasEmpty;
        }).toList();
        List<BaseTreeNodeDTO> treeNodes = rows.stream().map(row -> buildTreeNode(row, pkSet)).flatMap(Collection::stream).collect(Collectors.toList());
        List<BaseTreeNodeDTO> tree = DatasetUtils.mergeDuplicateTree(treeNodes, "root");
        return tree;
    }

    private List<BaseTreeNodeDTO> buildTreeNode(String[] row, Set<String> pkSet) {
        List<BaseTreeNodeDTO> nodes = new ArrayList<>();
        List<String> parentPkList = new ArrayList<>();
        for (int i = 0; i < row.length; i++) {
            String text = row[i];

            parentPkList.add(text);
            String val = String.join(TreeUtils.SEPARATOR, parentPkList);
            String parentVal = i == 0 ? TreeUtils.DEFAULT_ROOT : row[i - 1];
            String pk = String.join(TreeUtils.SEPARATOR, parentPkList);
            if (pkSet.contains(pk)) continue;
            pkSet.add(pk);
            BaseTreeNodeDTO node = new BaseTreeNodeDTO(val, parentVal, StringUtils.isNotBlank(text) ? text.trim() : text, pk + TreeUtils.SEPARATOR + i);
            nodes.add(node);
        }
        return nodes;

    }
}
