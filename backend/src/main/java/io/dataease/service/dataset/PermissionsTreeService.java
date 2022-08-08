package io.dataease.service.dataset;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.dataease.auth.api.dto.CurrentRoleDto;
import io.dataease.auth.entity.SysUserEntity;
import io.dataease.auth.service.AuthUserService;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.plugins.common.base.domain.DatasetTable;
import io.dataease.plugins.common.base.domain.DatasetTableField;
import io.dataease.plugins.common.request.permission.DataSetRowPermissionsTreeDTO;
import io.dataease.plugins.common.request.permission.DatasetRowPermissionsTreeItem;
import io.dataease.plugins.common.request.permission.DatasetRowPermissionsTreeObj;
import io.dataease.plugins.common.request.permission.DatasetRowPermissionsTreeRequest;
import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.xpack.auth.service.RowPermissionTreeService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PermissionsTreeService {
    @Resource
    private AuthUserService authUserService;
    @Resource
    private DataSetTableFieldsService dataSetTableFieldsService;

    public List<DataSetRowPermissionsTreeDTO> getRowPermissionsTree(List<DatasetTableField> fields, DatasetTable datasetTable, Long user) {
        // 获取当前数据集下，当前用户、角色、组织所有的行权限（非白名单，非禁用）
        List<DataSetRowPermissionsTreeDTO> records = rowPermissionsTree(datasetTable.getId(), user);
        // 构建权限tree中的field，如果field不存在，置为null
        for (DataSetRowPermissionsTreeDTO record : records) {
            getField(record.getTree());
        }
        return records;
    }

    private List<DataSetRowPermissionsTreeDTO> rowPermissionsTree(String datasetId, Long userId) {
        List<DataSetRowPermissionsTreeDTO> datasetRowPermissions = new ArrayList<>();
        Map<String, RowPermissionTreeService> beansOfType = SpringContextUtil.getApplicationContext().getBeansOfType((RowPermissionTreeService.class));
        if (beansOfType.keySet().size() == 0) {
            return datasetRowPermissions;
        }
        RowPermissionTreeService rowPermissionTreeService = SpringContextUtil.getBean(RowPermissionTreeService.class);
        SysUserEntity userEntity = userId != null ? authUserService.getUserById(userId) : AuthUtils.getUser();
        List<Long> roleIds = new ArrayList<>();
        Long deptId = null;

        if (userEntity == null) {
            return datasetRowPermissions;
        }
        if (userEntity.getIsAdmin()) {
            return datasetRowPermissions;
        }
        userId = userEntity.getUserId();
        deptId = userEntity.getDeptId();
        List<CurrentRoleDto> currentRoleDtos = authUserService.roleInfos(userId);
        roleIds = currentRoleDtos.stream().map(CurrentRoleDto::getId).collect(Collectors.toList());

        DatasetRowPermissionsTreeRequest dataSetRowPermissionsDTO = new DatasetRowPermissionsTreeRequest();
        dataSetRowPermissionsDTO.setDatasetId(datasetId);
        dataSetRowPermissionsDTO.setEnable(true);

        if (ObjectUtils.isNotEmpty(userId)) {
            dataSetRowPermissionsDTO.setAuthTargetIds(Collections.singletonList(userId));
            dataSetRowPermissionsDTO.setAuthTargetType("user");
            datasetRowPermissions.addAll(rowPermissionTreeService.list(dataSetRowPermissionsDTO));
        }

        if (ObjectUtils.isNotEmpty(roleIds)) {
            dataSetRowPermissionsDTO.setAuthTargetIds(roleIds);
            dataSetRowPermissionsDTO.setAuthTargetType("role");
            datasetRowPermissions.addAll(rowPermissionTreeService.list(dataSetRowPermissionsDTO));
        }

        if (ObjectUtils.isNotEmpty(deptId)) {
            dataSetRowPermissionsDTO.setAuthTargetIds(Collections.singletonList(deptId));
            dataSetRowPermissionsDTO.setAuthTargetType("dept");
            datasetRowPermissions.addAll(rowPermissionTreeService.list(dataSetRowPermissionsDTO));
        }

        dataSetRowPermissionsDTO.setAuthTargetIds(null);
        dataSetRowPermissionsDTO.setAuthTargetType("sysParams");
        datasetRowPermissions.addAll(rowPermissionTreeService.list(dataSetRowPermissionsDTO));

        // 若当前用户是白名单中的，则忽略permission tree
        // 若当前规则是系统变量，则替换变量
        List<DataSetRowPermissionsTreeDTO> result = new ArrayList<>();
        Gson gson = new Gson();
        for (DataSetRowPermissionsTreeDTO record : datasetRowPermissions) {
            List<Long> userIdList = gson.fromJson(record.getWhiteListUser(), new TypeToken<List<Long>>() {
            }.getType());
            List<Long> roleIdList = gson.fromJson(record.getWhiteListRole(), new TypeToken<List<Long>>() {
            }.getType());
            List<Long> deptIdList = gson.fromJson(record.getWhiteListDept(), new TypeToken<List<Long>>() {
            }.getType());
            if (ObjectUtils.isNotEmpty(userId) && ObjectUtils.isNotEmpty(userIdList) && userIdList.contains(userId)) {
                continue;
            }
            if (ObjectUtils.isNotEmpty(roleIds) && ObjectUtils.isNotEmpty(roleIdList) && ObjectUtils.isNotEmpty(intersectionForList(roleIds, roleIdList))) {
                continue;
            }
            if (ObjectUtils.isNotEmpty(deptIdList) && ObjectUtils.isNotEmpty(deptIdList) && deptIdList.contains(deptId)) {
                continue;
            }
            // 替换系统变量
            if (StringUtils.equalsIgnoreCase(record.getAuthTargetType(), "sysParams")) {
                String expressionTree = record.getExpressionTree();
                if (StringUtils.isNotEmpty(userEntity.getUsername())) {
                    expressionTree = expressionTree.replaceAll("\\$\\{sysParams\\.userId}", userEntity.getUsername());
                }
                if (StringUtils.isNotEmpty(userEntity.getNickName())) {
                    expressionTree = expressionTree.replaceAll("\\$\\{sysParams\\.userName}", userEntity.getNickName());
                }
                if (StringUtils.isNotEmpty(userEntity.getEmail())) {
                    expressionTree = expressionTree.replaceAll("\\$\\{sysParams\\.userEmail}", userEntity.getEmail());
                }
                if (userEntity.getFrom() != null) {
                    expressionTree = expressionTree.replaceAll("\\$\\{sysParams\\.userSource}", userEntity.getFrom() == 0 ? "LOCAL" : "OIDC");
                }
                if (StringUtils.isNotEmpty(userEntity.getDeptName())) {
                    expressionTree = expressionTree.replaceAll("\\$\\{sysParams\\.dept}", userEntity.getDeptName());
                }
                if (CollectionUtils.isNotEmpty(currentRoleDtos)) {
                    expressionTree = expressionTree.replaceAll("\\$\\{sysParams\\.roles}", String.join(",", currentRoleDtos.stream().map(CurrentRoleDto::getName).collect(Collectors.toList())));
                }
                record.setExpressionTree(expressionTree);

                DatasetRowPermissionsTreeObj tree = gson.fromJson(expressionTree, DatasetRowPermissionsTreeObj.class);
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

    private void getField(DatasetRowPermissionsTreeObj tree) {
        if (ObjectUtils.isNotEmpty(tree)) {
            if (ObjectUtils.isNotEmpty(tree.getItems())) {
                for (DatasetRowPermissionsTreeItem item : tree.getItems()) {
                    if (ObjectUtils.isNotEmpty(item)) {
                        if (StringUtils.equalsIgnoreCase(item.getType(), "item") || ObjectUtils.isEmpty(item.getSubTree())) {
                            item.setField(dataSetTableFieldsService.selectByPrimaryKey(item.getFieldId()));
                        } else if (StringUtils.equalsIgnoreCase(item.getType(), "tree") || (ObjectUtils.isNotEmpty(item.getSubTree()) && StringUtils.isNotEmpty(item.getSubTree().getLogic()))) {
                            getField(item.getSubTree());
                        }
                    }
                }
            }
        }
    }
}
