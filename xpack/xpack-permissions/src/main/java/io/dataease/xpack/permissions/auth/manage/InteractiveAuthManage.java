package io.dataease.xpack.permissions.auth.manage;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.permissions.auth.vo.BusiPerVO;
import io.dataease.api.permissions.auth.vo.PermissionItem;
import io.dataease.auth.bo.TokenUserBO;
import io.dataease.constant.BusiResourceEnum;
import io.dataease.exception.DEException;
import io.dataease.license.utils.LicenseUtil;
import io.dataease.utils.AuthUtils;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.CacheUtils;
import io.dataease.utils.TreeUtils;
import io.dataease.xpack.permissions.auth.dao.ext.entity.BusiPerPO;
import io.dataease.xpack.permissions.auth.dao.ext.entity.BusiResourcePO;
import io.dataease.xpack.permissions.auth.dao.ext.mapper.InteractiveBusiAuthExtMapper;
import io.dataease.xpack.permissions.user.entity.UserRole;
import io.dataease.xpack.permissions.user.manage.RoleManage;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Component
public class InteractiveAuthManage {

    private static final List<Long> XPACKMENUIDS = new ArrayList<>();


    @Resource
    private RoleManage roleManage;

    @Resource
    private BusiAuthManage busiAuthManage;

    @Resource
    private OrgResourceManage orgResourceManage;

    @Resource
    private MenuAuthManage menuAuthManage;

    @Resource
    private InteractiveBusiAuthExtMapper interactiveBusiAuthExtMapper;

    @Resource
    private BusiRootAuthManage busiRootAuthManage;

    public List<Long> menuIds() {
        TokenUserBO user = AuthUtils.getUser();
        Long uid = user.getUserId();
        if (AuthUtils.isSysAdmin(uid)) {
            return xpackFilter(orgResourceManage.menuIds());
        }
        List<UserRole> userRoles = roleManage.userRole(uid, user.getDefaultOid());
        if (isRootAdmin(userRoles)) {
            return xpackFilter(orgResourceManage.menuIds());
        }
        List<Long> rids = userRoles.stream().filter(item -> !item.isRoot()).map(UserRole::getId).toList();
        if (CollectionUtil.isNotEmpty(rids)) {
            List<PermissionItem> permissionItems = menuAuthManage.permissionItems(rids);
            return xpackFilter(permissionItems.stream().map(PermissionItem::getId).toList());
        }
        return null;
    }

    private BusiPerPO rootNode(int weight) {
        return new BusiPerPO(0L, "root", false, weight, -1L, 0, 0L);
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
        int resourceEnumFlag = busiResourceEnum.getFlag();
        if (!rootAdmin.get() && CollectionUtil.isNotEmpty(userRoles = roleManage.userRole(uid, oid)))
            userRoles = userRoles.stream().filter(role -> {
                if (role.isRootAdmin()) {
                    rootAdmin.set(true);
                    return false;
                }
                return true;
            }).toList();
        if (rootAdmin.get()) {
            List<BusiResourcePO> pos = busiAuthManage.resourceWithOid(resourceEnumFlag, oid);
            if (CollectionUtil.isNotEmpty(pos)) {
                List<BusiPerPO> perPOS = pos.stream().map(this::convert).collect(Collectors.toList());
                perPOS.add(rootNode(9));
                return TreeUtils.mergeTree(perPOS, BusiPerVO.class, false);
            }
            return null;
        }
        int enumFlag = resourceEnumFlag;

        List<BusiPerPO> pos = new ArrayList<>();
        List<BusiPerPO> userPerPOS = userPos(enumFlag);
        if (CollectionUtil.isNotEmpty(userPerPOS)) {
            pos.addAll(userPerPOS);
        }
        int userRootWeight = busiRootAuthManage.userRootPer(enumFlag, oid, uid);
        int roleRootWeight = 0;
        if (CollectionUtil.isNotEmpty(userRoles)) {
            List<Long> rids = userRoles.stream().map(UserRole::getId).toList();
            roleRootWeight = busiRootAuthManage.roleRootPer(enumFlag, rids);
            List<BusiPerPO> rolePos = rolePos(rids, enumFlag);
            if (CollectionUtil.isNotEmpty(rolePos)) {
                pos.addAll(rolePos);
            }
        }
        pos.add(rootNode(Math.max(userRootWeight, roleRootWeight)));
        pos = pos.stream().distinct().toList();
        List<BusiPerVO> vos = TreeUtils.mergeTree(pos, BusiPerVO.class, false);
        return vos;
    }

    public List<BusiPerPO> rolePos(List<Long> sourceRids, Integer enumFlag) {
        String cacheName = "role_busi_pers_interactive";
        List<BusiPerPO> resultPos = new ArrayList<>();
        List<Long> rids = sourceRids.stream().filter(roleId -> {
            String rid = roleId.toString();
            if (CacheUtils.keyExist(cacheName, rid + enumFlag)) {
                Object o = CacheUtils.get(cacheName, rid + enumFlag);
                resultPos.addAll((List<BusiPerPO>) o);
                return false;
            }
            return true;
        }).toList();

        if (CollectionUtil.isNotEmpty(rids)) {
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("pbr.rt_id", enumFlag);
            queryWrapper.in("pabr.rid", rids);
            List<BusiPerPO> rolePerPOS = null;
            if (CollectionUtil.isNotEmpty(rolePerPOS = interactiveBusiAuthExtMapper.queryWithRid(queryWrapper))) {
                resultPos.addAll(rolePerPOS);
                Map<Long, List<BusiPerPO>> listMap = rolePerPOS.stream().collect(Collectors.groupingBy(BusiPerPO::getTargetId));
                listMap.entrySet().stream().forEach(entry -> {
                    Long rid = entry.getKey();
                    List<BusiPerPO> rpos = entry.getValue();
                    CacheUtils.put(cacheName, rid.toString() + enumFlag, rpos);
                });
            }
        }

        return resultPos;
    }

    public List<BusiPerPO> userPos(Integer enumFlag) {
        String cacheName = "user_busi_pers_interactive";
        TokenUserBO user = AuthUtils.getUser();
        Long uid = user.getUserId();
        Long oid = user.getDefaultOid();
        String key = oid.toString() + uid.toString() + enumFlag.toString();
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("pabu.uid", uid);
        queryWrapper.eq("pbr.org_id", oid);
        queryWrapper.eq("pbr.rt_id", enumFlag);
        if (CacheUtils.keyExist(cacheName, key)) {
            Object o = CacheUtils.get(cacheName, key);
            if (ObjectUtils.isNotEmpty(o)) {
                return (List<BusiPerPO>) o;
            }
            return null;
        }
        List<BusiPerPO> userPerPOS = interactiveBusiAuthExtMapper.queryWithUid(queryWrapper);
        CacheUtils.put(cacheName, key, userPerPOS);

        return userPerPOS;
    }

    private BusiPerPO convert(BusiResourcePO resourcePO) {
        BusiPerPO busiPerPO = BeanUtils.copyBean(new BusiPerPO(), resourcePO);
        busiPerPO.setWeight(9);
        return busiPerPO;
    }

    private List<Long> xpackFilter(List<Long> mids) {
        if (LicenseUtil.licenseValid()) return mids;
        return mids.stream().filter(id -> !XPACKMENUIDS.contains(id)).toList();
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
