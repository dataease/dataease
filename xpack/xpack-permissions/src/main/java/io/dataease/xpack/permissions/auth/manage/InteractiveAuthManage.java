package io.dataease.xpack.permissions.auth.manage;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.permissions.auth.vo.BusiPerVO;
import io.dataease.api.permissions.auth.vo.PermissionItem;
import io.dataease.api.permissions.auth.vo.PermissionOrigin;
import io.dataease.auth.bo.TokenUserBO;
import io.dataease.constant.BusiResourceEnum;
import io.dataease.exception.DEException;
import io.dataease.license.utils.LicenseUtil;
import io.dataease.utils.AuthUtils;
import io.dataease.utils.CacheUtils;
import io.dataease.utils.TreeUtils;
import io.dataease.xpack.permissions.auth.dao.ext.entity.BusiPerPO;
import io.dataease.xpack.permissions.auth.dao.ext.entity.BusiResourcePO;
import io.dataease.xpack.permissions.auth.dao.ext.mapper.InteractiveBusiAuthExtMapper;
import io.dataease.xpack.permissions.auth.dao.ext.mapper.MenuAuthExtMapper;
import io.dataease.xpack.permissions.user.entity.UserRole;
import io.dataease.xpack.permissions.user.manage.RoleManage;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Component
public class InteractiveAuthManage {

    private static final List<Long> XPACKMENUIDS = new ArrayList<>();
    @Resource
    private RoleAuthManage roleAuthManage;

    @Resource
    private RoleManage roleManage;

    @Resource
    private BusiAuthManage busiAuthManage;

    @Resource
    private UserAuthManage userAuthManage;

    @Resource
    private MenuAuthExtMapper menuAuthExtMapper;


    @Resource
    private InteractiveBusiAuthExtMapper interactiveBusiAuthExtMapper;

    public List<Long> menuIds() {
        TokenUserBO user = AuthUtils.getUser();
        Long uid = user.getUserId();
        if (AuthUtils.isSysAdmin(uid)) {
            return xpackFilter(roleAuthManage.queryMenuIds());
        }
        List<UserRole> userRoles = roleManage.userRole(uid, user.getDefaultOid());
        if (isRootAdmin(userRoles)) {
            return xpackFilter(roleAuthManage.queryMenuIds());
        }
        List<Long> rids = userRoles.stream().filter(item -> !item.isRoot()).map(UserRole::getId).toList();
        if (CollectionUtil.isNotEmpty(rids)) {
            return xpackFilter(menuAuthExtMapper.interactiveMenuIds(rids));
        }
        return null;
    }

    public List<Long> resourceIds(String flag) {
        BusiResourceEnum busiResourceEnum = BusiResourceEnum.valueOf(flag.toUpperCase());
        if (ObjectUtils.isEmpty(busiResourceEnum)) {
            DEException.throwException("invalid flag value");
        }
        int enumFlag = busiResourceEnum.getFlag();
        TokenUserBO user = AuthUtils.getUser();
        Long uid = user.getUserId();
        Long oid = user.getDefaultOid();
        if (AuthUtils.isSysAdmin(uid)) {
            return busiAuthManage.resourceIdsByRt(enumFlag, oid);
        }
        List<UserRole> userRoles = roleManage.userRole(uid, user.getDefaultOid());
        if (isRootAdmin(userRoles)) {
            return busiAuthManage.resourceIdsByRt(enumFlag, oid);
        }
        Set<Long> set = null;
        List<PermissionItem> permissionItems = userAuthManage.permissionItems(uid, oid, enumFlag);
        if (CollectionUtil.isNotEmpty(permissionItems)) {
            set = permissionItems.stream().map(PermissionItem::getId).collect(Collectors.toSet());
        }
        if (CollectionUtil.isNotEmpty(userRoles)) {
            List<PermissionOrigin> permissionOrigins = roleAuthManage.roleOrigin(userRoles, enumFlag);
            if (CollectionUtil.isNotEmpty(permissionOrigins)) {
                Set<Long> itemSet = permissionOrigins.stream().flatMap(origin -> origin.getPermissions().stream().map(PermissionItem::getId)).collect(Collectors.toSet());
                set.addAll(itemSet);
            }
        }
        if (CollectionUtil.isNotEmpty(set)) {
            return new ArrayList<>(set);
        }
        return null;
    }

    public List<BusiPerVO> resource(String flag) {
        BusiResourceEnum busiResourceEnum = BusiResourceEnum.valueOf(flag.toUpperCase());
        if (ObjectUtils.isEmpty(busiResourceEnum)) {
            DEException.throwException("invalid flag value");
        }
        List<UserRole> userRoles = null;
        TokenUserBO user = AuthUtils.getUser();
        Long uid = user.getUserId();
        Long oid = user.getDefaultOid();
        AtomicBoolean rootAdmin = new AtomicBoolean(AuthUtils.isSysAdmin(uid));
        if (!rootAdmin.get() && CollectionUtil.isNotEmpty(userRoles = roleManage.userRole(uid, oid)))
            userRoles = userRoles.stream().filter(role -> {
                if (role.isRootAdmin()) {
                    rootAdmin.set(true);
                    return false;
                }
                return true;
            }).toList();
        if (rootAdmin.get()) {
            List<BusiResourcePO> pos = busiAuthManage.resourceWithOid(busiResourceEnum);
            // 加上weight = 9 转换成树
            return null;
        }
        int enumFlag = busiResourceEnum.getFlag();
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("pabu.uid", uid);
        queryWrapper.eq("pbr.org_id", oid);
        queryWrapper.eq("pbr.rt_id", enumFlag);
        List<BusiPerPO> pos = new ArrayList<>();
        List<BusiPerPO> userPerPOS = null;
        if (CollectionUtil.isNotEmpty(userPerPOS = interactiveBusiAuthExtMapper.queryWithUid(queryWrapper))) {
            pos.addAll(userPerPOS);
        }
        List<BusiPerPO> cacheRolePerPOS = new ArrayList<>();
        queryWrapper.clear();
        String cacheName = "role_busi_pers_interactive";
        List<Long> rids = userRoles.stream().filter(role -> {
            String rid = role.getId().toString();
            if (CacheUtils.keyExist(cacheName, rid + enumFlag)) {
                Object o = CacheUtils.get(cacheName, rid + enumFlag);
                cacheRolePerPOS.addAll((List<BusiPerPO>) o);
                return false;
            }
            return true;
        }).map(UserRole::getId).toList();
        if (CollectionUtil.isNotEmpty(cacheRolePerPOS)) {
            pos.addAll(cacheRolePerPOS);
        }
        if (CollectionUtil.isNotEmpty(rids)) {
            queryWrapper.eq("pbr.rt_id", enumFlag);
            queryWrapper.in("pabr.rt_id", rids);
            List<BusiPerPO> rolePerPOS = null;
            if (CollectionUtil.isNotEmpty(rolePerPOS = interactiveBusiAuthExtMapper.queryWithUid(queryWrapper))) {
                pos.addAll(cacheRolePerPOS);
            }
            Map<Long, List<BusiPerPO>> listMap = rolePerPOS.stream().collect(Collectors.groupingBy(BusiPerPO::getTargetId));
            listMap.entrySet().stream().forEach(entry -> {
                Long rid = entry.getKey();
                List<BusiPerPO> rpos = entry.getValue();
                CacheUtils.put(cacheName, rid.toString() + enumFlag, rpos);
            });
        }
        pos = pos.stream().distinct().toList();
        List<BusiPerVO> vos = TreeUtils.mergeTree(pos, 0L, BusiPerVO.class, false);
        return vos;
    }

    private List<Long> xpackFilter(List<Long> mids) {
        if (LicenseUtil.licenseValid()) return mids;
        return mids.stream().filter(id -> !XPACKMENUIDS.contains(id)).toList();
    }


    private void convert() {

    }


    private boolean isRootAdmin(List<UserRole> roles) {
        return roles.stream().anyMatch(UserRole::isRootAdmin);
    }


    @PostConstruct
    public void init() {
        XPACKMENUIDS.add(8L);
        XPACKMENUIDS.add(9L);
        XPACKMENUIDS.add(10L);
    }

}
