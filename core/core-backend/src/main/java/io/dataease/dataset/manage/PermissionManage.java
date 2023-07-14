package io.dataease.dataset.manage;


import com.fasterxml.jackson.core.type.TypeReference;
import io.dataease.api.chart.dto.ColumnPermissionItem;
import io.dataease.api.chart.dto.ColumnPermissions;
import io.dataease.dto.dataset.DatasetTableFieldDTO;
import io.dataease.api.permissions.dataset.api.ColumnPermissionsApi;
import io.dataease.api.permissions.dataset.api.RowPermissionsApi;
import io.dataease.api.permissions.dataset.dto.*;
import io.dataease.api.permissions.user.vo.UserFormVO;
import io.dataease.constant.ColumnPermissionConstants;
import io.dataease.utils.AuthUtils;
import io.dataease.utils.CommonBeanFactory;
import io.dataease.utils.JsonUtil;
import jakarta.annotation.PostConstruct;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PermissionManage {

    private RowPermissionsApi rowPermissionsApi = null;
    private ColumnPermissionsApi columnPermissionsApi = null;

    private RowPermissionsApi getRowPermissionsApi(){
        if(rowPermissionsApi == null){
            rowPermissionsApi = CommonBeanFactory.getApplicationContext().getBean(RowPermissionsApi.class);
        }
        return rowPermissionsApi;
    }

    private ColumnPermissionsApi getColumnPermissionsApi(){
        if(columnPermissionsApi == null){
            columnPermissionsApi = CommonBeanFactory.getApplicationContext().getBean(ColumnPermissionsApi.class);
        }
        return columnPermissionsApi;
    }

    public List<DatasetTableFieldDTO> filterColumnPermissions(List<DatasetTableFieldDTO> fields, Map<String, ColumnPermissionItem> desensitizationList, Long datasetTableId, Long user) {
        List<DatasetTableFieldDTO> result = new ArrayList<>();

        List<ColumnPermissionItem> userColumnPermissionItems = new ArrayList<>();
        List<ColumnPermissionItem> roleColumnPermissionItems = new ArrayList<>();

        for (DataSetColumnPermissionsDTO dataSetColumnPermissionsDTO : columnPermissions(datasetTableId, user)) {
            ColumnPermissions columnPermissions = JsonUtil.parse(dataSetColumnPermissionsDTO.getPermissions(), ColumnPermissions.class);
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
        List<DataSetColumnPermissionsDTO> dataSetColumnPermissionsDTOS = getColumnPermissionsApi().searchPermissions(dataSetColumnPermissionsDTO);
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
                for (DataSetColumnPermissionsDTO columnPermissionsDTO : getColumnPermissionsApi().searchPermissions(dataSetColumnPermissionsDTO)) {
                    columnPermissionsDTO.getWhiteListUser();
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
                String expressionTree = record.getExpressionTree();
                if (StringUtils.isNotEmpty(userEntity.getName())) {
                    expressionTree = expressionTree.replaceAll("\\$\\{sysParams\\.userId}", userEntity.getName());
                }
                if (StringUtils.isNotEmpty(userEntity.getEmail())) {
                    expressionTree = expressionTree.replaceAll("\\$\\{sysParams\\.userEmail}", userEntity.getEmail());
                }
                record.setExpressionTree(expressionTree);
                DatasetRowPermissionsTreeObj tree = JsonUtil.parse(expressionTree, DatasetRowPermissionsTreeObj.class);
                record.setTree(tree);
            }
            result.add(record);
        }
        return result;
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

    }
}
