package io.dataease.xpack.permissions.auth.manage;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.permissions.auth.dto.*;
import io.dataease.api.permissions.auth.vo.PermissionItem;
import io.dataease.api.permissions.auth.vo.PermissionOrigin;
import io.dataease.api.permissions.auth.vo.PermissionVO;
import io.dataease.api.permissions.auth.vo.ResourceVO;
import io.dataease.auth.bo.TokenUserBO;
import io.dataease.constant.BusiResourceEnum;
import io.dataease.exception.DEException;
import io.dataease.utils.*;
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
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Transactional
public class AuthManage {


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

    @Resource
    private AuthWeightService authWeightService;

    @Resource
    private BusiAuthManage busiAuthManage;

    public List<ResourceVO> resourceTree(String flag) {
        BusiResourceEnum busiResourceEnum = BusiResourceEnum.valueOf(flag.toUpperCase());
        if (ObjectUtils.isEmpty(busiResourceEnum)) {
            DEException.throwException("invalid flag value");
        }
        TokenUserBO user = AuthUtils.getUser();
        // 判断是否包含组织默认管理员角色
        List<UserRole> userRoles = null;
        AtomicBoolean rootAdmin = new AtomicBoolean(AuthUtils.isSysAdmin());
        if (!rootAdmin.get() && CollectionUtil.isNotEmpty(userRoles = roleManage.userAdminRole()))
            userRoles = userRoles.stream().filter(role -> {
                if (role.isRoot()) {
                    rootAdmin.set(true);
                    return false;
                }
                return true;
            }).toList();
        List<BusiResourcePO> pos = null;
        if (rootAdmin.get()) {
            pos = busiAuthManage.resourceWithOid(busiResourceEnum);
        } else {
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("pbr.org_id", user.getDefaultOid());
            queryWrapper.eq("pbr.rt_id", busiResourceEnum.getFlag());
            queryWrapper.eq("pabu.uid", user.getUserId());
            queryWrapper.eq("pabu.weight", 9);
            queryWrapper.eq("pabu.resource_type", busiResourceEnum.getFlag());
            pos = busiAuthExtMapper.resourceByUid(queryWrapper);

            if (CollectionUtil.isNotEmpty(userRoles)) {
                queryWrapper.clear();
                List<Long> rids = userRoles.stream().map(UserRole::getId).toList();
                queryWrapper.eq("pbr.org_id", user.getDefaultOid());
                queryWrapper.eq("pbr.rt_id", busiResourceEnum.getFlag());
                queryWrapper.eq("pabr.weight", 9);
                queryWrapper.eq("pabr.resource_type", busiResourceEnum.getFlag());
                queryWrapper.in("pabr.rid", rids);
                List<BusiResourcePO> rolePos = null;
                if (CollectionUtil.isNotEmpty((rolePos = busiAuthExtMapper.resourceByRid(queryWrapper)))) {
                    pos.addAll(rolePos);
                }
            }
            if (CollectionUtil.isNotEmpty(pos)) {
                pos = CollectionUtil.distinct(pos);
            } else {
                return null;
            }
        }
        return TreeUtils.mergeTree(pos, 0L, ResourceVO.class, false);
    }

    public List<ResourceVO> menuTree() {
        AtomicBoolean rootAdmin = new AtomicBoolean(AuthUtils.isSysAdmin());
        List<BusiResourcePO> pos = null;
        List<UserRole> userRoles = null;


        if (!rootAdmin.get() && CollectionUtil.isNotEmpty(userRoles = roleManage.userAdminRole()))
            userRoles = userRoles.stream().filter(role -> {
                if (role.isRoot()) {
                    rootAdmin.set(true);
                    return false;
                }
                return true;
            }).toList();

        if (rootAdmin.get()) {
            QueryWrapper queryWrapper = new QueryWrapper();
            pos = menuAuthExtMapper.query(queryWrapper);
        } else if (CollectionUtil.isNotEmpty(userRoles)) {

            List<Long> rids = userRoles.stream().map(UserRole::getId).toList();
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("pam.weight", 9);
            queryWrapper.in("pam.rid", rids);
            pos = menuAuthExtMapper.menusByRids(queryWrapper);
        } else {
            return null;
        }

        if (CollectionUtil.isNotEmpty(pos)) {
            pos = CollectionUtil.distinct(pos);
        } else {
            return null;
        }
        return TreeUtils.mergeTree(pos, 0L, ResourceVO.class, true);
    }

    public PermissionVO menuPermission(MenuPermissionRequest request) {
        PermissionVO vo = new PermissionVO();
        RoleInfo roleInfo = roleManage.roleInfo(request.getId());
        if (roleInfo.isRoot()) {
            BeanUtils.copyBean(vo, roleInfo, "permissions");
            return vo;
        }
        List<PermissionItem> permissionItems = menuAuthManage.permissionItems(request.getId());
        vo.setPermissions(permissionItems);
        return vo;
    }

    public PermissionVO busiPermission(BusiPermissionRequest request) {
        BusiResourceEnum busiResourceEnum = BusiResourceEnum.valueOf(request.getFlag().toUpperCase());
        if (ObjectUtils.isEmpty(busiResourceEnum)) {
            DEException.throwException("invalid flag value");
        }
        Long oid = AuthUtils.getUser().getDefaultOid();
        int flag = busiResourceEnum.getFlag();
        PermissionVO vo = new PermissionVO();
        if (request.getType() == 0) {
            List<UserRole> userRoles = roleManage.userRole(request.getId(), oid);

            if (CollectionUtil.isNotEmpty(userRoles)) {
                for (int i = 0; i < userRoles.size(); i++) {
                    UserRole item = userRoles.get(i);
                    if (item.isRoot()) {
                        vo.setRoot(true);
                        vo.setReadonly(item.isReadonly());
                        if (!item.isReadonly()) {
                            return vo;
                        }
                    }
                }
                userRoles = userRoles.stream().filter(item -> !item.isRoot()).toList();
            }
            List<PermissionItem> permissionItems = userAuthManage.permissionItems(request.getId(), oid, flag);
            vo.setPermissions(permissionItems);

            if (CollectionUtil.isNotEmpty(userRoles)) {
                List<PermissionOrigin> permissionOrigins = roleAuthManage.roleOrigin(userRoles, flag);
                vo.setPermissionOrigins(permissionOrigins);
            }
            return vo;
        } else {
            RoleInfo roleInfo = roleManage.roleInfo(request.getId());
            if (roleInfo.isRoot()) {
                BeanUtils.copyBean(vo, roleInfo, "permissions");
                return vo;
            }
            List<PermissionItem> permissionItems = roleAuthManage.permissionItems(request.getId(), flag);
            vo.setPermissions(permissionItems);
            return vo;
        }
    }

    public PermissionVO busiTargetPermission(BusiPermissionRequest request) {
        PermissionVO vo = new PermissionVO();
        Long resourceId = request.getId();
        Integer type = request.getType();
        BusiResourceEnum busiResourceEnum = BusiResourceEnum.valueOf(request.getFlag().toUpperCase());
        if (ObjectUtils.isEmpty(busiResourceEnum)) {
            DEException.throwException("invalid flag value");
        }
        if (type == 0) {
            // 资源授权给了哪些用户？
            List<PermissionItem> userPermissionItems = busiAuthExtMapper.busiUserPermission(resourceId, busiResourceEnum.getFlag());
            List<PermissionOrigin> permissionOrigins = busiAuthExtMapper.batchUserRolePermission(resourceId, busiResourceEnum.getFlag());
            PermissionOrigin adminOrigin = null;
            PermissionOrigin readonlyOrigin = null;
            if (ObjectUtils.isNotEmpty(adminOrigin = defaultAdminOrigin())) {
                permissionOrigins.add(adminOrigin);
            }
            if (ObjectUtils.isNotEmpty(readonlyOrigin = defaultReadonlyOrigin())) {
                permissionOrigins.add(readonlyOrigin);
            }
            vo.setPermissions(authWeightService.filterValid(userPermissionItems));
            if (CollectionUtil.isNotEmpty(permissionOrigins)) {
                List<PermissionOrigin> origins = permissionOrigins.stream().map(origin -> {
                    origin.setPermissions(authWeightService.filterValid(origin.getPermissions()));
                    return origin;
                }).toList();
                vo.setPermissionOrigins(origins);
            }
        } else {
            // 资源授权给了哪些角色？
            List<PermissionItem> permissionItems = busiAuthExtMapper.busiRolePermission(resourceId, busiResourceEnum.getFlag());
            vo.setPermissions(authWeightService.filterValid(permissionItems));
        }
        return vo;
    }

    private PermissionOrigin defaultAdminOrigin() {
        return busiAuthExtMapper.adminOrigin(AuthUtils.getUser().getDefaultOid());
    }

    private PermissionOrigin defaultReadonlyOrigin() {
        return busiAuthExtMapper.readonlyOrigin(AuthUtils.getUser().getDefaultOid());
    }

    public PermissionVO menuTargetPermission(MenuPermissionRequest request) {
        PermissionVO vo = new PermissionVO();
        List<PermissionItem> permissionItems = menuAuthExtMapper.menuTargetPermission(request.getId());
        vo.setPermissions(authWeightService.filterValid(permissionItems));
        return vo;
    }

    @Transactional
    public void saveBusiPer(BusiPerEditor editor) {
        if (CollectionUtil.isEmpty(editor.getPermissions())) return;
        String flagName = editor.getFlag().toUpperCase();
        int flag = BusiResourceEnum.valueOf(flagName).getFlag();
        Long id = editor.getId();
        List<PermissionItem> paramPermissions = editor.getPermissions();
        Integer type = editor.getType();
        deleteBusiPer(flag, id, paramPermissions, type);
        List<PermissionItem> permissions = authWeightService.filterValid(paramPermissions);
        Long oid = AuthUtils.getUser().getDefaultOid();
        if (type == 0) {
            CacheUtils.remove("user_busi_pers", id.toString() + oid + flag, t -> {
                if (CollectionUtil.isNotEmpty(permissions)) {
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
                }
            });
        } else {
            CacheUtils.remove("role_busi_pers", id.toString() + flag, t -> {
                if (CollectionUtil.isNotEmpty(permissions)) {
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
            });
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

    @Transactional
    public void deleteBusiTargetPer(int flag, List<Long> resourceIds, List<PermissionItem> permissions, int type) {
        List<Long> ids = permissions.stream().map(PermissionItem::getId).toList();
        if (type == 0) {
            QueryWrapper<PerAuthBusiUser> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("resource_type", flag);
            queryWrapper.in("resource_id", resourceIds);
            queryWrapper.in("uid", ids);
            perAuthBusiUserMapper.delete(queryWrapper);
        } else {
            QueryWrapper<PerAuthBusiRole> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("resource_type", flag);
            queryWrapper.in("resource_id", resourceIds);
            queryWrapper.in("rid", ids);
            perAuthBusiRoleMapper.delete(queryWrapper);
        }
    }

    public void deleteMenuTargetPer(List<Long> resourceIds, List<PermissionItem> permissions) {
        List<Long> ids = permissions.stream().map(PermissionItem::getId).toList();
        QueryWrapper<PerAuthMenu> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("rid", ids);
        queryWrapper.in("resource_id", resourceIds);
        perAuthMenuMapper.delete(queryWrapper);
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
        List<PermissionItem> realPermissionItems = authWeightService.filterValid(permissionItems);

        CacheUtils.remove("role_menu_pers", id.toString(), t -> {
            if (CollectionUtil.isEmpty(realPermissionItems)) {
                List<PerAuthMenu> perAuthMenus = realPermissionItems.stream().map(per -> {
                    PerAuthMenu perAuthMenu = new PerAuthMenu();
                    perAuthMenu.setId(IDUtils.snowID());
                    perAuthMenu.setResourceId(per.getId());
                    perAuthMenu.setWeight(per.getWeight());
                    perAuthMenu.setRid(id);
                    return perAuthMenu;
                }).toList();
                menuAuthManage.saveBatch(perAuthMenus);
            }
        });

    }

    @Transactional
    public void saveBusiTargetPer(BusiTargetPerCreator creator) {
        if (CollectionUtil.isEmpty(creator.getPermissions())) return;
        String flagName = creator.getFlag().toUpperCase();
        int flag = BusiResourceEnum.valueOf(flagName).getFlag();
        List<Long> ids = creator.getIds();
        List<PermissionItem> permissions = creator.getPermissions();
        Integer type = creator.getType();
        if (0 == type) {
            List<Long> uids = permissions.stream().map(PermissionItem::getId).toList();
            List<PermissionBO> permissionBOS = busiAuthExtMapper.queryExistUserPer(ids, flag, uids);
            Map<Long, Map<Long, Integer>> mappingMap = formatMap(permissionBOS);
            Long dirId = ids.get(0);

            Map<Long, Integer> dirMap = mappingMap.getOrDefault(dirId, new HashMap<>());
            List<PerAuthBusiUser> busiUsers = permissions.stream().flatMap(per -> {
                Long uid = per.getId();
                int weight = per.getWeight();
                Integer dirSourceWeight = dirMap.getOrDefault(uid, 0);
                boolean asc = weight > dirSourceWeight;

                return ids.stream().map(resourceId -> {
                    PerAuthBusiUser busiUser = new PerAuthBusiUser();
                    busiUser.setResourceType(flag);
                    busiUser.setUid(uid);
                    busiUser.setWeight(asc ? Math.max(weight, mappingMap.getOrDefault(resourceId, new HashMap<>()).getOrDefault(uid, 0)) : weight);
                    busiUser.setResourceId(resourceId);
                    busiUser.setId(IDUtils.snowID());
                    return busiUser;
                });
            }).toList();
            Long oid = AuthUtils.getUser().getDefaultOid();
            CacheUtils.remove("user_busi_pers", uids.stream().map(uid -> uid.toString() + oid + flag).toList(), t -> {
                deleteBusiTargetPer(flag, ids, permissions, type);
                userAuthManage.saveBatch(busiUsers);
            });
        } else {
            List<Long> rids = permissions.stream().map(PermissionItem::getId).toList();
            List<PermissionBO> permissionBOS = busiAuthExtMapper.queryExistRolePer(ids, flag, rids);
            Map<Long, Map<Long, Integer>> mappingMap = formatMap(permissionBOS);
            Map<Long, Integer> dirMap = mappingMap.getOrDefault(ids.get(0), new HashMap<>());
            List<PerAuthBusiRole> busiRoles = permissions.stream().flatMap(per -> {
                Long rid = per.getId();
                int weight = per.getWeight();
                Integer dirSourceWeight = dirMap.getOrDefault(rid, 0);
                boolean asc = weight > dirSourceWeight;

                return ids.stream().map(resourceId -> {
                    PerAuthBusiRole busiRole = new PerAuthBusiRole();
                    busiRole.setResourceType(flag);
                    busiRole.setRid(rid);
                    busiRole.setWeight(asc ? Math.max(weight, mappingMap.getOrDefault(resourceId, new HashMap<>()).getOrDefault(rid, 0)) : weight);
                    busiRole.setResourceId(resourceId);
                    busiRole.setId(IDUtils.snowID());
                    return busiRole;
                });
            }).toList();
            CacheUtils.remove("role_busi_pers", rids.stream().map(rid -> rid.toString() + flag).toList(), t -> {
                deleteBusiTargetPer(flag, ids, permissions, type);
                roleAuthManage.saveBatch(busiRoles);
            });

        }
    }

    public void saveMenuTargetPer(MenuTargetPerCreator creator) {

        List<PermissionItem> permissions = creator.getPermissions();
        List<Long> ids = creator.getIds();
        List<Long> rids = permissions.stream().map(PermissionItem::getId).toList();
        List<PermissionBO> permissionBOS = menuAuthExtMapper.queryExistPer(ids, rids);
        Map<Long, Map<Long, Integer>> mappingMap = formatMap(permissionBOS);
        Map<Long, Integer> dirMap = mappingMap.getOrDefault(ids.get(0), new HashMap<>());
        List<PerAuthMenu> menuRoles = permissions.stream().flatMap(per -> {
            Long rid = per.getId();
            int weight = per.getWeight();
            Integer dirSourceWeight = dirMap.getOrDefault(rid, 0);
            boolean asc = weight > dirSourceWeight;

            return ids.stream().map(resourceId -> {
                PerAuthMenu menuRole = new PerAuthMenu();
                menuRole.setWeight(asc ? Math.max(weight, mappingMap.getOrDefault(resourceId, new HashMap<>()).getOrDefault(rid, 0)) : weight);
                menuRole.setResourceId(resourceId);
                menuRole.setId(IDUtils.snowID());
                menuRole.setRid(rid);
                return menuRole;
            });
        }).toList();
        CacheUtils.remove("role_menu_pers", rids.stream().map(rid -> rid.toString()).toList(), t -> {
            deleteMenuTargetPer(ids, permissions);
            menuAuthManage.saveBatch(menuRoles);
        });
    }

    private Map<Long, Map<Long, Integer>> formatMap(List<PermissionBO> list) {
        Map<Long, Map<Long, Integer>> result = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            PermissionBO bo = list.get(i);
            Map<Long, Integer> innerMap = result.getOrDefault(bo.getResourceId(), new HashMap<>());
            innerMap.put(bo.getId(), bo.getWeight());
            result.put(bo.getResourceId(), innerMap);
        }
        return result;
    }


}
