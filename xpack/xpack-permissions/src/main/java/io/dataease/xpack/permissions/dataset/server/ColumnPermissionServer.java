package io.dataease.xpack.permissions.dataset.server;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import io.dataease.api.permissions.dataset.api.ColumnPermissionsApi;
import io.dataease.api.permissions.dataset.dto.DataSetColumnPermissionsDTO;
import io.dataease.api.permissions.dataset.dto.DataSetRowPermissionsTreeDTO;
import io.dataease.api.permissions.dataset.dto.DatasetRowPermissionsTreeRequest;
import io.dataease.api.permissions.user.vo.UserFormVO;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.IDUtils;
import io.dataease.utils.JsonUtil;
import io.dataease.xpack.permissions.dataset.dto.auto.entity.PerDatasetColumnPermissions;
import io.dataease.xpack.permissions.dataset.dto.auto.entity.PerDatasetRowPermissionsTree;
import io.dataease.xpack.permissions.dataset.dto.auto.mapper.PerDatasetColumnPermissionsMapper;
import io.dataease.xpack.permissions.dataset.dto.ext.mapper.ColumnPermissionsExtMapper;
import io.dataease.xpack.permissions.user.dao.auto.entity.PerRole;
import io.dataease.xpack.permissions.user.dao.auto.entity.PerUser;
import io.dataease.xpack.permissions.user.dao.auto.mapper.PerRoleMapper;
import io.dataease.xpack.permissions.user.dao.auto.mapper.PerUserMapper;
import io.dataease.xpack.permissions.user.dao.ext.mapper.UserExtMapper;
import io.dataease.xpack.permissions.user.manage.UserPageManage;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dataset/columnPermissions")
@Primary
public class ColumnPermissionServer implements ColumnPermissionsApi {
    @Resource
    private PerDatasetColumnPermissionsMapper columnPermissionsMapper;
    @Resource
    private ColumnPermissionsExtMapper columnPermissionsExtMapper;
    @Resource
    private UserPageManage userPageManage;
    @Resource
    private PerUserMapper perUserMapper;
    @Resource
    private PerRoleMapper perRoleMapper;
    @Resource
    private UserExtMapper userExtMapper;

    @Override
    public IPage<DataSetColumnPermissionsDTO> columnPermissions(@PathVariable Long datasetId, @PathVariable int goPage, @PathVariable int pageSize) {
        QueryWrapper<DataSetColumnPermissionsDTO> wrapper = new QueryWrapper<>();
        wrapper.eq("dataset_id", datasetId);
        Page<DataSetColumnPermissionsDTO> page = new Page<>(goPage, pageSize);
        IPage<DataSetColumnPermissionsDTO> pager = columnPermissionsExtMapper.pager(page, wrapper);

        for (DataSetColumnPermissionsDTO dto : pager.getRecords()) {
            String authTargetName = null;
            if (StringUtils.equalsIgnoreCase(dto.getAuthTargetType(), "user")) {
                PerUser sysUser = perUserMapper.selectById(dto.getAuthTargetId());
                authTargetName = ObjectUtils.isEmpty(sysUser) ? null : sysUser.getName();
            }
            if (StringUtils.equalsIgnoreCase(dto.getAuthTargetType(), "role")) {
                PerRole sysRole = perRoleMapper.selectById(dto.getAuthTargetId());
                authTargetName = ObjectUtils.isEmpty(sysRole) ? null : sysRole.getName();
            }
            dto.setAuthTargetName(authTargetName);
            TypeReference<List<Long>> listTypeReference = new TypeReference<List<Long>>() {};
            List<Long> userIdList =  JsonUtil.parseList(dto.getWhiteListUser(), listTypeReference);
            List<UserFormVO> sysUsers = new ArrayList<>();
            if (!CollectionUtils.isEmpty(userIdList)) {
                for (Long aLong : userIdList) {
                    UserFormVO vo = userPageManage.queryForm(aLong);
                    sysUsers.add(vo);
                }
                dto.setWhiteListUsers(sysUsers);
            }
        }
        return pager;
    }

    @Override
    public void save(@RequestBody DataSetColumnPermissionsDTO dataSetColumnPermissionsDTO) {
        if (StringUtils.equalsIgnoreCase(dataSetColumnPermissionsDTO.getAuthTargetType(), "sysParams")) {
            dataSetColumnPermissionsDTO.setAuthTargetIds(null);
        }
        if (dataSetColumnPermissionsDTO.getId() == null) {
            dataSetColumnPermissionsDTO.setId(IDUtils.snowID());
            dataSetColumnPermissionsDTO.setUpdateTime(System.currentTimeMillis());
            PerDatasetColumnPermissions datasetColumnPermissions = new PerDatasetColumnPermissions();
            BeanUtils.copyBean(datasetColumnPermissions, dataSetColumnPermissionsDTO);
            columnPermissionsMapper.insert(datasetColumnPermissions);
        } else {
            dataSetColumnPermissionsDTO.setUpdateTime(System.currentTimeMillis());
            PerDatasetColumnPermissions perDatasetColumnPermissions = new PerDatasetColumnPermissions();
            BeanUtils.copyBean(perDatasetColumnPermissions, dataSetColumnPermissionsDTO);
            columnPermissionsMapper.updateById(perDatasetColumnPermissions);
        }
    }


    @Override
    public void delete(@RequestBody DataSetColumnPermissionsDTO dataSetColumnPermissionsDTO) {
        columnPermissionsMapper.deleteById(dataSetColumnPermissionsDTO.getId());
    }

    @Override
    public List<DataSetColumnPermissionsDTO> list(DataSetColumnPermissionsDTO dataSetColumnPermissionsDTO) {
        QueryWrapper<PerDatasetColumnPermissions> wrapper = new QueryWrapper<>();
        if (dataSetColumnPermissionsDTO.getDatasetId() != null) {
            wrapper.eq("dataset_id", dataSetColumnPermissionsDTO.getDatasetId());
        }
        if (StringUtils.isNotEmpty(dataSetColumnPermissionsDTO.getAuthTargetType())) {
            wrapper.eq("auth_target_type", dataSetColumnPermissionsDTO.getAuthTargetType());
        }
        if (ObjectUtils.isNotEmpty(dataSetColumnPermissionsDTO.getAuthTargetId())) {
            wrapper.eq("auth_target_id", dataSetColumnPermissionsDTO.getAuthTargetId());
        }
        if (!CollectionUtils.isEmpty(dataSetColumnPermissionsDTO.getAuthTargetIds())) {
            wrapper.in("auth_target_id", dataSetColumnPermissionsDTO.getAuthTargetIds());
        }
        if (ObjectUtils.isNotEmpty(dataSetColumnPermissionsDTO.getEnable())) {
            wrapper.eq("enable", dataSetColumnPermissionsDTO.getEnable());
        }
        List<DataSetColumnPermissionsDTO> list = new ArrayList<>();
        for (PerDatasetColumnPermissions perDatasetColumnPermissions : columnPermissionsMapper.selectList(wrapper)) {
            DataSetColumnPermissionsDTO dataSetColumnPermissionsDT = new DataSetColumnPermissionsDTO();
            BeanUtils.copyBean(dataSetColumnPermissionsDT, perDatasetColumnPermissions);
            list.add(dataSetColumnPermissionsDT);
        }
        return list;
    };

    public UserFormVO getUserById(Long id) {
        return userPageManage.queryForm(id);
    }

}
