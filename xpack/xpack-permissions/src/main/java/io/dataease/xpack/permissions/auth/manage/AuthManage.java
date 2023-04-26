package io.dataease.xpack.permissions.auth.manage;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.permissions.auth.dto.BusiPerEditor;
import io.dataease.api.permissions.auth.dto.BusiPermissionRequest;
import io.dataease.api.permissions.auth.dto.MenuPerEditor;
import io.dataease.api.permissions.auth.dto.MenuPermissionRequest;
import io.dataease.api.permissions.auth.vo.PermissionItem;
import io.dataease.api.permissions.auth.vo.PermissionOrigin;
import io.dataease.api.permissions.auth.vo.PermissionVO;
import io.dataease.api.permissions.auth.vo.ResourceVO;
import io.dataease.auth.bo.TokenUserBO;
import io.dataease.constant.BusiResourceEnum;
import io.dataease.exception.DEException;
import io.dataease.utils.AuthUtils;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.IDUtils;
import io.dataease.xpack.permissions.auth.bo.ResourceTreeNode;
import io.dataease.xpack.permissions.auth.dao.auto.entity.PerAuthBusiRole;
import io.dataease.xpack.permissions.auth.dao.auto.entity.PerAuthBusiUser;
import io.dataease.xpack.permissions.auth.dao.auto.entity.PerAuthMenu;
import io.dataease.xpack.permissions.auth.dao.auto.mapper.PerAuthBusiRoleMapper;
import io.dataease.xpack.permissions.auth.dao.auto.mapper.PerAuthBusiUserMapper;
import io.dataease.xpack.permissions.auth.dao.auto.mapper.PerAuthMenuMapper;
import io.dataease.xpack.permissions.auth.dao.ext.entity.BusiResourcePO;
import io.dataease.xpack.permissions.auth.dao.ext.mapper.BusiAuthExtMapper;
import io.dataease.xpack.permissions.auth.dao.ext.mapper.MenuAuthExtMapper;
import io.dataease.xpack.permissions.user.entity.RoleInfo;
import io.dataease.xpack.permissions.user.entity.UserRole;
import io.dataease.xpack.permissions.user.manage.RoleManage;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AuthManage {
    private final static int ROOTID = 0;

    private final static String I18N_PREFIX = "i18n_auth_menu.";

    @Resource
    private BusiAuthExtMapper busiAuthExtMapper;

    @Resource
    private MenuAuthExtMapper menuAuthExtMapper;

    @Resource
    private RoleManage roleManage;

    @Resource
    private PerAuthBusiUserMapper perAuthBusiUserMapper;

    @Resource
    private PerAuthBusiRoleMapper perAuthBusiRoleMapper;

    @Resource
    private UserAuthManage userAuthManage;

    @Resource
    private RoleAuthManage roleAuthManage;

    @Resource
    private PerAuthMenuMapper perAuthMenuMapper;

    @Resource
    private MenuAuthManage menuAuthManage;

    private AuthManageUtil authManageUtil;

    public List<ResourceVO> resourceTree(String flag) {
        BusiResourceEnum busiResourceEnum = BusiResourceEnum.valueOf(flag.toUpperCase());
        if (ObjectUtils.isEmpty(busiResourceEnum)) {
            DEException.throwException("invalid flag value");
        }
        TokenUserBO user = AuthUtils.getUser();
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("org_id", user.getDefaultOid());
        queryWrapper.eq("rt_id", busiResourceEnum.getFlag());
        List<BusiResourcePO> pos = busiAuthExtMapper.query(queryWrapper);
        return authManageUtil.convertPos(pos, false);
    }

    public List<ResourceVO> menuTree() {
        QueryWrapper queryWrapper = new QueryWrapper();
        List<BusiResourcePO> pos = menuAuthExtMapper.query(queryWrapper);
        return authManageUtil.convertPos(pos, true);
    }

    public List<PermissionVO> menuPermission(MenuPermissionRequest request) {
        List<PermissionVO> result = new ArrayList<>();
        PermissionVO vo = new PermissionVO();
        RoleInfo roleInfo = roleManage.roleInfo(request.getId());
        if (roleInfo.isRoot()) {
            BeanUtils.copyBean(vo, roleInfo, "permissions");
            result.add(vo);
            return result;
        }
        List<PermissionItem> permissionItems = menuAuthExtMapper.rolePermission(request.getId());
        vo.setPermissions(permissionItems);
        result.add(vo);
        return result;
    }

    public List<PermissionVO> busiPermission(BusiPermissionRequest request) {
        List<PermissionVO> result = new ArrayList<>();
        BusiResourceEnum busiResourceEnum = BusiResourceEnum.valueOf(request.getFlag().toUpperCase());
        if (ObjectUtils.isEmpty(busiResourceEnum)) {
            DEException.throwException("invalid flag value");
        }
        PermissionVO vo = new PermissionVO();
        if (request.getType() == 0) {
            List<UserRole> userRoles = roleManage.userRole(request.getId());
            if (CollectionUtil.isNotEmpty(userRoles) && userRoles.stream().anyMatch(item -> item.isRoot() && !item.isReadonly())) {
                vo.setRoot(true);
                vo.setReadonly(false);
                result.add(vo);
                return result;
            }
            List<PermissionItem> permissionItems = busiAuthExtMapper.userPermission(request.getId(), busiResourceEnum.getFlag());
            vo.setPermissions(permissionItems);
            result.add(vo);
            if (CollectionUtil.isNotEmpty(userRoles)) {
                List<PermissionVO> permissionVOS = userRoles.stream().map(role -> {
                    List<PermissionItem> permission = busiAuthExtMapper.rolePermission(role.getId(), busiResourceEnum.getFlag());
                    PermissionVO temp = new PermissionVO();
                    temp.setRoot(role.isRoot());
                    temp.setReadonly(role.isReadonly());
                    temp.setPermissions(permission);
                    PermissionOrigin origin = new PermissionOrigin();
                    origin.setId(role.getId());
                    origin.setName(role.getName());
                    temp.setPermissionOrigin(origin);
                    return temp;
                }).toList();
                result.addAll(permissionVOS);
            }
        } else {
            RoleInfo roleInfo = roleManage.roleInfo(request.getId());
            if (roleInfo.isRoot()) {
                BeanUtils.copyBean(vo, roleInfo, "permissions");
                result.add(vo);
                return result;
            }

            List<PermissionItem> permissionItems = busiAuthExtMapper.rolePermission(request.getId(), busiResourceEnum.getFlag());
            vo.setPermissions(permissionItems);
            result.add(vo);
        }
        return result;
    }

    @Transactional
    public void saveBusiPer(BusiPerEditor editor) {
        if (CollectionUtil.isEmpty(editor.getPermissions())) return;
        String flagName = editor.getFlag().toUpperCase();
        int flag = BusiResourceEnum.valueOf(flagName).getFlag();
        Long id = editor.getId();
        List<PermissionItem> permissions = editor.getPermissions();
        Integer type = editor.getType();
        deleteBusiPer(flag, id, permissions, type);
        if (type == 0) {
            List<PerAuthBusiUser> busiUsers = permissions.stream().map(per -> {
                PerAuthBusiUser userPermission = new PerAuthBusiUser();
                userPermission.setId(IDUtils.snowID());
                userPermission.setResourceId(per.getId());
                userPermission.setWeight(per.getWeight());
                userPermission.setUid(id);
                userPermission.setResourceType(flag);
                return userPermission;
            }).toList();
            userAuthManage.saveBatch(busiUsers);
        } else {
            List<PerAuthBusiRole> busiRoles = permissions.stream().map(per -> {
                PerAuthBusiRole rolePermission = new PerAuthBusiRole();
                rolePermission.setId(IDUtils.snowID());
                rolePermission.setResourceId(per.getId());
                rolePermission.setWeight(per.getWeight());
                rolePermission.setRid(id);
                rolePermission.setResourceType(flag);
                return rolePermission;
            }).toList();
            roleAuthManage.saveBatch(busiRoles);
        }
    }

    @Transactional
    public void deleteBusiPer(int flag, Long id, List<PermissionItem> permissions, int type) {
        List<Long> ids = permissions.stream().map(PermissionItem::getId).toList();
        if (type == 0) {
            QueryWrapper<PerAuthBusiUser> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("uid", id);
            queryWrapper.eq("resource_type", flag);
            queryWrapper.in("resource_id", ids);
            perAuthBusiUserMapper.delete(queryWrapper);
        } else {
            QueryWrapper<PerAuthBusiRole> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("rid", id);
            queryWrapper.eq("resource_type", flag);
            queryWrapper.in("resource_id", ids);
            perAuthBusiRoleMapper.delete(queryWrapper);
        }
    }

    public void saveMenuPer(MenuPerEditor editor) {
        List<PermissionItem> permissionItems = null;
        if (CollectionUtil.isEmpty((permissionItems = editor.getPermissions()))) {
            return;
        }
        Long id = editor.getId();
        List<Long> ids = permissionItems.stream().map(PermissionItem::getId).toList();
        QueryWrapper<PerAuthMenu> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("rid", id);
        queryWrapper.in("resource_id", ids);
        perAuthMenuMapper.delete(queryWrapper);

        List<PerAuthMenu> perAuthMenus = permissionItems.stream().map(per -> {
            PerAuthMenu perAuthMenu = new PerAuthMenu();
            perAuthMenu.setId(IDUtils.snowID());
            perAuthMenu.setResourceId(per.getId());
            perAuthMenu.setWeight(per.getWeight());
            perAuthMenu.setRid(id);
            return perAuthMenu;
        }).toList();
        menuAuthManage.saveBatch(perAuthMenus);
    }


     private class AuthManageUtil {
        public List<ResourceVO> convertPos(List<BusiResourcePO> pos, boolean appendI18nPrefix) {
            List<ResourceTreeNode> nodes = pos.stream().map(po -> BeanUtils.copyBean(new ResourceTreeNode(), po)).collect(Collectors.toList());
            List<ResourceTreeNode> treeNodes = poTree(nodes);
            return convertTree(treeNodes, appendI18nPrefix);
        }

         private List<ResourceTreeNode> poTree(List<ResourceTreeNode> nodeList) {
            List<ResourceTreeNode> result = new ArrayList<>();
            Map<Long, List<ResourceTreeNode>> childMap = nodeList.stream().collect(Collectors.groupingBy(ResourceTreeNode::getPid));
            nodeList.forEach(po -> {
                po.setChildren(childMap.get(po.getId()));
                if (po.getPid() == ROOTID) {
                    result.add(po);
                }
            });
            return result;
        }

         private List<ResourceVO> convertTree(List<ResourceTreeNode> roots, boolean appendI18nPrefix) {
            List<ResourceVO> result = new ArrayList<>();
            for (int i = 0; i < roots.size(); i++) {
                ResourceTreeNode node = roots.get(i);
                ResourceVO vo = BeanUtils.copyBean(new ResourceVO(), node, "children");
                if (appendI18nPrefix) {
                    vo.setName(I18N_PREFIX + vo.getName());
                }
                result.add(vo);
                List<ResourceTreeNode> children = null;
                if (!CollectionUtils.isEmpty(children = node.getChildren())) {
                    vo.setChildren(convertTree(children, appendI18nPrefix));
                }
            }
            return result;
        }
    }

    @PostConstruct
    public void init() {
        authManageUtil = new AuthManageUtil();
    }



}
