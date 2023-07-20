package io.dataease.xpack.permissions.dataset.server;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import io.dataease.api.permissions.auth.dto.BusiPermissionRequest;
import io.dataease.api.permissions.auth.vo.PermissionItem;
import io.dataease.api.permissions.dataset.api.RowPermissionsApi;
import io.dataease.api.permissions.dataset.dto.*;
import io.dataease.api.permissions.role.dto.UserRequest;
import io.dataease.api.permissions.user.vo.UserFormVO;
import io.dataease.api.permissions.user.vo.UserGridVO;
import io.dataease.api.permissions.user.vo.UserItem;
import io.dataease.utils.AuthUtils;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.IDUtils;
import io.dataease.utils.JsonUtil;
import io.dataease.xpack.permissions.auth.manage.AuthManage;
import io.dataease.xpack.permissions.dataset.dto.auto.entity.PerDatasetRowPermissionsTree;
import io.dataease.xpack.permissions.dataset.dto.auto.mapper.PerDatasetRowPermissionsTreeMapper;
import io.dataease.xpack.permissions.dataset.dto.ext.mapper.DatasetTableFieldExtMapper;
import io.dataease.xpack.permissions.dataset.dto.ext.mapper.RowPermissionsExtMapper;
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

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dataset/rowPermissions")
@Primary
public class RowPermissionServer implements RowPermissionsApi {
    @Resource
    private PerDatasetRowPermissionsTreeMapper rowPermissionsTreeMapper;
    @Resource
    private RowPermissionsExtMapper rowPermissionsExtMapper;
    @Resource
    private AuthManage authManage;
    @Resource
    private UserPageManage userPageManage;
    @Resource
    private PerUserMapper perUserMapper;
    @Resource
    private PerRoleMapper perRoleMapper;
    @Resource
    private UserExtMapper userExtMapper;
    @Resource
    private DatasetTableFieldExtMapper datasetTableFieldExtMapper;
    @Override
    public IPage<DataSetRowPermissionsTreeDTO> rowPermissions(@PathVariable Long datasetId, @PathVariable int goPage, @PathVariable int pageSize) {
        QueryWrapper<DataSetRowPermissionsTreeDTO> wrapper = new QueryWrapper<>();
        wrapper.eq("dataset_id", datasetId);
        Page<DataSetRowPermissionsTreeDTO> page = new Page<>(goPage, pageSize);
        IPage<DataSetRowPermissionsTreeDTO> pager = rowPermissionsExtMapper.pager(page, wrapper);

        List<DataSetRowPermissionsTreeDTO> records = pager.getRecords();
        records = CollectionUtils.isEmpty(records) ? new ArrayList<>(): records.stream().map(this::transObj).collect(Collectors.toList()) ;
        pager.setRecords(records);
        return pager;
    }

    @Override
    public void save(@RequestBody DataSetRowPermissionsTreeDTO datasetRowPermissions) {
        if (StringUtils.equalsIgnoreCase(datasetRowPermissions.getAuthTargetType(), "sysParams")) {
            datasetRowPermissions.setAuthTargetIds(null);
        }
        if (datasetRowPermissions.getId() == null) {
            datasetRowPermissions.setId(IDUtils.snowID());
            datasetRowPermissions.setUpdateTime(System.currentTimeMillis());
            PerDatasetRowPermissionsTree perDatasetRowPermissionsTree = new PerDatasetRowPermissionsTree();
            BeanUtils.copyBean(perDatasetRowPermissionsTree, datasetRowPermissions);
            rowPermissionsTreeMapper.insert(perDatasetRowPermissionsTree);
        } else {
            datasetRowPermissions.setUpdateTime(System.currentTimeMillis());
            PerDatasetRowPermissionsTree perDatasetRowPermissionsTree = new PerDatasetRowPermissionsTree();
            BeanUtils.copyBean(perDatasetRowPermissionsTree, datasetRowPermissions);
            rowPermissionsTreeMapper.updateById(perDatasetRowPermissionsTree);
        }
    }

    @Override
    public void delete(@RequestBody DataSetRowPermissionsTreeDTO datasetRowPermissions) {
        rowPermissionsTreeMapper.deleteById(datasetRowPermissions.getId());
    }

    @Override
    public List<Item> authObjs(Long datasetId, String type) {
        List<Item> list = new ArrayList<>();
        BusiPermissionRequest request = new BusiPermissionRequest();
        request.setFlag("DATASET");
        request.setId(datasetId);
        switch (type) {
            case "user":
                request.setType(0);
                List<Long> userIds = authManage.busiTargetPermission(request).getPermissions().stream().map(PermissionItem::getId).collect(Collectors.toList());
                if(!CollectionUtils.isEmpty(userIds)){
                    QueryWrapper<PerUser> wrapper = new QueryWrapper<>();
                    wrapper.in("id", userIds);
                    perUserMapper.selectList(wrapper).forEach(perUser -> {
                        Item item = new Item();
                        item.setId(perUser.getId());
                        item.setName(perUser.getName());
                        list.add(item);
                    });
                }
                break;
            case "role":
                request.setType(1);
                List<Long> roleIds = authManage.busiTargetPermission(request).getPermissions().stream().map(PermissionItem::getId).collect(Collectors.toList());
                if(!CollectionUtils.isEmpty(roleIds)){
                    QueryWrapper<PerRole> wrapper = new QueryWrapper<>();
                    wrapper.in("id", roleIds);
                    perRoleMapper.selectList(wrapper).forEach(perRole -> {
                        Item item = new Item();
                        item.setId(perRole.getId());
                        item.setName(perRole.getName());
                        list.add(item);
                    });
                }
                break;
            default:
                break;
        }
        return list;
    }

    @Override
    public DataSetRowPermissionsTreeDTO dataSetRowPermissionInfo(@RequestBody DataSetRowPermissionsTreeDTO request) {
        return new DataSetRowPermissionsTreeDTO();
    }

    @Override
    public List<UserFormVO> whiteListUsers(WhiteListUsersRequest whiteListUsersRequest) {
        List<UserFormVO> voList = new ArrayList<>();
        switch (whiteListUsersRequest.getAuthTargetType()){
            case "role":
                UserRequest request = new UserRequest();
                request.setRid(whiteListUsersRequest.getAuthTargetId());
                for (UserItem userItem : userPageManage.selectedForRole(request)) {
                    UserFormVO vo = new UserFormVO();
                    vo.setName(userItem.getName());
                    vo.setId(userItem.getId());
                    voList.add(vo);
                }
                break;
            case "sysParams":
                Long oid = AuthUtils.getUser().getDefaultOid();
                QueryWrapper<UserGridVO> wrapper = new QueryWrapper<>();
                wrapper.eq("pur.oid", oid);
                for (UserGridVO userGridVO : userExtMapper.ListUserForOrg(wrapper)) {
                    UserFormVO vo = new UserFormVO();
                    vo.setName(userGridVO.getName());
                    vo.setId(userGridVO.getId());
                    voList.add(vo);
                }
                break;
        }

        return voList;
    }

    @Override
    public UserFormVO getUserById(Long id) {
        return userPageManage.queryForm(id);
    }

    public List<DataSetRowPermissionsTreeDTO> list(DatasetRowPermissionsTreeRequest dataSetRowPermissionsTreeDTO) {
        QueryWrapper<PerDatasetRowPermissionsTree> wrapper = new QueryWrapper<>();
        if (dataSetRowPermissionsTreeDTO.getDatasetId() != null) {
            wrapper.eq("dataset_id", dataSetRowPermissionsTreeDTO.getDatasetId());
        }
        if (StringUtils.isNotEmpty(dataSetRowPermissionsTreeDTO.getAuthTargetType())) {
            wrapper.eq("auth_target_type", dataSetRowPermissionsTreeDTO.getAuthTargetType());
        }
        if (ObjectUtils.isNotEmpty(dataSetRowPermissionsTreeDTO.getAuthTargetId())) {
            wrapper.eq("auth_target_id", dataSetRowPermissionsTreeDTO.getAuthTargetId());
        }
        if (!CollectionUtils.isEmpty(dataSetRowPermissionsTreeDTO.getAuthTargetIds())) {
            wrapper.in("auth_target_id", dataSetRowPermissionsTreeDTO.getAuthTargetIds());
        }
        if (ObjectUtils.isNotEmpty(dataSetRowPermissionsTreeDTO.getEnable())) {
            wrapper.eq("enable", dataSetRowPermissionsTreeDTO.getEnable());
        }
        List<PerDatasetRowPermissionsTree> list = rowPermissionsTreeMapper.selectList(wrapper);
        return CollectionUtils.isEmpty(list) ? new ArrayList<>(): list.stream().map(this::transObj).collect(Collectors.toList()) ;
    };

    private DataSetRowPermissionsTreeDTO transObj(DataSetRowPermissionsTreeDTO record) {
        PerDatasetRowPermissionsTree perDatasetRowPermissionsTree = new PerDatasetRowPermissionsTree();
        org.springframework.beans.BeanUtils.copyProperties(record, perDatasetRowPermissionsTree);
        return transObj(perDatasetRowPermissionsTree);
    }
    private DataSetRowPermissionsTreeDTO transObj(PerDatasetRowPermissionsTree record) {
        DataSetRowPermissionsTreeDTO dto = new DataSetRowPermissionsTreeDTO();
        org.springframework.beans.BeanUtils.copyProperties(record, dto);
        String authTargetName = null;
        if (StringUtils.equalsIgnoreCase(dto.getAuthTargetType(), "user")) {
            PerUser sysUser = perUserMapper.selectById(dto.getAuthTargetId());
            authTargetName = ObjectUtils.isEmpty(sysUser) ? null : sysUser.getName();
        }
        if (StringUtils.equalsIgnoreCase(dto.getAuthTargetType(), "role")) {
            PerRole sysRole = perRoleMapper.selectById(dto.getAuthTargetId());
            authTargetName = ObjectUtils.isEmpty(sysRole) ? null : sysRole.getName();
        }

        DatasetRowPermissionsTreeObj tree = JsonUtil.parseObject(dto.getExpressionTree(), DatasetRowPermissionsTreeObj.class);

        TypeReference<List<Long>> listTypeReference = new TypeReference<List<Long>>() {};
        List<Long> userIdList =  JsonUtil.parseList(dto.getWhiteListUser(), listTypeReference);
        List<UserFormVO> sysUsers = new ArrayList<>();
        if (!CollectionUtils.isEmpty(userIdList)) {
            for (Long aLong : userIdList) {
                UserFormVO vo = userPageManage.queryForm(aLong);
                sysUsers.add(vo);
            }
        }
        dto.setAuthTargetName(authTargetName);
        dto.setTree(tree);
        dto.setWhiteListUsers(sysUsers);
        getField(dto.getTree());
        return dto;
    }

    private void getField(DatasetRowPermissionsTreeObj tree) {
        if (ObjectUtils.isNotEmpty(tree)) {
            if (ObjectUtils.isNotEmpty(tree.getItems())) {
                for (DatasetRowPermissionsTreeItem item : tree.getItems()) {
                    if (ObjectUtils.isNotEmpty(item)) {
                        if (StringUtils.equalsIgnoreCase(item.getType(), "item") || ObjectUtils.isEmpty(item.getSubTree())) {
                            item.setField(datasetTableFieldExtMapper.selectDatasetTableFieldDTO(item.getFieldId()));
                        } else if (StringUtils.equalsIgnoreCase(item.getType(), "tree") || (ObjectUtils.isNotEmpty(item.getSubTree()) && StringUtils.isNotEmpty(item.getSubTree().getLogic()))) {
                            getField(item.getSubTree());
                        }
                    }
                }
            }
        }
    }
}
