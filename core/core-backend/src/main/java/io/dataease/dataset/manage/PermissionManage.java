package io.dataease.dataset.manage;


import com.fasterxml.jackson.core.type.TypeReference;
import io.dataease.api.permissions.dataset.api.ColumnPermissionsApi;
import io.dataease.api.permissions.dataset.api.RowPermissionsApi;
import io.dataease.api.permissions.dataset.dto.*;
import io.dataease.api.permissions.user.vo.UserFormVO;
import io.dataease.api.permissions.variable.dto.SysVariableValueDto;
import io.dataease.api.permissions.variable.dto.SysVariableValueItem;
import io.dataease.constant.ColumnPermissionConstants;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.extensions.view.dto.ColumnPermissionItem;
import io.dataease.extensions.view.dto.ColumnPermissions;
import io.dataease.extensions.view.dto.DatasetRowPermissionsTreeItem;
import io.dataease.extensions.view.dto.DatasetRowPermissionsTreeObj;
import io.dataease.utils.AuthUtils;
import io.dataease.utils.JsonUtil;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PermissionManage {

    @Autowired(required = false)
    private RowPermissionsApi rowPermissionsApi;

    @Autowired(required = false)
    private ColumnPermissionsApi columnPermissionsApi = null;
    @Resource
    private DatasetTableFieldManage datasetTableFieldManage;

    private RowPermissionsApi getRowPermissionsApi() {

        return rowPermissionsApi;
    }

    private ColumnPermissionsApi getColumnPermissionsApi() {

        return columnPermissionsApi;
    }

    public List<DatasetTableFieldDTO> filterColumnPermissions(List<DatasetTableFieldDTO> fields, Map<String, ColumnPermissionItem> desensitizationList, Long datasetTableId, Long user) {
        List<DatasetTableFieldDTO> result = new ArrayList<>();

        List<ColumnPermissionItem> userColumnPermissionItems = new ArrayList<>();
        List<ColumnPermissionItem> roleColumnPermissionItems = new ArrayList<>();

        for (DataSetColumnPermissionsDTO dataSetColumnPermissionsDTO : columnPermissions(datasetTableId, user)) {
            ColumnPermissions columnPermissions = JsonUtil.parseObject(dataSetColumnPermissionsDTO.getPermissions(), ColumnPermissions.class);
            if (!columnPermissions.getEnable()) {
                continue;
            }
            if (dataSetColumnPermissionsDTO.getAuthTargetType().equalsIgnoreCase("user")) {
                userColumnPermissionItems.addAll(columnPermissions.getColumns().stream().filter(columnPermissionItem -> columnPermissionItem.getSelected()).collect(Collectors.toList()));
            }
            if (dataSetColumnPermissionsDTO.getAuthTargetType().equalsIgnoreCase("role")) {
                roleColumnPermissionItems.addAll(columnPermissions.getColumns().stream().filter(columnPermissionItem -> columnPermissionItem.getSelected()).collect(Collectors.toList()));
            }
        }

        fields.forEach(field -> {
            List<ColumnPermissionItem> fieldUserColumnPermissionItems = userColumnPermissionItems.stream().filter(columnPermissionItem -> columnPermissionItem.getId().equals(field.getId())).collect(Collectors.toList());
            List<ColumnPermissionItem> fieldRoleColumnPermissionItems = roleColumnPermissionItems.stream().filter(columnPermissionItem -> columnPermissionItem.getId().equals(field.getId())).collect(Collectors.toList());

            if (CollectionUtils.isNotEmpty(fieldUserColumnPermissionItems)) {
                if (fieldUserColumnPermissionItems.stream().map(ColumnPermissionItem::getOpt).collect(Collectors.toList()).contains(ColumnPermissionConstants.Desensitization)) {
                    desensitizationList.put(field.getDataeaseName(), fieldUserColumnPermissionItems.get(0));
                    result.add(field);
                }
                return;
            }
            if (CollectionUtils.isNotEmpty(fieldRoleColumnPermissionItems)) {
                if (fieldRoleColumnPermissionItems.stream().map(ColumnPermissionItem::getOpt).collect(Collectors.toList()).contains(ColumnPermissionConstants.Desensitization)) {
                    desensitizationList.put(field.getDataeaseName(), fieldRoleColumnPermissionItems.get(0));
                    result.add(field);
                }
                return;
            }
            result.add(field);
        });
        return result;
    }

    private List<DataSetColumnPermissionsDTO> columnPermissions(Long datasetId, Long userId) {
        List<DataSetColumnPermissionsDTO> datasetColumnPermissions = new ArrayList<>();
        userId = userId != null ? userId : AuthUtils.getUser().getUserId();

        if (getRowPermissionsApi() == null || getColumnPermissionsApi() == null) {
            return new ArrayList<>();
        }
        if (AuthUtils.isSysAdmin(userId)) {
            return new ArrayList<>();
        }

        DataSetColumnPermissionsDTO dataSetColumnPermissionsDTO = new DataSetColumnPermissionsDTO();
        dataSetColumnPermissionsDTO.setDatasetId(datasetId);
        dataSetColumnPermissionsDTO.setAuthTargetIds(Collections.singletonList(userId));
        dataSetColumnPermissionsDTO.setAuthTargetType("user");
        List<DataSetColumnPermissionsDTO> dataSetColumnPermissionsDTOS = getColumnPermissionsApi().list(dataSetColumnPermissionsDTO);
        if (dataSetColumnPermissionsDTOS != null && CollectionUtils.isNotEmpty(dataSetColumnPermissionsDTOS)) {
            datasetColumnPermissions.addAll(dataSetColumnPermissionsDTOS);
        }

        List<Long> roleIds = getRowPermissionsApi().getUserById(userId).getRoleIds().stream().map(x -> Long.valueOf(x)).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(roleIds)) {
            List<Item> items = (List<Item>) getRowPermissionsApi().authObjs(datasetId, "role");
            roleIds = roleIds.stream().filter(id -> {
                return items.stream().map(Item::getId).collect(Collectors.toList()).contains(id);
            }).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(roleIds)) {
                dataSetColumnPermissionsDTO.setAuthTargetIds(roleIds);
                dataSetColumnPermissionsDTO.setAuthTargetType("role");
                List<DataSetColumnPermissionsDTO> roleColumnPermissionsDTOS = new ArrayList<>();
                for (DataSetColumnPermissionsDTO columnPermissionsDTO : getColumnPermissionsApi().list(dataSetColumnPermissionsDTO)) {
                    TypeReference<List<Long>> listTypeReference = new TypeReference<List<Long>>() {
                    };
                    List<Long> userIdList = JsonUtil.parseList(columnPermissionsDTO.getWhiteListUser(), listTypeReference);
                    if (CollectionUtils.isEmpty(userIdList) || !userIdList.contains(userId)) {
                        roleColumnPermissionsDTOS.add(columnPermissionsDTO);
                    }
                }
                datasetColumnPermissions.addAll(roleColumnPermissionsDTOS);
            }
        }
        return datasetColumnPermissions;
    }

    public List<DataSetRowPermissionsTreeDTO> getRowPermissionsTree(Long datasetId, Long user) {
        // 获取当前数据集下，当前用户、角色、组织所有的行权限（非白名单，非禁用）
        List<DataSetRowPermissionsTreeDTO> records = rowPermissionsTree(datasetId, user);
        // 构建权限tree中的field，如果field不存在，置为null
        for (DataSetRowPermissionsTreeDTO record : records) {
            getField(record.getTree());
        }
        return records;
    }

    private List<DataSetRowPermissionsTreeDTO> rowPermissionsTree(Long datasetId, Long userId) {
        List<DataSetRowPermissionsTreeDTO> datasetRowPermissions = new ArrayList<>();
        userId = userId != null ? userId : AuthUtils.getUser().getUserId();

        if (AuthUtils.isSysAdmin(userId)) {
            return datasetRowPermissions;
        }
        UserFormVO userEntity = getRowPermissionsApi().getUserById(userId);
        List<Long> roleIds = userEntity.getRoleIds().stream().map(x -> Long.valueOf(x)).collect(Collectors.toList());
        DatasetRowPermissionsTreeRequest dataSetRowPermissionsDTO = new DatasetRowPermissionsTreeRequest();
        dataSetRowPermissionsDTO.setDatasetId(datasetId);
        dataSetRowPermissionsDTO.setEnable(true);

        if (ObjectUtils.isNotEmpty(userId)) {
            dataSetRowPermissionsDTO.setAuthTargetIds(Collections.singletonList(userId));
            dataSetRowPermissionsDTO.setAuthTargetType("user");
            datasetRowPermissions.addAll(getRowPermissionsApi().list(dataSetRowPermissionsDTO));
        }

        if (ObjectUtils.isNotEmpty(roleIds)) {
            dataSetRowPermissionsDTO.setAuthTargetIds(roleIds);
            dataSetRowPermissionsDTO.setAuthTargetType("role");
            datasetRowPermissions.addAll(getRowPermissionsApi().list(dataSetRowPermissionsDTO));
        }

        dataSetRowPermissionsDTO.setAuthTargetIds(null);
        dataSetRowPermissionsDTO.setAuthTargetType("sysParams");
        datasetRowPermissions.addAll(getRowPermissionsApi().list(dataSetRowPermissionsDTO));

        // 若当前用户是白名单中的，则忽略permission tree
        // 若当前规则是系统变量，则替换变量
        List<DataSetRowPermissionsTreeDTO> result = new ArrayList<>();
        TypeReference<List<Long>> listTypeReference = new TypeReference<List<Long>>() {
        };
        for (DataSetRowPermissionsTreeDTO record : datasetRowPermissions) {
            List<Long> userIdList = JsonUtil.parseList(record.getWhiteListUser(), listTypeReference);
            List<Long> roleIdList = JsonUtil.parseList(record.getWhiteListRole(), listTypeReference);
            List<Long> deptIdList = JsonUtil.parseList(record.getWhiteListDept(), listTypeReference);
            if (ObjectUtils.isNotEmpty(userId) && ObjectUtils.isNotEmpty(userIdList) && userIdList.contains(userId)) {
                continue;
            }
            if (ObjectUtils.isNotEmpty(roleIds) && ObjectUtils.isNotEmpty(roleIdList) && ObjectUtils.isNotEmpty(intersectionForList(roleIds, roleIdList))) {
                continue;
            }
            // 替换系统变量
            if (StringUtils.equalsIgnoreCase(record.getAuthTargetType(), "sysParams")) {
                DatasetRowPermissionsTreeObj tree = JsonUtil.parseObject(record.getExpressionTree(), DatasetRowPermissionsTreeObj.class);
                List<DatasetRowPermissionsTreeItem> items = new ArrayList<>();
                for (DatasetRowPermissionsTreeItem datasetRowPermissionsTreeItem : tree.getItems()) {
                    if (StringUtils.isNotEmpty(userEntity.getAccount()) && datasetRowPermissionsTreeItem.getValue().equalsIgnoreCase("${sysParams.userId}")) {
                        datasetRowPermissionsTreeItem.setValue(userEntity.getAccount());
                        items.add(datasetRowPermissionsTreeItem);
                        continue;
                    }
                    if (StringUtils.isNotEmpty(userEntity.getEmail()) && datasetRowPermissionsTreeItem.getValue().equalsIgnoreCase("${sysParams.userEmail}")) {
                        datasetRowPermissionsTreeItem.setValue(userEntity.getEmail());
                        items.add(datasetRowPermissionsTreeItem);
                        continue;
                    }
                    if (StringUtils.isNotEmpty(userEntity.getName()) && datasetRowPermissionsTreeItem.getValue().equalsIgnoreCase("${sysParams.userName}")) {
                        datasetRowPermissionsTreeItem.setValue(userEntity.getName());
                        items.add(datasetRowPermissionsTreeItem);
                        continue;
                    }

                    String value = handleSysVariable(userEntity, datasetRowPermissionsTreeItem);
                    if (value == null) {
                        continue;
                    } else {
                        datasetRowPermissionsTreeItem.setValue(value);
                    }
                    items.add(datasetRowPermissionsTreeItem);
                }
                tree.setItems(items);
                record.setTree(tree);
            }
            result.add(record);
        }
        return result;
    }

    private String handleSysVariable(UserFormVO userEntity, DatasetRowPermissionsTreeItem datasetRowPermissionsTreeItem) {
        String value = null;
        String sysVariable = datasetRowPermissionsTreeItem.getValue();
        if (StringUtils.isEmpty(sysVariable) && !(sysVariable.startsWith("${") && sysVariable.endsWith("}"))) {
            return value;
        }
        String variableId = sysVariable.substring(2, sysVariable.length() - 1);
        for (SysVariableValueItem variable : userEntity.getVariables()) {
            if (!variable.isValid()) {
                continue;
            }
            if (!variableId.equalsIgnoreCase(variable.getVariableId().toString())) {
                continue;
            }
            if (variable.getSysVariableDto().getType().equalsIgnoreCase("text")) {
                if (Arrays.asList("in", "not in").contains(datasetRowPermissionsTreeItem.getTerm())) {
                    value = String.join(",", variable.getValueList().stream().filter(sysVariableValueDto -> variable.getVariableValueIds().contains(sysVariableValueDto.getId().toString())).map(SysVariableValueDto::getValue).collect(Collectors.toList()));
                } else {
                    for (SysVariableValueDto sysVariableValueDto : variable.getValueList()) {
                        if (sysVariableValueDto.getId().equals(variable.getVariableValueId())) {
                            value = sysVariableValueDto.getValue();
                        }
                    }
                }
            } else {
                value = variable.getVariableValue();
            }
        }

        return value;
    }

    private List<Long> intersectionForList(List<Long> list1, List<Long> list2) {
        List<Long> result = new ArrayList<>();
        for (Long id : list1) {
            if (list2.contains(id)) {
                result.add(id);
            }
        }
        return result;
    }

    public void getField(DatasetRowPermissionsTreeObj tree) {
        if (ObjectUtils.isNotEmpty(tree)) {
            if (ObjectUtils.isNotEmpty(tree.getItems())) {
                for (DatasetRowPermissionsTreeItem item : tree.getItems()) {
                    if (ObjectUtils.isNotEmpty(item)) {
                        if (StringUtils.equalsIgnoreCase(item.getType(), "item") || ObjectUtils.isEmpty(item.getSubTree())) {
                            item.setField(datasetTableFieldManage.selectById(item.getFieldId()));
                        } else if (StringUtils.equalsIgnoreCase(item.getType(), "tree") || (ObjectUtils.isNotEmpty(item.getSubTree()) && StringUtils.isNotEmpty(item.getSubTree().getLogic()))) {
                            getField(item.getSubTree());
                        }
                    }
                }
            }
        }
    }
}
