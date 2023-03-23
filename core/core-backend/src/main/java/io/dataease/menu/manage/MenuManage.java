package io.dataease.menu.manage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.menu.vo.MenuMeta;
import io.dataease.api.menu.vo.MenuVO;
import io.dataease.menu.bo.MenuTreeNode;
import io.dataease.menu.dao.auto.entity.CoreMenu;
import io.dataease.menu.dao.auto.mapper.CoreMenuMapper;
import io.dataease.utils.BeanUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class MenuManage {

    private final static int ROOTID = 0;

    @Resource
    private CoreMenuMapper coreMenuMapper;

    public List<MenuVO> query() {
        QueryWrapper<CoreMenu> wrapper = new QueryWrapper<>();
        List<CoreMenu> coreMenus = coreMenuMapper.selectList(wrapper);
        List<MenuTreeNode> menuTreeNodes = coreMenus.stream().map(menu -> BeanUtils.copyBean(new MenuTreeNode(), menu)).toList();
        List<MenuTreeNode> treeNodes = buildPOTree(menuTreeNodes);
        List<MenuVO> menuVOS = convertTree(treeNodes);
        return menuVOS;
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
            result.add(vo);
            List<MenuTreeNode> children = null;
            if (!CollectionUtils.isEmpty(children = menuTreeNode.getChildren())) {
                vo.setChildren(convertTree(children));
            }
        }
        return result;
    }

    private MenuVO convert(CoreMenu coreMenu) {
        MenuVO menuVO = new MenuVO();
        BeanUtils.copyBean(menuVO, coreMenu, "children");
        MenuMeta meta = new MenuMeta();
        meta.setTitle(coreMenu.getName());
        meta.setIcon(coreMenu.getIcon());
        menuVO.setMeta(meta);
        return menuVO;
    }
}
