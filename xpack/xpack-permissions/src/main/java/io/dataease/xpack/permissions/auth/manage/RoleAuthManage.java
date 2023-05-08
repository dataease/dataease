package io.dataease.xpack.permissions.auth.manage;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.dataease.utils.IDUtils;
import io.dataease.xpack.permissions.auth.dao.auto.entity.PerAuthBusiRole;
import io.dataease.xpack.permissions.auth.dao.auto.entity.PerAuthMenu;
import io.dataease.xpack.permissions.auth.dao.auto.mapper.PerAuthBusiRoleMapper;
import io.dataease.xpack.permissions.auth.dao.ext.entity.ResourcePO;
import io.dataease.xpack.permissions.auth.dao.ext.mapper.BusiAuthExtMapper;
import io.dataease.xpack.permissions.auth.dao.ext.mapper.MenuAuthExtMapper;
import io.dataease.xpack.permissions.user.dao.auto.entity.PerRole;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
        List<ResourcePO> pos = busiAuthExtMapper.resourceIds();
        Map<Integer, List<ResourcePO>> listMap = pos.stream().collect(Collectors.groupingBy(ResourcePO::getType));
        List<PerAuthMenu> menus = syncItem(0, menuIds, perRole.getId(), readonly);
        menuAuthManage.saveBatch(menus);
        List<Integer> busiTypes = List.of(1, 2, 3, 4);
        List<PerAuthBusiRole> perAuthBusiRoles = busiTypes.stream().flatMap(type -> syncBusiItem(type, listMap.get(type).stream().map(item -> item.getId()).toList(), perRole.getId(), readonly)).toList();
        saveBatch(perAuthBusiRoles, 1000);
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
