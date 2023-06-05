package io.dataease.xpack.permissions.auth.manage;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.dataease.api.permissions.auth.vo.PermissionItem;
import io.dataease.api.permissions.auth.vo.PermissionOrigin;
import io.dataease.utils.CacheUtils;
import io.dataease.utils.IDUtils;
import io.dataease.xpack.permissions.auth.dao.auto.entity.PerAuthBusiRole;
import io.dataease.xpack.permissions.auth.dao.auto.entity.PerAuthMenu;
import io.dataease.xpack.permissions.auth.dao.auto.mapper.PerAuthBusiRoleMapper;
import io.dataease.xpack.permissions.auth.dao.ext.entity.ResourcePO;
import io.dataease.xpack.permissions.auth.dao.ext.mapper.BusiAuthExtMapper;
import io.dataease.xpack.permissions.auth.dao.ext.mapper.MenuAuthExtMapper;
import io.dataease.xpack.permissions.user.dao.auto.entity.PerRole;
import io.dataease.xpack.permissions.user.entity.UserRole;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class RoleAuthManage extends ServiceImpl<PerAuthBusiRoleMapper, PerAuthBusiRole> {

    private static Map<Integer, List<Integer>> weightMap = new ConcurrentHashMap<>();

    @Resource
    private PerAuthBusiRoleMapper perAuthBusiRoleMapper;

    @Resource
    private BusiAuthExtMapper busiAuthExtMapper;

    @Resource
    private MenuAuthExtMapper menuAuthExtMapper;

    @Resource
    private MenuAuthManage menuAuthManage;

    @Resource
    private AuthWeightService authWeightService;

    public List<PerAuthBusiRole> ridForRootWay(String rootWay) {
        if (StringUtils.isBlank(rootWay)) return ListUtil.empty();
        List<String> ids = Arrays.stream(rootWay.split(",")).toList();
        if (CollectionUtil.isEmpty(ids)) return ListUtil.empty();
        return perAuthBusiRoleMapper.selectBatchIds(ids);
    }

    public void syncCascade(List<PerAuthBusiRole> pers, Long resourceId) {
        if (CollectionUtil.isEmpty(pers)) return;
        List<PerAuthBusiRole> perAuthBusiRoles = pers.stream().map(per -> {
            per.setId(IDUtils.snowID());
            per.setResourceId(resourceId);
            return per;
        }).toList();
        saveBatch(perAuthBusiRoles);
    }

    @Transactional
    public void syncForRoleCreate(PerRole perRole) {
        // 基础角色的资源及菜单权限都给当前角色
        initWeightMap();
        List<Long> menuIds = queryMenuIds();
        boolean readonly = perRole.getReadonly();
        List<ResourcePO> pos = busiAuthExtMapper.resourcePOs();

        List<PerAuthMenu> menus = syncItem(0, menuIds, perRole.getId(), readonly);
        menuAuthManage.saveBatch(menus);
        Map<Integer, List<ResourcePO>> listMap = pos.stream().collect(Collectors.groupingBy(ResourcePO::getType));
        if (CollectionUtil.isNotEmpty(listMap)) {
            List<Integer> busiTypes = List.of(1, 2, 3, 4);
            List<PerAuthBusiRole> perAuthBusiRoles = busiTypes.stream().flatMap(type -> syncBusiItem(type, listMap.get(type).stream().map(item -> item.getId()).toList(), perRole.getId(), readonly)).toList();
            saveBatch(perAuthBusiRoles, 1000);
        }
    }

    public List<PerAuthMenu> syncItem(int type, List<Long> ids, Long rid, boolean readonly) {
        List<Integer> weights = readonly ? List.of(1) : weightMap.get(type);
        return ids.stream().flatMap(resourceId -> weights.stream().map(weight -> {
            PerAuthMenu authMenu = new PerAuthMenu();
            authMenu.setRid(rid);
            authMenu.setWeight(weight);
            authMenu.setResourceId(resourceId);
            authMenu.setId(IDUtils.snowID());
            return authMenu;
        })).toList();
    }


    public Stream<PerAuthBusiRole> syncBusiItem(int type, List<Long> ids, Long rid, boolean readonly) {
        List<Integer> weights = readonly ? List.of(1) : weightMap.get(type);
        return ids.stream().flatMap(resourceId -> weights.stream().map(weight -> {
            PerAuthBusiRole authBusiRole = new PerAuthBusiRole();
            authBusiRole.setRid(rid);
            authBusiRole.setWeight(weight);
            authBusiRole.setResourceId(resourceId);
            authBusiRole.setId(IDUtils.snowID());
            authBusiRole.setResourceType(type);
            return authBusiRole;
        }));
    }


    public List<Long> queryMenuIds() {
        // 这里是固定的后面记得加上缓存
        return menuAuthExtMapper.menuIds();
    }

    @Cacheable(value = "role_busi_pers", key = "#rid.toString() + #flag.toString()")
    public List<PermissionItem> permissionItems(Long rid, Integer flag) {
        List<PermissionItem> permissionItems = busiAuthExtMapper.rolePermission(rid, flag);
        permissionItems = authWeightService.filterValid(permissionItems);
        return permissionItems;
    }

    public List<PermissionOrigin> roleOrigin(List<UserRole> userRoles, Integer flag) {
        boolean isMenu = flag == 0;
        List<Long> rids = userRoles.stream().filter(item -> !item.isRoot()).map(UserRole::getId).toList();
        String cacheName = isMenu ? "role_menu_pers" : "role_busi_pers";
        String keySuffix = isMenu ? "" : flag.toString();
        List<PermissionOrigin> cachePermissionOrigins = new ArrayList<>();
        rids = rids.stream().filter(rid -> {
            if (CacheUtils.keyExist(cacheName, rid.toString() + keySuffix)) {
                Object o = CacheUtils.get(cacheName, rid.toString() + keySuffix);
                PermissionOrigin origin = new PermissionOrigin();
                origin.setId(rid);
                origin.setPermissions((List<PermissionItem>) o);
                cachePermissionOrigins.add(origin);
                return false;
            }
            return true;
        }).toList();
        List<PermissionOrigin> permissionOrigins = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(rids)) {
            permissionOrigins = isMenu ? menuAuthExtMapper.batchRolePermission(rids) : busiAuthExtMapper.batchRolePermission(rids, flag);
        }
        permissionOrigins = CollectionUtil.addAllIfNotContains(permissionOrigins, cachePermissionOrigins);
        if (CollectionUtil.isNotEmpty(permissionOrigins)) {
            Map<Long, List<UserRole>> roleMap = userRoles.stream().collect(Collectors.groupingBy(UserRole::getId));
            permissionOrigins.forEach(permissionOrigin -> {
                Long rid = permissionOrigin.getId();
                List<UserRole> roles = roleMap.get(rid);
                permissionOrigin.setName(roles.get(0).getName());
                permissionOrigin.setPermissions(authWeightService.filterValid(permissionOrigin.getPermissions()));
                CacheUtils.put(cacheName, rid.toString() + keySuffix, permissionOrigin.getPermissions());
            });
        }
        return permissionOrigins;
    }

    private void initWeightMap() {
        if (CollectionUtil.isEmpty(weightMap)) {
            weightMap.put(0, ListUtil.toList(1, 9));//menu
            weightMap.put(1, ListUtil.toList(1, 4, 7, 9));//panel
            weightMap.put(2, ListUtil.toList(1, 4, 7, 9));//screen
            weightMap.put(3, ListUtil.toList(1, 7, 9));//dataset
            weightMap.put(4, ListUtil.toList(1, 7, 9));//datasource
        }
    }

}
