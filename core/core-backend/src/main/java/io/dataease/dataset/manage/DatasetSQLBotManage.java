package io.dataease.dataset.manage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import io.dataease.api.dataset.union.DatasetGroupInfoDTO;
import io.dataease.api.dataset.union.DatasetTableInfoDTO;
import io.dataease.api.dataset.union.UnionDTO;
import io.dataease.api.dataset.vo.DataSQLBotAssistantVO;
import io.dataease.api.dataset.vo.SQLBotAssistanTable;
import io.dataease.api.dataset.vo.SQLBotAssistantField;
import io.dataease.api.permissions.dataset.api.ColumnPermissionsApi;
import io.dataease.api.permissions.dataset.dto.DataSetColumnPermissionsDTO;
import io.dataease.api.permissions.dataset.dto.DataSetRowPermissionsTreeDTO;
import io.dataease.auth.bo.TokenUserBO;
import io.dataease.commons.utils.EncryptUtils;
import io.dataease.constant.ColumnPermissionConstants;
import io.dataease.dataset.dao.auto.entity.CoreDatasetGroup;
import io.dataease.dataset.dao.ext.mapper.DataSetAssistantMapper;
import io.dataease.dataset.utils.TableUtils;
import io.dataease.datasource.dao.auto.entity.CoreDatasource;
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
import io.dataease.extensions.datasource.dto.CalParam;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.extensions.datasource.dto.DatasourceSchemaDTO;
import io.dataease.extensions.datasource.dto.FieldGroupDTO;
import io.dataease.extensions.datasource.factory.ProviderFactory;
import io.dataease.extensions.datasource.model.SQLMeta;
import io.dataease.extensions.datasource.provider.Provider;
import io.dataease.extensions.datasource.vo.Configuration;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
import io.dataease.extensions.view.dto.ColumnPermissionItem;
import io.dataease.extensions.view.dto.ColumnPermissions;
import io.dataease.extensions.view.dto.DatasetRowPermissionsTreeItem;
import io.dataease.extensions.view.dto.DatasetRowPermissionsTreeObj;
import io.dataease.home.manage.DeIndexManage;
import io.dataease.i18n.Translator;
import io.dataease.utils.*;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

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

    private CoreDatasource deEngine;

    @Resource
    private PermissionManage permissionManage;

    @Resource
    private DatasetSQLManage datasetSQLManage;

    @Autowired(required = false)
    private PluginManageApi pluginManage;

    @Value("${dataease.sqlbot.encrypt:false}")
    private boolean encryptEnabled;

    @Value("${dataease.sqlbot.aes-key:y5txe1mRmS_JpOrUzFzHEu-kIQn3lf7l}")
    private String aesKey;
    @Value("${dataease.sqlbot.aes-iv:sqlbot_em_aes_iv}")
    private String aesIv;
    @Value("${dataease.sqlbot.log:false}")
    private boolean sqlbotApiLog;

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

    private Map<Long, List<DataSetColumnPermissionsDTO>> getColPermission(Long uid, List<Long> roleIds) {
        ColumnPermissionsApi columnPermissionsApi = CommonBeanFactory.getBean(ColumnPermissionsApi.class);
        Objects.requireNonNull(columnPermissionsApi);

        DataSetColumnPermissionsDTO dataSetColumnPermissionsDTO = new DataSetColumnPermissionsDTO();
        dataSetColumnPermissionsDTO.setAuthTargetId(uid);
        dataSetColumnPermissionsDTO.setAuthTargetType("user");
        // dataSetColumnPermissionsDTO.setEnable(true);
        List<DataSetColumnPermissionsDTO> dataSetColumnPermissionsDTOS = columnPermissionsApi.list(dataSetColumnPermissionsDTO);

        if (CollectionUtils.isNotEmpty(roleIds)) {
            dataSetColumnPermissionsDTO.setAuthTargetId(null);
            dataSetColumnPermissionsDTO.setAuthTargetIds(roleIds);
            dataSetColumnPermissionsDTO.setAuthTargetType("role");
            List<DataSetColumnPermissionsDTO> roleDataSetColumnPermissionsDTOS = columnPermissionsApi.list(dataSetColumnPermissionsDTO);
            if (CollectionUtils.isNotEmpty(roleDataSetColumnPermissionsDTOS)) {
                dataSetColumnPermissionsDTOS.addAll(roleDataSetColumnPermissionsDTOS);
            }
        }
        if (CollectionUtils.isEmpty(dataSetColumnPermissionsDTOS)) {
            return null;
        }
        return dataSetColumnPermissionsDTOS.stream().collect(Collectors.groupingBy(DataSetColumnPermissionsDTO::getDatasetId));
    }

    private Map<Long, List<DataSetRowPermissionsTreeDTO>> getRowPermission(Long uid, List<Long> roleIds) {
        List<DataSetRowPermissionsTreeDTO> datasetRowPermissions = permissionManage.getRowPermissionsTree(null, uid);
        return datasetRowPermissions.stream().collect(Collectors.groupingBy(DataSetRowPermissionsTreeDTO::getDatasetId));
    }


    public List<DataSQLBotAssistantVO> getDatasourceList(Long dsId, Long datasetId) {
        TokenUserBO user = Objects.requireNonNull(AuthUtils.getUser());
        Long oid = user.getDefaultOid();
        Long uid = user.getUserId();
        List<Long> roleIds = null;
        Map<Long, List<DataSetColumnPermissionsDTO>> colPermissionMap = null;
        Map<Long, List<DataSetRowPermissionsTreeDTO>> rowPermissionMap = null;
        Boolean model = deIndexManage.xpackModel();
        List<Map<String, Object>> list = null;
        boolean isAdmin = uid == 1;
        QueryWrapper<Object> queryWrapper = new QueryWrapper<>();
        if (ObjectUtils.isNotEmpty(datasetId)) {
            queryWrapper.eq("cdg.id", datasetId);
        }
        if (ObjectUtils.isNotEmpty(dsId)) {
            queryWrapper.eq("cd.id", dsId);
        }
        if (ObjectUtils.isEmpty(model)) {
            if (!isAdmin) {
                return null;
            }
            list = dataSetAssistantMapper.queryAll(queryWrapper);
        } else if (!model) {
            if (!isAdmin) {
                return null;
            }
            list = dataSetAssistantMapper.queryCommunity(queryWrapper);
        } else {
            boolean isRootRole = isAdmin;
            if (!isAdmin) {
                List<Map<String, Object>> roleMapList = dataSetAssistantMapper.roleInfoByUid(uid, oid);
                if (CollectionUtils.isNotEmpty(roleMapList)) {
                    Long rootPid = 0L;
                    isRootRole = roleMapList.stream().anyMatch(item -> ObjectUtils.isNotEmpty(item.get("pid")) && rootPid.equals(Long.parseLong(item.get("pid").toString())));
                    roleIds = roleMapList.stream().map(item -> Long.parseLong(item.get("id").toString())).distinct().collect(Collectors.toList());
                } else {
                    roleIds = new ArrayList<>();
                }
                colPermissionMap = getColPermission(uid, roleIds);
                rowPermissionMap = getRowPermission(uid, roleIds);
            }
            list = dataSetAssistantMapper.queryEnterprise(oid, uid, isRootRole, queryWrapper);
        }
        if (sqlbotApiLog) {
            LogUtil.info("sqlbot ds api list: {}", list);
        }

        List<DataSQLBotAssistantVO> result = new ArrayList<>();
        Map<String, DataSQLBotAssistantVO> dsFlagMap = new HashMap<>();
        Map<String, SQLBotAssistanTable> tableFlagMap = new HashMap<>();
        Map<String, SQLBotAssistantField> fieldFlagMap = new HashMap<>();
        deEngine = engineManage.getDeEngine();
        for (Map<String, Object> row : list) {
            // build ds
            String datasourceId = row.get("cd_id").toString();
            DataSQLBotAssistantVO vo = dsFlagMap.get(datasourceId);
            if (ObjectUtils.isEmpty(vo)) {
                vo = buildDs(row);
                if (ObjectUtils.isEmpty(vo))
                    continue;
                dsFlagMap.put(datasourceId, vo);
                result.add(vo);
            }
            // build table
            String tableId = row.get("cdg_id").toString();
            SQLBotAssistanTable table = tableFlagMap.get(tableId);
            if (ObjectUtils.isEmpty(table)) {
                table = buildTable(row);
                if (ObjectUtils.isEmpty(table))
                    continue;
                tableFlagMap.put(tableId, table);
                vo.getTables().add(table);
            }
            // build field
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
        filterPermissions(result, list, colPermissionMap, rowPermissionMap);
        if (sqlbotApiLog) {
            LogUtil.info("sqlbot ds api result: {}", result);
        }
        return result;
    }

    public void buildFieldName(Map<String, Object> sqlMap, List<DatasetTableFieldDTO> fields) {
        // 获取内层union sql和字段
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

    TypeReference<List<FieldGroupDTO>> groupTokenType = new TypeReference<>() {
    };
    TypeReference<List<CalParam>> typeToken = new TypeReference<>() {
    };

    private void rebuildTable(SQLBotAssistanTable table, List<DataSetColumnPermissionsDTO> columnPermissionsDTOS, List<DataSetRowPermissionsTreeDTO> rowPermissionsTree, Map<String, Object> dsRowData) {
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

        Map<String, Object> sqlMap = null;
        CoreDatasource coreDatasource = null;
        String dsType = dsRowData.get("type").toString();
        Configuration config = null;
        if (dsType.contains(DatasourceConfiguration.DatasourceType.Excel.name()) || dsType.contains(DatasourceConfiguration.DatasourceType.API.name())) {
            coreDatasource = deEngine;
        } else {
            coreDatasource = BeanUtils.mapToBean(dsRowData, CoreDatasource.class);
        }
        try {
            sqlMap = datasetSQLManage.getUnionSQLForEdit(datasetGroupInfoDTO, null, coreDatasource);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        String sql = (String) sqlMap.get("sql");

        // 获取allFields
        List<DatasetTableFieldDTO> fields = datasetGroupInfoDTO.getAllFields();
        if (ObjectUtils.isEmpty(fields)) {
            DEException.throwException(Translator.get("i18n_no_fields"));
        }

        List<DatasetTableFieldDTO> originFields = new ArrayList<>(fields);
        Map<String, ColumnPermissionItem> desensitizationList = new HashMap<>();
        if (CollectionUtils.isNotEmpty(columnPermissionsDTOS)) {
            List<ColumnPermissionItem> columnPermissionItems = new ArrayList<>();
            for (DataSetColumnPermissionsDTO dataSetColumnPermissionsDTO : columnPermissionsDTOS) {
                ColumnPermissions columnPermissions = JsonUtil.parseObject(dataSetColumnPermissionsDTO.getPermissions(), ColumnPermissions.class);
                if (!columnPermissions.getEnable()) {
                    continue;
                }
                if (StringUtils.equalsAnyIgnoreCase(dataSetColumnPermissionsDTO.getAuthTargetType(), "user", "role")) {
                    columnPermissionItems.addAll(columnPermissions.getColumns().stream().filter(columnPermissionItem -> columnPermissionItem.getSelected()).collect(Collectors.toList()));
                }
            }
            fields = fields.stream().filter(field -> {
                List<ColumnPermissionItem> fieldColumnPermissionItems = columnPermissionItems.stream().filter(columnPermissionItem -> columnPermissionItem.getId().equals(field.getId())).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(fieldColumnPermissionItems)) {
                    return true;
                }
                return fieldColumnPermissionItems.stream().map(ColumnPermissionItem::getOpt).toList().contains(ColumnPermissionConstants.Desensitization);
            }).collect(Collectors.toList());
            // fields = permissionManage.filterColumnPermissions(fields, desensitizationList, datasetGroupInfoDTO.getId(), null);
            if (ObjectUtils.isEmpty(fields)) {
                DEException.throwException(Translator.get("i18n_no_column_permission"));
            }
            if (sqlbotFields.size() > fields.size()) {
                Set<Long> fieldIdSet = fields.stream().map(DatasetTableFieldDTO::getId).collect(Collectors.toSet());
                List<SQLBotAssistantField> filterSqlbotFields = sqlbotFields.stream().filter(item -> fieldIdSet.contains(item.getFieldId())).collect(Collectors.toList());
                table.setFields(filterSqlbotFields);
            }
        }
        buildFieldName(sqlMap, originFields);
        Map<Long, DatasourceSchemaDTO> dsMap = (Map<Long, DatasourceSchemaDTO>) sqlMap.get("dsMap");
        List<String> dsList = new ArrayList<>();
        for (Map.Entry<Long, DatasourceSchemaDTO> next : dsMap.entrySet()) {
            dsList.add(next.getValue().getType());
        }
        boolean needOrder = Utils.isNeedOrder(dsList);
        sql = Utils.replaceSchemaAlias(sql, dsMap);
        Provider provider = ProviderFactory.getProvider(dsList.getFirst());

        // build query sql
        SQLMeta sqlMeta = new SQLMeta();
        Table2SQLObj.table2sqlobj(sqlMeta, null, "(" + sql + ")", false);
        Field2SQLObj.field2sqlObj(sqlMeta, fields, fields, false, dsMap, Utils.getParams(fields), null, pluginManage, true);
        WhereTree2Str.transFilterTrees(sqlMeta, rowPermissionsTree, fields, false, dsMap, Utils.getParams(fields), null, pluginManage);
        Order2SQLObj.getOrders(sqlMeta, datasetGroupInfoDTO.getSortFields(), fields, false, dsMap, Utils.getParams(fields), null, pluginManage);
        String querySQL;
        querySQL = SQLProvider.createQuerySQL(sqlMeta, false, needOrder, false);
        querySQL = provider.rebuildSQL(querySQL, sqlMeta, false, dsMap, true);
        table.setSql(querySQL);
    }

    private void filterPermissions(
            List<DataSQLBotAssistantVO> vos,
            List<Map<String, Object>> list,
            Map<Long, List<DataSetColumnPermissionsDTO>> colPermissionMap,
            Map<Long, List<DataSetRowPermissionsTreeDTO>> rowPermissionMap
    ) {
        if (CollectionUtils.isEmpty(vos)) {
            return;
        }
        vos.forEach(vo -> {
            Map<String, Object> dsRowData = vo.getRowData();
            List<SQLBotAssistanTable> tables = vo.getTables();
            tables.forEach(table -> {
                Long datasetGroupId = table.getDatasetGroupId();
                List<DataSetColumnPermissionsDTO> columnPermissionsDTOS = ObjectUtils.isEmpty(colPermissionMap) ? null : colPermissionMap.get(datasetGroupId);
                List<DataSetRowPermissionsTreeDTO> rowPermissionsTreeDTOS = ObjectUtils.isEmpty(rowPermissionMap) ? null : rowPermissionMap.get(datasetGroupId);
                if (table.isNeedTransform() || ObjectUtils.isNotEmpty(columnPermissionsDTOS) || ObjectUtils.isNotEmpty(rowPermissionsTreeDTOS)) {
                    try {
                        rebuildTable(table, columnPermissionsDTOS, rowPermissionsTreeDTOS, dsRowData);
                    } catch (Exception e) {
                        LogUtil.error(e);
                    }
                }
            });
        });
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
        Object dsConfig = row.get("cd_configuration");
        if (ObjectUtils.isEmpty(dsConfig) || StringUtils.isBlank(dsConfig.toString())) {
            return null;
        }
        String dsHost = environment.getProperty("dataease.ds-host", String.class);
        if (StringUtils.isBlank(dsHost)) {
            dsHost = environment.getProperty("dataease.dataease-servers", String.class);
        }
        String dsType = row.get("cd_type").toString();
        String config_json = null;
        Configuration config = null;
        if (dsType.contains(DatasourceConfiguration.DatasourceType.Excel.name()) || dsType.contains(DatasourceConfiguration.DatasourceType.API.name())) {
            config_json = EncryptUtils.aesDecrypt(deEngine.getConfiguration()).toString();
            config = JsonUtil.parseObject(config_json, Configuration.class);
            if (StringUtils.isNotBlank(dsHost) && ObjectUtils.isNotEmpty(config)) {
                config.setHost(dsHost);
            }
            dsType = deEngine.getType();
        } else {
            config_json = EncryptUtils.aesDecrypt(dsConfig.toString()).toString();
            config = JsonUtil.parseObject(config_json, Configuration.class);
        }
        DataSQLBotAssistantVO vo = new DataSQLBotAssistantVO();
        vo.setDataBase(config.getDataBase());
        vo.setExtraParams(config.getExtraParams());
        vo.setHost(config.getHost());
        vo.setPort(config.getPort());
        vo.setName(row.get("cd_name").toString());
        vo.setComment(ObjectUtils.isEmpty(row.get("cd_description")) ? vo.getName() : row.get("cd_description").toString());
        vo.setType(dsType);
        vo.setSchema(config.getSchema());
        vo.setUser(config.getUsername());
        vo.setPassword(config.getPassword());
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

    private SQLBotAssistanTable buildTable(Map<String, Object> row) {
        SQLBotAssistanTable table = new SQLBotAssistanTable();
        table.setName(row.get("cdg_name").toString());
        table.setComment(row.get("cdg_name").toString());
        table.setDatasetGroupId(Long.parseLong(row.get("cdg_id").toString()));

        Object infoObj = null;
        if (ObjectUtils.isNotEmpty(infoObj = row.get("cdt_info"))) {
            String info = infoObj.toString();
            DatasetTableInfoDTO tableInfoDTO = JsonUtil.parseObject(info, DatasetTableInfoDTO.class);
            if (StringUtils.isNotBlank(tableInfoDTO.getSql())) {
                String sql = new String(Base64.getDecoder().decode(tableInfoDTO.getSql()));
                table.setSql(sql);
            }
            if (StringUtils.isNotBlank(tableInfoDTO.getTable())) {
                table.setName(tableInfoDTO.getTable());
            }
        }
        Map<String, Object> tableRowData = buildRowData(row, 1);
        tableRowData.put("datasource_id", Long.parseLong(row.get("cd_id").toString()));
        table.setRowData(tableRowData);
        return table;
    }

}
