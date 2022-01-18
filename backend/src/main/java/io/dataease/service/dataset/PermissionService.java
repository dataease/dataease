package io.dataease.service.dataset;

import com.alibaba.fastjson.JSONObject;
import io.dataease.auth.api.dto.CurrentRoleDto;
import io.dataease.auth.api.dto.CurrentUserDto;
import io.dataease.auth.entity.SysUserEntity;
import io.dataease.auth.service.AuthUserService;
import io.dataease.base.domain.DatasetTable;
import io.dataease.base.domain.DatasetTableField;
import io.dataease.commons.constants.ColumnPermissionConstants;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.dto.chart.ChartCustomFilterItemDTO;
import io.dataease.dto.chart.ChartFieldCustomFilterDTO;
import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.xpack.auth.dto.request.*;
import io.dataease.plugins.xpack.auth.service.ColumnPermissionService;
import io.dataease.plugins.xpack.auth.service.RowPermissionService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PermissionService {
    @Resource
    private AuthUserService authUserService;

    public List<ChartFieldCustomFilterDTO> getCustomFilters(List<DatasetTableField> fields, DatasetTable datasetTable, Long user) {
        List<ChartFieldCustomFilterDTO> customFilter = new ArrayList<>();
        for (DatasetRowPermissions datasetRowPermissions : rowPermissions(datasetTable.getId(), user)) {
            ChartFieldCustomFilterDTO dto = new ChartFieldCustomFilterDTO();
            if (StringUtils.isEmpty(datasetRowPermissions.getDatasetFieldId())) {
                continue;
            }
            DatasetTableField field = getFieldById(fields, datasetRowPermissions.getDatasetFieldId());
            if (field == null) {
                continue;
            }
            dto.setField(field);
            dto.setId(field.getId());
            dto.setFilterType(datasetRowPermissions.getFilterType());
            if (datasetRowPermissions.getFilterType().equalsIgnoreCase("logic")) {
                if (StringUtils.isEmpty(datasetRowPermissions.getFilter())) {
                    continue;
                }
                List<ChartCustomFilterItemDTO> lists = JSONObject.parseArray(datasetRowPermissions.getFilter(), ChartCustomFilterItemDTO.class);
                lists.forEach(chartCustomFilterDTO -> {
                    chartCustomFilterDTO.setFieldId(field.getId());
                });
                dto.setFilter(lists);
                dto.setLogic(datasetRowPermissions.getLogic());
                customFilter.add(dto);
            } else {
                if (StringUtils.isEmpty(datasetRowPermissions.getEnumCheckField())) {
                    continue;
                }
                dto.setEnumCheckField(Arrays.asList(datasetRowPermissions.getEnumCheckField().split(",").clone()));
                customFilter.add(dto);
            }
        }
        return customFilter;
    }

    public List<DatasetTableField> filterColumnPermissons(List<DatasetTableField> fields, List<String>desensitizationList, DatasetTable datasetTable, Long user){
        List<DatasetTableField> result = new ArrayList<>();
        List<ColumnPermissionItem> allColumnPermissionItems = new ArrayList<>();
        for (DataSetColumnPermissionsDTO dataSetColumnPermissionsDTO : columnPermissions(datasetTable.getId(), user)) {
            ColumnPermissions columnPermissions = JSONObject.parseObject(dataSetColumnPermissionsDTO.getPermissions(), ColumnPermissions.class);
            if(!columnPermissions.getEnable()){continue;}
            allColumnPermissionItems.addAll(columnPermissions.getColumns().stream().filter(columnPermissionItem -> columnPermissionItem.getSelected()).collect(Collectors.toList()));
        }
        fields.forEach(field ->{
            List<String> permissions = allColumnPermissionItems.stream().filter(columnPermissionItem -> columnPermissionItem.getId().equalsIgnoreCase(field.getId())).map(ColumnPermissionItem::getOpt).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(permissions)){
                result.add(field);
            }else {
                if(!permissions.contains(ColumnPermissionConstants.Prohibit)){
                    desensitizationList.add(field.getDataeaseName());
                    result.add(field);
                }
            }
        });
        return result;
    }


    private List<DatasetRowPermissions> rowPermissions(String datasetId, Long userId) {
        List<DatasetRowPermissions> datasetRowPermissions = new ArrayList<>();
        Map<String, RowPermissionService> beansOfType = SpringContextUtil.getApplicationContext().getBeansOfType((RowPermissionService.class));
        if (beansOfType.keySet().size() == 0) {
            return new ArrayList<>();
        }
        RowPermissionService rowPermissionService = SpringContextUtil.getBean(RowPermissionService.class);
        CurrentUserDto user = AuthUtils.getUser();
        List<Long> roleIds = new ArrayList<>();
        Long deptId = null;

        if (user == null && userId == null) {
            return datasetRowPermissions;
        }

        if (user != null && userId != null) {
            return datasetRowPermissions;
        }

        if (user != null) {
            if (user.getIsAdmin()) {
                return datasetRowPermissions;
            }
            userId = user.getUserId();
            deptId = user.getDeptId();
            roleIds = user.getRoles().stream().map(CurrentRoleDto::getId).collect(Collectors.toList());
        }

        if (userId != null) {
            SysUserEntity userEntity = authUserService.getUserById(userId);
            if (userEntity.getIsAdmin()) {
                return datasetRowPermissions;
            }
            deptId = userEntity.getDeptId();
            roleIds = authUserService.roles(userId).stream().map(r -> Long.valueOf(r)).collect(Collectors.toList());
        }


        DataSetRowPermissionsDTO dataSetRowPermissionsDTO = new DataSetRowPermissionsDTO();
        dataSetRowPermissionsDTO.setDatasetId(datasetId);
        dataSetRowPermissionsDTO.setAuthTargetIds(Collections.singletonList(userId));
        dataSetRowPermissionsDTO.setAuthTargetType("user");
        datasetRowPermissions.addAll(rowPermissionService.searchRowPermissions(dataSetRowPermissionsDTO));
        dataSetRowPermissionsDTO.setAuthTargetIds(roleIds);
        dataSetRowPermissionsDTO.setAuthTargetType("role");
        datasetRowPermissions.addAll(rowPermissionService.searchRowPermissions(dataSetRowPermissionsDTO));
        dataSetRowPermissionsDTO.setAuthTargetIds(Collections.singletonList(deptId));
        dataSetRowPermissionsDTO.setAuthTargetType("dept");
        datasetRowPermissions.addAll(rowPermissionService.searchRowPermissions(dataSetRowPermissionsDTO));
        return datasetRowPermissions;
    }

    private List<DataSetColumnPermissionsDTO> columnPermissions(String datasetId, Long userId) {
        List<DataSetColumnPermissionsDTO> datasetColumnPermissions = new ArrayList<>();
        Map<String, ColumnPermissionService> beansOfType = SpringContextUtil.getApplicationContext().getBeansOfType((ColumnPermissionService.class));
        if (beansOfType.keySet().size() == 0) {
            return new ArrayList<>();
        }
        ColumnPermissionService columnPermissionService = SpringContextUtil.getBean(ColumnPermissionService.class);
        CurrentUserDto user = AuthUtils.getUser();
        List<Long> roleIds = new ArrayList<>();
        Long deptId = null;

        if (user == null && userId == null) {
            return datasetColumnPermissions;
        }

        if (user != null && userId != null) {
            return datasetColumnPermissions;
        }

        if (user != null) {
            if (user.getIsAdmin()) {
                return datasetColumnPermissions;
            }
            userId = user.getUserId();
            deptId = user.getDeptId();
            roleIds = user.getRoles().stream().map(CurrentRoleDto::getId).collect(Collectors.toList());
        }

        if (userId != null) {
            SysUserEntity userEntity = authUserService.getUserById(userId);
            if (userEntity.getIsAdmin()) {
                return datasetColumnPermissions;
            }
            deptId = userEntity.getDeptId();
            roleIds = authUserService.roles(userId).stream().map(r -> Long.valueOf(r)).collect(Collectors.toList());
        }

        DataSetColumnPermissionsDTO dataSetColumnPermissionsDTO = new DataSetColumnPermissionsDTO();
        dataSetColumnPermissionsDTO.setDatasetId(datasetId);
        dataSetColumnPermissionsDTO.setAuthTargetIds(Collections.singletonList(userId));
        dataSetColumnPermissionsDTO.setAuthTargetType("user");
        datasetColumnPermissions.addAll(columnPermissionService.searchPermissions(dataSetColumnPermissionsDTO));
        dataSetColumnPermissionsDTO.setAuthTargetIds(roleIds);
        dataSetColumnPermissionsDTO.setAuthTargetType("role");
        datasetColumnPermissions.addAll(columnPermissionService.searchPermissions(dataSetColumnPermissionsDTO));
        dataSetColumnPermissionsDTO.setAuthTargetIds(Collections.singletonList(deptId));
        dataSetColumnPermissionsDTO.setAuthTargetType("dept");
        datasetColumnPermissions.addAll(columnPermissionService.searchPermissions(dataSetColumnPermissionsDTO));
        return datasetColumnPermissions;
    }

    private DatasetTableField getFieldById(List<DatasetTableField> fields, String fieldId) {
        DatasetTableField field = null;
        for (DatasetTableField datasetTableField : fields) {
            if (fieldId.equalsIgnoreCase(datasetTableField.getId())) {
                field = datasetTableField;
            }
        }
        return field;
    }
}
