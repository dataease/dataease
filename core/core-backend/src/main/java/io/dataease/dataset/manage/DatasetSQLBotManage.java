package io.dataease.dataset.manage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.dataease.api.permissions.dataset.api.DatasetAssistantEnterpriseService;
import io.dataease.api.dataset.union.DatasetGroupInfoDTO;
import io.dataease.api.dataset.union.DatasetTableInfoDTO;
import io.dataease.api.dataset.union.UnionDTO;
import io.dataease.api.dataset.vo.DataSQLBotAssistantVO;
import io.dataease.api.dataset.vo.DataSQLBotDatasetVO;
import io.dataease.api.dataset.vo.SQLBotAssistanTable;
import io.dataease.api.dataset.vo.SQLBotAssistantField;
import io.dataease.api.permissions.dataset.api.ColumnPermissionsApi;
import io.dataease.api.permissions.dataset.api.RowPermissionsApi;
import io.dataease.api.permissions.dataset.dto.DataSetColumnPermissionsDTO;
import io.dataease.api.permissions.dataset.dto.DataSetRowPermissionsTreeDTO;
import io.dataease.api.permissions.user.vo.UserFormVO;
import io.dataease.commons.utils.SqlVariableHandleResult;
import io.dataease.commons.utils.SqlparserUtils;
import io.dataease.constant.ColumnPermissionConstants;
import io.dataease.dao.auto.entity.*;
import io.dataease.dataset.dao.ext.mapper.DataSetAssistantMapper;
import io.dataease.dataset.utils.TableUtils;
import io.dataease.datasource.manage.DataSourceManage;
import io.dataease.datasource.manage.EngineManage;
import io.dataease.engine.constant.ExtFieldConstant;
import io.dataease.engine.sql.SQLProvider;
import io.dataease.engine.trans.Field2SQLObj;
import io.dataease.engine.trans.Order2SQLObj;
import io.dataease.engine.trans.Table2SQLObj;
import io.dataease.engine.trans.WhereTree2Str;
import io.dataease.engine.utils.Utils;
import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.api.PluginManageApi;
import io.dataease.extensions.datasource.dto.*;
import io.dataease.extensions.datasource.factory.ProviderFactory;
import io.dataease.extensions.datasource.model.SQLMeta;
import io.dataease.extensions.datasource.model.SQLObj;
import io.dataease.extensions.datasource.provider.Provider;
import io.dataease.extensions.datasource.vo.Configuration;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
import io.dataease.extensions.view.dto.ColumnPermissionItem;
import io.dataease.extensions.view.dto.ColumnPermissions;
import io.dataease.extensions.view.dto.DatasetRowPermissionsTreeItem;
import io.dataease.extensions.view.dto.DatasetRowPermissionsTreeObj;
import io.dataease.home.manage.DeIndexManage;
import io.dataease.i18n.Translator;
import io.dataease.permission.util.V3UserUtil;
import io.dataease.utils.*;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.sql.Types;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class DatasetSQLBotManage {

    @Resource
    private DeIndexManage deIndexManage;

    @Resource
    private DataSetAssistantMapper dataSetAssistantMapper;

    @Resource
    private EngineManage engineManage;

    @Resource
    private Environment environment;

    private volatile CoreDatasource deEngine;

    @Resource
    private DataSourceManage dataSourceManage;

    @Resource
    private PermissionManage permissionManage;

    @Resource
    private DatasetSQLManage datasetSQLManage;

    @Autowired(required = false)
    private PluginManageApi pluginManage;

    @Autowired(required = false)
    private RowPermissionsApi rowPermissionsApi;

    @Resource
    private JPAQueryFactory queryFactory;

    @Autowired(required = false)
    private DatasetAssistantEnterpriseService enterpriseService;

    @Value("${dataease.sqlbot.encrypt:false}")
    private boolean encryptEnabled;

    @Value("${dataease.sqlbot.aes-key:y5txe1mRmS_JpOrUzFzHEu-kIQn3lf7l}")
    private String aesKey;

    @Value("${dataease.sqlbot.aes-iv:sqlbot_em_aes_iv}")
    private String aesIv;

    @Value("${dataease.sqlbot.log:false}")
    private boolean sqlbotApiLog;

    @Value("${dataease.sqlbot.ds-id-fixed:false}")
    private boolean dsIdFixed;

    private CoreDatasource getDeEngine() {
        if (deEngine == null) {
            synchronized (this) {
                if (deEngine == null) {
                    deEngine = engineManage.getDeEngine();
                }
            }
        }
        return deEngine;
    }

    private String aesEncrypt(String text) {
        String iv = aesIv;
        int len = iv.length();
        if (len > 16) {
            iv = iv.substring(0, 16);
        }
        if (len < 16) {
            iv = String.format("%-16s", iv).replace(' ', '0');
        }
        return AesUtils.aesEncrypt(text, aesKey, iv);
    }

    TypeReference<List<Long>> listTypeReference = new TypeReference<>() {
    };
    TypeReference<List<FieldGroupDTO>> groupTokenType = new TypeReference<>() {
    };
    TypeReference<List<CalParam>> typeToken = new TypeReference<>() {
    };

    private Map<Long, List<DataSetColumnPermissionsDTO>> getColPermission(List<Long> roleIds, boolean proxy) {
        Long uid = V3UserUtil.getUid();
        ColumnPermissionsApi columnPermissionsApi = CommonBeanFactory.getBean(ColumnPermissionsApi.class);
        Objects.requireNonNull(columnPermissionsApi);

        DataSetColumnPermissionsDTO dataSetColumnPermissionsDTO = new DataSetColumnPermissionsDTO();
        List<DataSetColumnPermissionsDTO> dataSetColumnPermissionsDTOS = new ArrayList<>();
        // 代理管理员：不查当前用户级列权限
        if (!proxy) {
            dataSetColumnPermissionsDTO.setAuthTargetId(uid);
            dataSetColumnPermissionsDTO.setAuthTargetType("user");
            dataSetColumnPermissionsDTOS.addAll(columnPermissionsApi.list(dataSetColumnPermissionsDTO));
        }

        if (CollectionUtils.isNotEmpty(roleIds)) {
            dataSetColumnPermissionsDTO.setAuthTargetId(null);
            dataSetColumnPermissionsDTO.setAuthTargetIds(roleIds);
            dataSetColumnPermissionsDTO.setAuthTargetType("role");
            List<DataSetColumnPermissionsDTO> roleDataSetColumnPermissionsDTOS = columnPermissionsApi.list(dataSetColumnPermissionsDTO);
            if (CollectionUtils.isNotEmpty(roleDataSetColumnPermissionsDTOS)) {
                for (DataSetColumnPermissionsDTO dto : roleDataSetColumnPermissionsDTOS) {
                    // 代理管理员：不做用户白名单剔除（豁免真实用户与代理身份无关）
                    if (proxy) {
                        dataSetColumnPermissionsDTOS.add(dto);
                        continue;
                    }
                    List<Long> userIdList = JsonUtil.parseList(dto.getWhiteListUser(), listTypeReference);
                    if (CollectionUtils.isEmpty(userIdList) || !userIdList.contains(uid)) {
                        dataSetColumnPermissionsDTOS.add(dto);
                    }
                }
            }
        }
        if (CollectionUtils.isEmpty(dataSetColumnPermissionsDTOS)) {
            return null;
        }
        return dataSetColumnPermissionsDTOS.stream().collect(Collectors.groupingBy(DataSetColumnPermissionsDTO::getDatasetId));
    }

    private Map<Long, List<DataSetRowPermissionsTreeDTO>> getRowPermission(Long uid) {
        List<DataSetRowPermissionsTreeDTO> datasetRowPermissions = permissionManage.getRowPermissionsTree(null, uid);
        return datasetRowPermissions.stream().collect(Collectors.groupingBy(DataSetRowPermissionsTreeDTO::getDatasetId));
    }

    private Map<Long, List<DataSetRowPermissionsTreeDTO>> getRowPermissionByRoles(List<Long> roleIds) {
        List<DataSetRowPermissionsTreeDTO> datasetRowPermissions = permissionManage.getRowPermissionsTreeByRoles(null, roleIds);
        return datasetRowPermissions.stream().collect(Collectors.groupingBy(DataSetRowPermissionsTreeDTO::getDatasetId));
    }

    public void getField(DatasetRowPermissionsTreeObj tree, Map<Long, DatasetTableFieldDTO> fieldMap) {
        if (ObjectUtils.isNotEmpty(tree)) {
            if (ObjectUtils.isNotEmpty(tree.getItems())) {
                for (DatasetRowPermissionsTreeItem item : tree.getItems()) {
                    if (ObjectUtils.isNotEmpty(item)) {
                        if (StringUtils.equalsIgnoreCase(item.getType(), "item") || ObjectUtils.isEmpty(item.getSubTree())) {
                            item.setField(fieldMap.get(item.getFieldId()));
                        } else if (StringUtils.equalsIgnoreCase(item.getType(), "tree") || (ObjectUtils.isNotEmpty(item.getSubTree()) && StringUtils.isNotEmpty(item.getSubTree().getLogic()))) {
                            getField(item.getSubTree(), fieldMap);
                        }
                    }
                }
            }
        }
    }

    public List<DataSQLBotDatasetVO> getDatasetList(String dvInfo) {
        Long dvId;
        try {
            dvId = Long.parseLong(dvInfo);
        } catch (NumberFormatException e) {
            LogUtil.warn("sqlbot: invalid dvInfo parameter: {}", dvInfo);
            return Collections.emptyList();
        }

        QCoreDatasetTable cdt = QCoreDatasetTable.coreDatasetTable;
        QCoreDatasource cd = QCoreDatasource.coreDatasource;
        QCoreDatasetGroup cdg = QCoreDatasetGroup.coreDatasetGroup;
        QCoreChartView cv = QCoreChartView.coreChartView;

        List<Tuple> tuples = queryFactory
                .select(cdt.datasetGroupId, cdt.name, cd.id, cd.name)
                .from(cdt)
                .innerJoin(cd).on(cdt.datasourceId.eq(cd.id))
                .innerJoin(cdg).on(cdt.datasetGroupId.eq(cdg.id))
                .innerJoin(cv).on(cv.tableId.eq(cdt.datasetGroupId))
                .where(cv.sceneId.eq(dvId))
                .fetch();

        List<DataSQLBotDatasetVO> result = new ArrayList<>();
        for (Tuple tuple : tuples) {
            DataSQLBotDatasetVO vo = new DataSQLBotDatasetVO();
            vo.setTableId(Objects.toString(tuple.get(cdt.datasetGroupId), null));
            vo.setTableName(tuple.get(cdt.name));
            vo.setDsId(Objects.toString(tuple.get(cd.id), null));
            vo.setDsName(tuple.get(cd.name));
            result.add(vo);
        }
        return result;
    }

    public List<DataSQLBotAssistantVO> getDatasourceList(Long dsId, Long datasetId) {
        Map<Long, List<DataSetColumnPermissionsDTO>> colPermissionMap = null;
        Map<Long, List<DataSetRowPermissionsTreeDTO>> rowPermissionMap = null;
        Boolean model = deIndexManage.xpackModel();
        List<Map<String, Object>> list;

        if (ObjectUtils.isEmpty(model)) {
            list = dataSetAssistantMapper.queryAll(dsId, datasetId);
        } else if (!model) {
            list = dataSetAssistantMapper.queryCommunity(dsId, datasetId);
        } else {
            if (enterpriseService == null) {
                LogUtil.warn("sqlbot: xpack model active but DatasetAssistantEnterpriseService unavailable");
                return Collections.emptyList();
            }
            Long uid = V3UserUtil.getUid();
            // 代理管理员：只按代理组织内置管理员角色过滤，不查当前用户自己的资源
            boolean proxy = V3UserUtil.getProxy().isProxy();
            List<Map<String, Object>> roleMapList = enterpriseService.queryUserRoles();
            if (CollectionUtils.isNotEmpty(roleMapList)) {
                List<Long> roleIds = roleMapList.stream()
                        .map(item -> Long.parseLong(item.get("id").toString()))
                        .distinct().collect(Collectors.toList());
                if (!roleIds.isEmpty()) {
                    colPermissionMap = getColPermission(roleIds, proxy);
                }
                rowPermissionMap = proxy ? getRowPermissionByRoles(roleIds) : getRowPermission(uid);
            }
            list = enterpriseService.queryEnterprise(dsId, datasetId);
        }
        if (sqlbotApiLog) {
            LogUtil.info("sqlbot ds api list: {}", list);
        }

        List<DataSQLBotAssistantVO> result = new ArrayList<>();
        Map<String, DataSQLBotAssistantVO> dsFlagMap = new HashMap<>();
        Map<String, SQLBotAssistanTable> tableFlagMap = new HashMap<>();
        Map<String, SQLBotAssistantField> fieldFlagMap = new HashMap<>();
        for (Map<String, Object> row : list) {
            String datasourceId = row.get("cd_id").toString();
            DataSQLBotAssistantVO vo = dsFlagMap.get(datasourceId);
            if (ObjectUtils.isEmpty(vo)) {
                vo = buildDs(row);
                if (ObjectUtils.isEmpty(vo))
                    continue;
                dsFlagMap.put(datasourceId, vo);
                result.add(vo);
            }
            String tableId = row.get("cdg_id").toString();
            SQLBotAssistanTable table = tableFlagMap.get(tableId);
            if (ObjectUtils.isEmpty(table)) {
                table = buildTable(row, vo.getRowData());
                if (ObjectUtils.isEmpty(table))
                    continue;
                tableFlagMap.put(tableId, table);
                vo.getTables().add(table);
            }
            Object cdt_id_obj;
            Long cdt_id;
            if (ObjectUtils.isNotEmpty(cdt_id_obj = row.get("cdt_id")) && !table.getTableIds().contains(cdt_id = Long.parseLong(cdt_id_obj.toString()))) {
                table.getTableIds().add(cdt_id);
                if (table.getTableIds().size() > 1) {
                    table.setNeedTransform(true);
                }
            }
            String fieldId = row.get("cdtf_id").toString();
            SQLBotAssistantField field = fieldFlagMap.get(fieldId);
            if (ObjectUtils.isEmpty(field)) {
                field = buildField(row);
                if (ObjectUtils.isEmpty(field))
                    continue;
                fieldFlagMap.put(fieldId, field);
                table.getFields().add(field);
                if (field.isNeedTransform()) {
                    table.setNeedTransform(true);
                }
            }
        }
        filterPermissions(result, colPermissionMap, rowPermissionMap);
        if (sqlbotApiLog) {
            LogUtil.info("sqlbot ds api result: {}", result);
        }
        return result;
    }

    private void filterPermissions(
            List<DataSQLBotAssistantVO> vos,
            Map<Long, List<DataSetColumnPermissionsDTO>> colPermissionMap,
            Map<Long, List<DataSetRowPermissionsTreeDTO>> rowPermissionMap
    ) {
        if (CollectionUtils.isEmpty(vos)) {
            return;
        }
        Iterator<DataSQLBotAssistantVO> voIterator = vos.iterator();
        while (voIterator.hasNext()) {
            DataSQLBotAssistantVO vo = voIterator.next();
            Map<String, Object> dsRowData = vo.getRowData();
            List<SQLBotAssistanTable> tables = vo.getTables();

            Iterator<SQLBotAssistanTable> tableIterator = tables.iterator();
            while (tableIterator.hasNext()) {
                SQLBotAssistanTable table = tableIterator.next();
                Long datasetGroupId = table.getDatasetGroupId();
                List<DataSetColumnPermissionsDTO> columnPermissionsDTOS = ObjectUtils.isEmpty(colPermissionMap) ? null : colPermissionMap.get(datasetGroupId);
                List<DataSetRowPermissionsTreeDTO> rowPermissionsTreeDTOS = ObjectUtils.isEmpty(rowPermissionMap) ? null : rowPermissionMap.get(datasetGroupId);

                if (table.isNeedTransform() || ObjectUtils.isNotEmpty(columnPermissionsDTOS) || ObjectUtils.isNotEmpty(rowPermissionsTreeDTOS)) {
                    try {
                        rebuildTable(table, columnPermissionsDTOS, rowPermissionsTreeDTOS, dsRowData);
                    } catch (Exception e) {
                        LogUtil.error(e);
                        tableIterator.remove();
                    }
                }
            }

            if (CollectionUtils.isEmpty(tables)) {
                voIterator.remove();
            }
        }
    }

    private void rebuildTable(SQLBotAssistanTable table, List<DataSetColumnPermissionsDTO> columnPermissionsDTOS,
                              List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, Map<String, Object> dsRowData) {
        Map<String, Object> rowData = table.getRowData();
        CoreDatasetGroup coreDatasetGroup = BeanUtils.mapToBean(rowData, CoreDatasetGroup.class);

        DatasetGroupInfoDTO datasetGroupInfoDTO = new DatasetGroupInfoDTO();
        BeanUtils.copyBean(datasetGroupInfoDTO, coreDatasetGroup);
        datasetGroupInfoDTO.setUnionSql(null);

        List<UnionDTO> unionDTOList = JsonUtil.parseList(coreDatasetGroup.getInfo(), new TypeReference<>() {
        });
        datasetGroupInfoDTO.setUnion(unionDTOList);

        List<SQLBotAssistantField> sqlbotFields = table.getFields();
        List<DatasetTableFieldDTO> dsFields = sqlbotFields.stream().map(field -> {
            Map<String, Object> fieldRowData = field.getRowData();
            DatasetTableFieldDTO fieldDTO = BeanUtils.mapToBean(fieldRowData, DatasetTableFieldDTO.class);
            if (ObjectUtils.isNotEmpty(fieldRowData.get("group_list"))) {
                fieldDTO.setGroupList(JsonUtil.parseList(fieldRowData.get("group_list").toString(), groupTokenType));
            }
            if (ObjectUtils.isNotEmpty(fieldRowData.get("params"))) {
                fieldDTO.setParams(JsonUtil.parseList(fieldRowData.get("params").toString(), typeToken));
            }
            fieldDTO.setFieldShortName(fieldDTO.getDataeaseName());
            return fieldDTO;
        }).collect(Collectors.toList());

        datasetGroupInfoDTO.setAllFields(dsFields);
        Map<Long, DatasetTableFieldDTO> fieldMap = dsFields.stream().collect(Collectors.toMap(DatasetTableFieldDTO::getId, Function.identity()));
        if (CollectionUtils.isNotEmpty(rowPermissionsTree)) {
            rowPermissionsTree.forEach(treeDTO -> {
                DatasetRowPermissionsTreeObj tree = treeDTO.getTree();
                getField(tree, fieldMap);
            });
        }

        Map<String, Object> sqlMap;
        CoreDatasource coreDatasource;
        String dsType = dsRowData.get("type").toString();
        if (dsType.contains(DatasourceConfiguration.DatasourceType.Excel.name()) || dsType.contains(DatasourceConfiguration.DatasourceType.API.name())) {
            coreDatasource = getDeEngine();
        } else {
            coreDatasource = BeanUtils.mapToBean(dsRowData, CoreDatasource.class);
        }
        try {
            sqlMap = datasetSQLManage.getUnionSQLForEdit(datasetGroupInfoDTO, null, coreDatasource, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        String sql = (String) sqlMap.get("sql");
        @SuppressWarnings("unchecked")
        List<TableFieldWithValue> tableFieldWithValues = (List<TableFieldWithValue>) sqlMap.get("tableFieldWithValues");
        if (CollectionUtils.isNotEmpty(tableFieldWithValues)) {
            sql = replacePreparedPlaceholders(sql, tableFieldWithValues);
        }

        @SuppressWarnings("unchecked")
        List<DatasetTableFieldDTO> fields = datasetGroupInfoDTO.getAllFields();
        if (ObjectUtils.isEmpty(fields)) {
            DEException.throwException(Translator.get("i18n_no_fields"));
        }

        List<DatasetTableFieldDTO> originFields = new ArrayList<>(fields);
        if (CollectionUtils.isNotEmpty(columnPermissionsDTOS)) {
            List<ColumnPermissionItem> columnPermissionItems = new ArrayList<>();
            for (DataSetColumnPermissionsDTO dataSetColumnPermissionsDTO : columnPermissionsDTOS) {
                ColumnPermissions columnPermissions = JsonUtil.parseObject(dataSetColumnPermissionsDTO.getPermissions(), ColumnPermissions.class);
                if (!columnPermissions.getEnable()) {
                    continue;
                }
                if (StringUtils.equalsAnyIgnoreCase(dataSetColumnPermissionsDTO.getAuthTargetType(), "user", "role")) {
                    columnPermissionItems.addAll(columnPermissions.getColumns().stream().filter(ColumnPermissionItem::getSelected).collect(Collectors.toList()));
                }
            }
            fields = fields.stream().filter(field -> {
                List<ColumnPermissionItem> fieldColumnPermissionItems = columnPermissionItems.stream()
                        .filter(item -> item.getId().equals(field.getId())).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(fieldColumnPermissionItems)) {
                    return true;
                }
                return fieldColumnPermissionItems.stream().map(ColumnPermissionItem::getOpt).toList()
                        .contains(ColumnPermissionConstants.Desensitization);
            }).collect(Collectors.toList());
            if (ObjectUtils.isEmpty(fields)) {
                DEException.throwException(Translator.get("i18n_no_column_permission"));
            }
            if (sqlbotFields.size() > fields.size()) {
                Set<Long> fieldIdSet = fields.stream().map(DatasetTableFieldDTO::getId).collect(Collectors.toSet());
                List<SQLBotAssistantField> filterSqlbotFields = sqlbotFields.stream()
                        .filter(item -> fieldIdSet.contains(item.getFieldId())).collect(Collectors.toList());
                table.setFields(filterSqlbotFields);
            }
        }
        buildFieldName(sqlMap, originFields);

        @SuppressWarnings("unchecked")
        Map<Long, DatasourceSchemaDTO> dsMap = (Map<Long, DatasourceSchemaDTO>) sqlMap.get("dsMap");
        List<String> dsList = new ArrayList<>();
        for (Map.Entry<Long, DatasourceSchemaDTO> next : dsMap.entrySet()) {
            dsList.add(next.getValue().getType());
        }
        boolean needOrder = Utils.isNeedOrder(dsList);
        sql = Utils.replaceSchemaAlias(sql, dsMap);
        Provider provider = ProviderFactory.getProvider(dsList.getFirst());

        SQLMeta sqlMeta = new SQLMeta();
        Table2SQLObj.table2sqlobj(sqlMeta, null, "(" + sql + ")", false);
        Field2SQLObj.field2sqlObj(sqlMeta, fields, fields, false, dsMap, Utils.getParams(fields), null, pluginManage, true);
        WhereTree2Str.transFilterTrees(sqlMeta, rowPermissionsTree, fields, false, dsMap, Utils.getParams(fields), null, pluginManage);
        Order2SQLObj.getOrders(sqlMeta, datasetGroupInfoDTO.getSortFields(), fields, false, dsMap, Utils.getParams(fields), null, pluginManage);
        String querySQL = SQLProvider.createQuerySQL(sqlMeta, false, needOrder, false);
        querySQL = provider.rebuildSQL(querySQL, sqlMeta, false, dsMap, true);
        for (int i = 0; i < sqlMeta.getXFields().size(); i++) {
            SQLObj fieldObj = sqlMeta.getXFields().get(i);
            if (fieldObj.getFieldAlias().endsWith("_" + i + '`')) {
                table.getFields().get(i).setName(fieldObj.getFieldAlias().substring(1, fieldObj.getFieldAlias().length() - 1));
            }
        }
        table.setSql(querySQL);
    }

    public void buildFieldName(Map<String, Object> sqlMap, List<DatasetTableFieldDTO> fields) {
        @SuppressWarnings("unchecked")
        List<DatasetTableFieldDTO> unionFields = (List<DatasetTableFieldDTO>) sqlMap.get("field");
        for (DatasetTableFieldDTO datasetTableFieldDTO : fields) {
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
            if (Objects.equals(datasetTableFieldDTO.getExtField(), ExtFieldConstant.EXT_GROUP)) {
                String dataeaseName = TableUtils.fieldNameShort(datasetTableFieldDTO.getId() + "_" + datasetTableFieldDTO.getOriginName());
                datasetTableFieldDTO.setDataeaseName(dataeaseName);
                datasetTableFieldDTO.setFieldShortName(dataeaseName);
                datasetTableFieldDTO.setDeExtractType(0);
                datasetTableFieldDTO.setDeType(0);
                datasetTableFieldDTO.setGroupType("d");
            }
        }
    }

    private String replacePreparedPlaceholders(String sql, List<TableFieldWithValue> bindings) {
        for (TableFieldWithValue binding : bindings) {
            int idx = sql.indexOf('?');
            if (idx < 0) {
                break;
            }
            sql = sql.substring(0, idx) + toSqlLiteral(binding) + sql.substring(idx + 1);
        }
        return sql;
    }

    private String toSqlLiteral(TableFieldWithValue binding) {
        Object value = binding.getValue();
        if (value == null) {
            return "NULL";
        }
        Integer type = binding.getType();
        if (type != null) {
            switch (type) {
                case Types.BIGINT:
                case Types.INTEGER:
                case Types.DECIMAL:
                case Types.NUMERIC:
                case Types.FLOAT:
                case Types.DOUBLE:
                case Types.BOOLEAN:
                    return value.toString();
            }
        }
        return "'" + value.toString().replace("'", "''") + "'";
    }

    private UserFormVO getUserEntity() {
        if (rowPermissionsApi == null) {
            return null;
        }
        return rowPermissionsApi.getUserById(V3UserUtil.getUid());
    }

    private String resolveSqlVariables(String sql, Map<String, Object> dsRowData, String sqlVariableDetails) {
        CoreDatasource coreDatasource;
        String dsType = dsRowData.get("type").toString();
        if (dsType.contains(DatasourceConfiguration.DatasourceType.Excel.name())
                || dsType.contains(DatasourceConfiguration.DatasourceType.API.name())) {
            coreDatasource = getDeEngine();
        } else {
            coreDatasource = BeanUtils.mapToBean(dsRowData, CoreDatasource.class);
        }
        DatasourceSchemaDTO dto = new DatasourceSchemaDTO();
        BeanUtils.copyBean(dto, coreDatasource);
        dto.setSchemaAlias(String.format(io.dataease.constant.SQLConstants.SCHEMA, dto.getId()));
        Map<Long, DatasourceSchemaDTO> dsMap = new LinkedHashMap<>();
        dsMap.put(dto.getId(), dto);
        Provider provider = ProviderFactory.getProvider(dto.getType());
        String s = provider.replaceComment(sql);
        SqlVariableHandleResult sqlResult = new SqlparserUtils().handleVariableDefaultValueWithPreparedParams(
                s, sqlVariableDetails, true, true, null, false, dsMap, pluginManage, getUserEntity());
        return replacePreparedPlaceholders(sqlResult.getSql(), sqlResult.getTableFieldWithValues());
    }

    private SQLBotAssistantField buildField(Map<String, Object> row) {
        SQLBotAssistantField field = new SQLBotAssistantField();
        if (ObjectUtils.isNotEmpty(row.get("cdtf_id"))) {
            field.setFieldId(Long.parseLong(row.get("cdtf_id").toString()));
        }
        if (ObjectUtils.isNotEmpty(row.get("cdtf_dataease_name"))) {
            field.setDataeaseName(row.get("cdtf_dataease_name").toString());
        }
        field.setName(row.get("cdtf_origin_name").toString());
        field.setType(row.get("cdtf_type").toString());
        field.setComment(row.get("cdtf_name").toString());
        if (ObjectUtils.isNotEmpty(row.get("cdtf_ext_field")) && !row.get("cdtf_ext_field").equals(0)) {
            Object extName = row.get("cdtf_name");
            String extNameText;
            if (ObjectUtils.isNotEmpty(extName) && StringUtils.isNotBlank(extNameText = extName.toString())) {
                field.setName(extNameText);
            }
            field.setNeedTransform(true);
        }
        Map<String, Object> fieldRowData = buildRowData(row, 3);
        fieldRowData.put("datasource_id", Long.parseLong(row.get("cd_id").toString()));
        fieldRowData.put("dataset_group_id", row.get("cdg_id"));
        fieldRowData.put("dataset_table_id", row.get("cdt_id"));
        field.setRowData(fieldRowData);
        return field;
    }

    private DataSQLBotAssistantVO buildDs(Map<String, Object> row) {
        String datasourceId = row.get("cd_id").toString();
        Object dsConfig = row.get("cd_configuration");
        if (ObjectUtils.isEmpty(dsConfig) || StringUtils.isBlank(dsConfig.toString())) {
            return null;
        }
        String dsHost = environment.getProperty("dataease.ds-host", String.class);
        if (StringUtils.isBlank(dsHost)) {
            dsHost = environment.getProperty("dataease.dataease-servers", String.class);
        }
        String dsType = row.get("cd_type").toString();
        String config_json;
        Configuration config;
        if (dsType.contains(DatasourceConfiguration.DatasourceType.Excel.name()) || dsType.contains(DatasourceConfiguration.DatasourceType.API.name())) {
            config_json = EncryptUtils.aesDecrypt(getDeEngine().getConfiguration()).toString();
            config = JsonUtil.parseObject(config_json, Configuration.class);
            if (StringUtils.isNotBlank(dsHost) && ObjectUtils.isNotEmpty(config)) {
                config.setHost(dsHost);
            }
            dsType = getDeEngine().getType();
        } else {
            config_json = EncryptUtils.aesDecrypt(dsConfig.toString()).toString();
            config = JsonUtil.parseObject(config_json, Configuration.class);
            config.convertJdbcUrl();
        }
        if (dsType.contains(DatasourceConfiguration.DatasourceType.mysql.name()) && ObjectUtils.isNotEmpty(config) && StringUtils.isNotBlank(config.getHost()) && StringUtils.equalsIgnoreCase("mysql-de", config.getHost()) && StringUtils.isNotBlank(dsHost)) {
            config.setHost(dsHost);
        }
        DataSQLBotAssistantVO vo = new DataSQLBotAssistantVO();
        Long dsId = Long.valueOf(datasourceId);
        vo.setId(dsId);
        vo.setDataBase(config.getDataBase());
        vo.setExtraParams(config.getExtraParams());
        vo.setHost(dsType.contains(DatasourceConfiguration.DatasourceType.es.name()) ? config.getUrl() : config.getHost());
        vo.setPort(config.getPort());
        vo.setName(row.get("cd_name").toString());
        vo.setComment(ObjectUtils.isEmpty(row.get("cd_description")) ? vo.getName() : row.get("cd_description").toString());
        vo.setType(dsType);
        vo.setSchema(config.getSchema());
        vo.setUser(config.getUsername());
        vo.setPassword(config.getPassword());
        vo.setMode(config.getConnectionType());
        if (dsType.contains(DatasourceConfiguration.DatasourceType.sqlServer.name())) {
            ConnectionObj connection = null;
            try {
                CoreDatasource coreDatasource = dataSourceManage.getCoreDatasource(dsId);
                DatasourceSchemaDTO datasourceSchemaDTO = new DatasourceSchemaDTO();
                if (coreDatasource.getType().contains(DatasourceConfiguration.DatasourceType.Excel.name()) || coreDatasource.getType().contains(DatasourceConfiguration.DatasourceType.API.name())) {
                    coreDatasource = engineManage.getDeEngine();
                }
                if (StringUtils.isNotEmpty(coreDatasource.getStatus()) && !"Error".equalsIgnoreCase(coreDatasource.getStatus())) {
                    BeanUtils.copyBean(datasourceSchemaDTO, coreDatasource);
                    datasourceSchemaDTO.setSchemaAlias(String.format(io.dataease.constant.SQLConstants.SCHEMA, datasourceSchemaDTO.getId()));
                    Provider provider = ProviderFactory.getProvider(coreDatasource.getType());
                    connection = provider.getConnection(datasourceSchemaDTO);
                    if (connection != null) {
                        datasourceSchemaDTO.setDsVersion(connection.getConnection().getMetaData().getDatabaseMajorVersion());
                        vo.setLowVersion(datasourceSchemaDTO.getDsVersion() < 11);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                if (connection != null && connection.getConnection() != null) {
                    try {
                        connection.getConnection().close();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        if (dsIdFixed) {
            vo.setId(Long.parseLong(row.get("cd_id").toString()));
        }
        row.put("cd_configuration", config_json);
        Map<String, Object> rowData = buildRowData(row, 0);
        rowData.put("id", Long.parseLong(row.get("cd_id").toString()));
        vo.setRowData(rowData);
        if (encryptEnabled) {
            aesVO(vo);
        }
        return vo;
    }

    private Map<String, Object> buildRowData(Map<String, Object> row, int level) {
        String[] levels = {"cd_", "cdg_", "cdt_", "cdtf_"};
        String alias = levels[level];
        Map<String, Object> filteredMap = new HashMap<>();
        row.forEach((key, value) -> {
            if (key.startsWith(alias)) {
                filteredMap.put(key.substring(alias.length()), value);
            }
        });
        return filteredMap;
    }

    private void aesVO(DataSQLBotAssistantVO vo) {
        if (StringUtils.isNotBlank(vo.getHost())) {
            vo.setHost(aesEncrypt(vo.getHost()));
        }
        if (ObjectUtils.isNotEmpty(vo.getUser())) {
            vo.setUser(aesEncrypt(vo.getUser()));
        }
        if (ObjectUtils.isNotEmpty(vo.getPassword())) {
            vo.setPassword(aesEncrypt(vo.getPassword()));
        }
        if (ObjectUtils.isNotEmpty(vo.getDataBase())) {
            vo.setDataBase(aesEncrypt(vo.getDataBase()));
        }
        if (ObjectUtils.isNotEmpty(vo.getSchema())) {
            vo.setSchema(aesEncrypt(vo.getSchema()));
        }
    }

    private SQLBotAssistanTable buildTable(Map<String, Object> row, Map<String, Object> dsRowData) {
        SQLBotAssistanTable table = new SQLBotAssistanTable();
        table.setName(row.get("cdg_name").toString());
        table.setComment(row.get("cdg_name").toString());
        table.setDatasetGroupId(Long.parseLong(row.get("cdg_id").toString()));

        Object infoObj;
        if (ObjectUtils.isNotEmpty(infoObj = row.get("cdt_info"))) {
            String info = infoObj.toString();
            DatasetTableInfoDTO tableInfoDTO = JsonUtil.parseObject(info, DatasetTableInfoDTO.class);
            if (StringUtils.isNotBlank(tableInfoDTO.getSql())) {
                String sql = new String(Base64.getDecoder().decode(tableInfoDTO.getSql()));
                if (StringUtils.isNotBlank(sql) && StringUtils.contains(sql, "$DE_PARAM")) {
                    table.setNeedTransform(true);
                }
                if (StringUtils.isNotBlank(sql) && !StringUtils.contains(sql, "$DE_PARAM") && StringUtils.contains(sql, "$f2cde[")) {
                    try {
                        Object variableDetails = row.get("cdt_sql_variable_details");
                        sql = resolveSqlVariables(sql, dsRowData, variableDetails == null ? null : variableDetails.toString());
                    } catch (Exception e) {
                        LogUtil.error(e);
                        // 变量替换失败时保留原始 SQL,不丢表、不影响返回
                    }
                }
                table.setSql(sql);
            }
            if (StringUtils.isBlank(tableInfoDTO.getSql()) && StringUtils.isNotBlank(tableInfoDTO.getTable())) {
                table.setName(tableInfoDTO.getTable());
            }
        }
        Map<String, Object> tableRowData = buildRowData(row, 1);
        tableRowData.put("datasource_id", Long.parseLong(row.get("cd_id").toString()));
        table.setRowData(tableRowData);
        Set<Long> tableIds = new HashSet<>();
        tableIds.add(Long.parseLong(row.get("cdt_id").toString()));
        table.setTableIds(tableIds);
        return table;
    }
}
