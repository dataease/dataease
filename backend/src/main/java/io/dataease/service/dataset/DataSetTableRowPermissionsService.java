package io.dataease.service.dataset;

import io.dataease.base.domain.DatasetRowPermissions;
import io.dataease.base.domain.DatasetRowPermissionsExample;
import io.dataease.base.mapper.DatasetRowPermissionsMapper;
import io.dataease.base.mapper.SysDeptMapper;
import io.dataease.base.mapper.SysRoleMapper;
import io.dataease.base.mapper.SysUserMapper;
import io.dataease.base.mapper.ext.ExtDataSetTableMapper;
import io.dataease.base.mapper.ext.query.GridExample;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.controller.sys.base.BaseGridRequest;
import io.dataease.controller.sys.base.ConditionEntity;
import io.dataease.dto.dataset.DataSetRowPermissionsDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)



public class DataSetTableRowPermissionsService {
    @Resource
    private ExtDataSetTableMapper extDataSetTableMapper;
    @Resource
    private DatasetRowPermissionsMapper datasetRowPermissionsMapper;
    @Resource
    private SysUserMapper sysUserMapper;
    @Resource
    private SysRoleMapper sysRoleMapper;
    @Resource
    private SysDeptMapper sysDeptMapper;

    public List<DataSetRowPermissionsDTO> searchRowPermissions(BaseGridRequest request, String datasetId){
        List<ConditionEntity> conditionEntities = request.getConditions() == null ? new ArrayList<>() : request.getConditions();
        ConditionEntity entity = new ConditionEntity();
        entity.setField("dataset_row_permissions.dataset_id");
        entity.setOperator("eq");
        entity.setValue(datasetId);
        conditionEntities.add(entity);
        request.setConditions(conditionEntities);
        GridExample gridExample = request.convertExample();
        gridExample.setExtendCondition(AuthUtils.getUser().getUserId().toString());
        return extDataSetTableMapper.searchRowPermissons(gridExample);
    }

    public List<? extends Object> authObjs(DataSetRowPermissionsDTO request){
        switch (request.getAuthTargetType()) {
            case "user":
                return extDataSetTableMapper.searchAuthUsers(request);
            case "role":
                return extDataSetTableMapper.searchAuthRoles(request);
            case "dept":
                return extDataSetTableMapper.searchAuthDepts(request);
            default:
                return new ArrayList<>();
        }
    }

    public void save(DatasetRowPermissions datasetRowPermissions){
        if(StringUtils.isEmpty(datasetRowPermissions.getId())){
            datasetRowPermissions.setId(UUID.randomUUID().toString());
            datasetRowPermissions.setUpdateTime(System.currentTimeMillis());
            datasetRowPermissionsMapper.insert(datasetRowPermissions);
        }else {
            datasetRowPermissions.setUpdateTime(System.currentTimeMillis());
            datasetRowPermissionsMapper.updateByPrimaryKey(datasetRowPermissions);
        }

    }

    public DataSetRowPermissionsDTO dataSetRowPermissionInfo(DataSetRowPermissionsDTO datasetRowPermissions){
        switch (datasetRowPermissions.getAuthTargetType()) {
            case "user":
                datasetRowPermissions.setAuthTargetName(Optional.ofNullable(sysUserMapper.selectByPrimaryKey(datasetRowPermissions.getAuthTargetId())).get().getUsername());
                break;
            case "role":
                datasetRowPermissions.setAuthTargetName(Optional.ofNullable(sysRoleMapper.selectByPrimaryKey(datasetRowPermissions.getAuthTargetId())).get().getName());
                break;
            case "dept":
                datasetRowPermissions.setAuthTargetName(Optional.ofNullable(sysDeptMapper.selectByPrimaryKey(datasetRowPermissions.getAuthTargetId())).get().getName());
                break;
            default:
                break;
        }
        return datasetRowPermissions;
    }

    public void delete(String id){
        datasetRowPermissionsMapper.deleteByPrimaryKey(id);
    }

    public List<DatasetRowPermissions> listDatasetRowPermissions(String datasetId, List<Long>authTargetIds, String authTargetType){
        if(CollectionUtils.isEmpty(authTargetIds)){
            return new ArrayList<>();
        }
        DatasetRowPermissionsExample example = new DatasetRowPermissionsExample();
        example.createCriteria().andDatasetIdEqualTo(datasetId).andAuthTargetTypeEqualTo(authTargetType).andAuthTargetIdIn(authTargetIds);
        return datasetRowPermissionsMapper.selectByExample(example);
    }
}
