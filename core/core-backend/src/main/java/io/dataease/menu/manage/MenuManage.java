package io.dataease.menu.manage;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.menu.vo.MenuMeta;
import io.dataease.api.menu.vo.MenuVO;
import io.dataease.api.permissions.auth.api.InteractiveAuthApi;
import io.dataease.menu.bo.MenuTreeNode;
import io.dataease.menu.dao.auto.entity.CoreMenu;
import io.dataease.menu.dao.auto.mapper.CoreMenuMapper;
import io.dataease.utils.BeanUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class MenuManage {

    private static final String I18N_PREFIX = "i18n_menu.";

    private final static int ROOTID = 0;

    @Resource
    private CoreMenuMapper coreMenuMapper;

    @Autowired(required = false)
    private InteractiveAuthApi interactiveAuthApi;

    // @Cacheable(value = "menu", key = "#root.methodName")
    public List<MenuVO> query() {
        QueryWrapper<CoreMenu> wrapper = new QueryWrapper<>();
        List<CoreMenu> coreMenus = coreMenuMapper.selectList(wrapper);
        coreMenus = filterAuth(coreMenus);
        List<MenuTreeNode> menuTreeNodes = coreMenus.stream().map(menu -> BeanUtils.copyBean(new MenuTreeNode(), menu)).toList();
        List<MenuTreeNode> treeNodes = buildPOTree(menuTreeNodes);
        List<MenuVO> menuVOS = convertTree(treeNodes);
        return menuVOS;
    }
    private List<CoreMenu> filterAuth(List<CoreMenu> list) {
        if (ObjectUtils.isEmpty(interactiveAuthApi)) return list;
        List<Long> menuIds = interactiveAuthApi.menuIds();
        return list.stream().filter(menu -> !menu.getAuth() || menuIds.contains(menu.getId())).toList();
    }

    private List<MenuTreeNode> buildPOTree(List<MenuTreeNode> coreMenus) {
        List<MenuTreeNode> result = new ArrayList<>();
        Map<Long, List<MenuTreeNode>> childMap = coreMenus.stream().collect(Collectors.groupingBy(CoreMenu::getPid));
        coreMenus.forEach(po -> {
            po.setChildren(childMap.get(po.getId()));
            if (po.getPid() == ROOTID) {
                result.add(po);
            }
        });
        return result;
    }

    private List<MenuVO> convertTree(List<MenuTreeNode> roots) {
        List<MenuVO> result = new ArrayList<>();
        for (int i = 0; i < roots.size(); i++) {
            MenuTreeNode menuTreeNode = roots.get(i);
            MenuVO vo = convert(menuTreeNode);
            List<MenuTreeNode> children = null;
            if (CollectionUtil.isNotEmpty(children = menuTreeNode.getChildren())) {
                vo.setChildren(convertTree(children));
            }
            if (CollectionUtil.isNotEmpty(children) || menuTreeNode.getType() != 1) {
                result.add(vo);
            }
        }
        return result;
    }

    private MenuVO convert(CoreMenu coreMenu) {
        if (ROOTID != coreMenu.getPid() && StringUtils.startsWith(coreMenu.getPath(), "/")) {
            coreMenu.setPath(coreMenu.getPath().substring(1));
        }
        MenuVO menuVO = new MenuVO();
        BeanUtils.copyBean(menuVO, coreMenu, "children");
        MenuMeta meta = new MenuMeta();
        meta.setTitle(I18N_PREFIX + coreMenu.getName());
        meta.setIcon(coreMenu.getIcon());
        menuVO.setMeta(meta);

        return menuVO;
    }
}
