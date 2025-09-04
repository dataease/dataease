package io.dataease.dataset.manage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.dataset.union.DatasetTableInfoDTO;
import io.dataease.api.dataset.vo.DataSQLBotAssistantVO;
import io.dataease.api.dataset.vo.SQLBotAssistanTable;
import io.dataease.api.dataset.vo.SQLBotAssistantField;
import io.dataease.api.permissions.dataset.api.ColumnPermissionsApi;
import io.dataease.api.permissions.dataset.api.RowPermissionsApi;
import io.dataease.api.permissions.dataset.dto.DataSetColumnPermissionsDTO;
import io.dataease.api.permissions.dataset.dto.DataSetRowPermissionsTreeDTO;
import io.dataease.api.permissions.dataset.dto.DatasetRowPermissionsTreeRequest;
import io.dataease.api.permissions.role.api.RoleApi;
import io.dataease.api.permissions.role.dto.RoleRequest;
import io.dataease.api.permissions.role.vo.RoleVO;
import io.dataease.auth.bo.TokenUserBO;
import io.dataease.commons.utils.EncryptUtils;
import io.dataease.constant.ColumnPermissionConstants;
import io.dataease.dataset.dao.ext.mapper.DataSetAssistantMapper;
import io.dataease.datasource.dao.auto.entity.CoreDatasource;
import io.dataease.datasource.manage.EngineManage;
import io.dataease.extensions.datasource.vo.Configuration;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
import io.dataease.extensions.view.dto.ColumnPermissionItem;
import io.dataease.extensions.view.dto.ColumnPermissions;
import io.dataease.home.manage.DeIndexManage;
import io.dataease.utils.*;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.*;
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

    @Value("${dataease.sqlbot.encrypt:false}")
    private boolean encryptEnabled;

    @Value("${dataease.sqlbot.aes-key:y5txe1mRmS_JpOrUzFzHEu-kIQn3lf7l}")
    private String aesKey;
    @Value("${dataease.sqlbot.aes-iv:sqlbot_em_aes_iv}")
    private String aesIv;

    private String aesEncrypt(String text) {
        String iv = aesIv;
        int len = iv.length();
        if (len > 16) {
            iv = iv.substring(0, 16);
        }
        if (len < 16) {
            iv = String.format("%-" + (16 - len) + "s", iv).replace(' ', '0');
        }
        return AesUtils.aesEncrypt(text, aesKey, iv);
    }

    private Map<Long, List<DataSetColumnPermissionsDTO>> getColPermission(Long uid, List<Long> roleIds) {
        ColumnPermissionsApi columnPermissionsApi = CommonBeanFactory.getBean(ColumnPermissionsApi.class);
        Objects.requireNonNull(columnPermissionsApi);

        DataSetColumnPermissionsDTO dataSetColumnPermissionsDTO = new DataSetColumnPermissionsDTO();
        dataSetColumnPermissionsDTO.setAuthTargetId(uid);
        dataSetColumnPermissionsDTO.setAuthTargetType("user");
        dataSetColumnPermissionsDTO.setEnable(true);
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
        RowPermissionsApi rowPermissionsApi = CommonBeanFactory.getBean(RowPermissionsApi.class);
        Objects.requireNonNull(rowPermissionsApi);

        DatasetRowPermissionsTreeRequest request = new DatasetRowPermissionsTreeRequest();
        request.setEnable(true);

        request.setAuthTargetId(uid);
        request.setAuthTargetType("user");
        List<DataSetRowPermissionsTreeDTO> permissionsTreeDTOS = rowPermissionsApi.list(request);

        if (ObjectUtils.isNotEmpty(roleIds)) {
            request.setAuthTargetId(null);
            request.setAuthTargetIds(roleIds);
            request.setAuthTargetType("role");
            List<DataSetRowPermissionsTreeDTO> rolePermissionDTOS = rowPermissionsApi.list(request);
            if (CollectionUtils.isNotEmpty(rolePermissionDTOS)) {
                permissionsTreeDTOS.addAll(rolePermissionDTOS);
            }
        }
        if (CollectionUtils.isEmpty(permissionsTreeDTOS)) {
            return null;
        }
        return permissionsTreeDTOS.stream().collect(Collectors.groupingBy(DataSetRowPermissionsTreeDTO::getDatasetId));
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
        boolean withColsOrRowsPermission = false;
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
                RoleRequest request = new RoleRequest();
                request.setUid(uid);
                List<RoleVO> roleVOS = Objects.requireNonNull(CommonBeanFactory.getBean(RoleApi.class)).selectedForUser(request);
                isRootRole = roleVOS.stream().anyMatch(RoleVO::isRoot);
                roleIds = roleVOS.stream().map(RoleVO::getId).toList();

                /*colPermissionMap = getColPermission(uid, roleIds);
                rowPermissionMap = getRowPermission(uid, roleIds);
                withColsOrRowsPermission = MapUtils.isNotEmpty(colPermissionMap) || MapUtils.isNotEmpty(rowPermissionMap);*/
            }
            list = dataSetAssistantMapper.queryEnterprise(oid, uid, isRootRole, queryWrapper);
        }


        List<DataSQLBotAssistantVO> result = new ArrayList<>();
        Map<String, DataSQLBotAssistantVO> dsFlagMap = new HashMap<>();
        Map<String, SQLBotAssistanTable> tableFlagMap = new HashMap<>();
        Map<String, SQLBotAssistantField> fieldFlagMap = new HashMap<>();
        deEngine = engineManage.getDeEngine();
        for (Map<String, Object> row : list) {
            // build ds
            String datasourceId = row.get("datasource_id").toString();
            DataSQLBotAssistantVO vo = dsFlagMap.get(datasourceId);
            if (ObjectUtils.isEmpty(vo)) {
                vo = buildDs(row);
                if (ObjectUtils.isEmpty(vo))
                    continue;
                dsFlagMap.put(datasourceId, vo);
                result.add(vo);
            }
            // build table
            String tableId = row.get("id").toString();
            SQLBotAssistanTable table = tableFlagMap.get(tableId);
            if (ObjectUtils.isEmpty(table)) {
                table = buildTable(row);
                if (ObjectUtils.isEmpty(table))
                    continue;
                tableFlagMap.put(tableId, table);
                vo.getTables().add(table);
            }
            // build field
            String fieldId = row.get("field_id").toString();
            SQLBotAssistantField field = fieldFlagMap.get(fieldId);
            if (ObjectUtils.isEmpty(field)) {
                field = buildField(row);
                if (ObjectUtils.isEmpty(field))
                    continue;
                fieldFlagMap.put(fieldId, field);
                table.getFields().add(field);
            }
        }
        /*if (withColsOrRowsPermission) {
            result = filterPermissions(result, list, colPermissionMap, rowPermissionMap);
        }*/
        LogUtil.info("sqlbot ds api result: {}", result);
        return result;
    }

    private List<DataSQLBotAssistantVO> filterPermissions(
            List<DataSQLBotAssistantVO> vos,
            List<Map<String, Object>> list,
            Map<Long, List<DataSetColumnPermissionsDTO>> colPermissionMap,
            Map<Long, List<DataSetRowPermissionsTreeDTO>> rowPermissionMap
    ) {
        if (CollectionUtils.isEmpty(vos)) {
            return vos;
        }
        vos.forEach(vo -> {
            List<SQLBotAssistanTable> tables = vo.getTables();
            tables.forEach(table -> {
                Long datasetGroupId = table.getDatasetGroupId();
                List<DataSetColumnPermissionsDTO> dataSetColumnPermissionsDTOS = colPermissionMap.get(datasetGroupId);

                List<ColumnPermissionItem> columnPermissionItems = new ArrayList<>();

                for (DataSetColumnPermissionsDTO dataSetColumnPermissionsDTO : dataSetColumnPermissionsDTOS) {
                    ColumnPermissions columnPermissions = JsonUtil.parseObject(dataSetColumnPermissionsDTO.getPermissions(), ColumnPermissions.class);
                    if (!columnPermissions.getEnable()) {
                        continue;
                    }
                    if (StringUtils.equalsAnyIgnoreCase(dataSetColumnPermissionsDTO.getAuthTargetType(), "user", "role")) {
                        columnPermissionItems.addAll(columnPermissions.getColumns().stream().filter(columnPermissionItem -> columnPermissionItem.getSelected()).collect(Collectors.toList()));
                    }
                }
                List<SQLBotAssistantField> filterFields = table.getFields().stream().filter(field -> {
                    List<ColumnPermissionItem> fieldColumnPermissionItems = columnPermissionItems.stream().filter(columnPermissionItem -> columnPermissionItem.getId().equals(field.getFieldId())).collect(Collectors.toList());
                    if (CollectionUtils.isEmpty(fieldColumnPermissionItems)) {
                        return true;
                    }
                    return fieldColumnPermissionItems.stream().map(ColumnPermissionItem::getOpt).toList().contains(ColumnPermissionConstants.Desensitization);
                }).collect(Collectors.toList());
                table.setFields(filterFields);
            });
        });
        return vos;
    }

    private SQLBotAssistantField buildField(Map<String, Object> row) {
        SQLBotAssistantField field = new SQLBotAssistantField();
        if (ObjectUtils.isNotEmpty(row.get("field_id"))) {
            field.setFieldId(Long.parseLong(row.get("field_id").toString()));
        }
        if (ObjectUtils.isNotEmpty(row.get("dataease_name"))) {
            field.setDataeaseName(row.get("dataease_name").toString());
        }
        field.setName(row.get("origin_name").toString());
        field.setType(row.get("field_type").toString());
        field.setComment(row.get("field_show_name").toString());
        return field;
    }


    private DataSQLBotAssistantVO buildDs(Map<String, Object> row) {
        Object dsConfig = row.get("ds_config");
        if (ObjectUtils.isEmpty(dsConfig) || StringUtils.isBlank(dsConfig.toString())) {
            return null;
        }
        String dsHost = environment.getProperty("dataease.dsHost", String.class);
        if (StringUtils.isBlank(dsHost)) {
            dsHost = environment.getProperty("dataease.dataease-servers", String.class);
        }
        String dsType = row.get("ds_type").toString();
        Configuration config = null;
        if (dsType.contains(DatasourceConfiguration.DatasourceType.Excel.name()) || dsType.contains(DatasourceConfiguration.DatasourceType.API.name())) {
            String config_json = EncryptUtils.aesDecrypt(deEngine.getConfiguration()).toString();
            config = JsonUtil.parseObject(config_json, Configuration.class);
            if (StringUtils.isNotBlank(dsHost) && ObjectUtils.isNotEmpty(config)) {
                config.setHost(dsHost);
            }
            dsType = deEngine.getType();
        } else {
            String config_json = EncryptUtils.aesDecrypt(dsConfig.toString()).toString();
            config = JsonUtil.parseObject(config_json, Configuration.class);
        }
        DataSQLBotAssistantVO vo = new DataSQLBotAssistantVO();
        vo.setDataBase(config.getDataBase());
        vo.setExtraParams(config.getExtraParams());
        vo.setHost(config.getHost());
        vo.setPort(config.getPort());
        vo.setName(row.get("ds_name").toString());
        vo.setComment(ObjectUtils.isEmpty(row.get("ds_desc")) ? vo.getName() : row.get("ds_desc").toString());
        vo.setType(dsType);
        vo.setSchema(config.getSchema());
        vo.setUser(config.getUsername());
        vo.setPassword(config.getPassword());
        if (encryptEnabled) {
            aesVO(vo);
        }
        return vo;
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
        table.setName(row.get("table_name").toString());
        table.setComment(row.get("dataset_name").toString());
        if (ObjectUtils.isNotEmpty(row.get("dataset_group_id"))) {
            table.setDatasetGroupId(Long.parseLong(row.get("dataset_group_id").toString()));
        }

        Object infoObj = null;
        if (ObjectUtils.isNotEmpty(infoObj = row.get("info"))) {
            String info = infoObj.toString();
            DatasetTableInfoDTO tableInfoDTO = JsonUtil.parseObject(info, DatasetTableInfoDTO.class);
            if (StringUtils.isNotBlank(tableInfoDTO.getSql())) {
                String sql = new String(Base64.getDecoder().decode(tableInfoDTO.getSql()));
                table.setSql(sql);
            }
        }
        return table;
    }

}
