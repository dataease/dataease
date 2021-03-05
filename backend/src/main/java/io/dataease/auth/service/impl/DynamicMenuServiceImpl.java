package io.dataease.auth.service.impl;

import io.dataease.auth.api.dto.DynamicMenuDto;
import io.dataease.auth.api.dto.MenuMeta;
import io.dataease.auth.service.DynamicMenuService;
import io.dataease.base.domain.SysMenu;
import io.dataease.base.domain.SysMenuExample;
import io.dataease.base.mapper.SysMenuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DynamicMenuServiceImpl implements DynamicMenuService {

    @Autowired(required = false)
    private SysMenuMapper sysMenuMapper;

    @Override
    public List<DynamicMenuDto> load(String userId) {
        SysMenuExample sysMenuExample = new SysMenuExample();
        sysMenuExample.createCriteria().andTypeLessThanOrEqualTo(1);
        sysMenuExample.setOrderByClause(" menu_sort ");
        List<SysMenu> sysMenus = sysMenuMapper.selectByExample(sysMenuExample);
        List<DynamicMenuDto> dynamicMenuDtos = sysMenus.stream().map(this::convert).collect(Collectors.toList());
        List<DynamicMenuDto> result = buildTree(dynamicMenuDtos);
        return result;
    }

    private DynamicMenuDto convert(SysMenu sysMenu){
        DynamicMenuDto dynamicMenuDto = new DynamicMenuDto();
        dynamicMenuDto.setId(sysMenu.getMenuId());
        dynamicMenuDto.setPid(sysMenu.getPid());
        dynamicMenuDto.setName(sysMenu.getName());
        dynamicMenuDto.setPath(sysMenu.getPath());
        dynamicMenuDto.setRedirect(null);
        dynamicMenuDto.setComponent(sysMenu.getComponent());
        MenuMeta menuMeta = new MenuMeta();
        menuMeta.setTitle(sysMenu.getTitle());
        menuMeta.setIcon(sysMenu.getIcon());
        dynamicMenuDto.setMeta(menuMeta);
        dynamicMenuDto.setPermission(sysMenu.getPermission());
        return dynamicMenuDto;
    }

    private List<DynamicMenuDto> buildTree(List<DynamicMenuDto> lists){
        List<DynamicMenuDto> rootNodes = new ArrayList<>();
        lists.forEach(node -> {
            if (isParent(node.getPid())) {
                rootNodes.add(node);
            }
            lists.forEach(tNode -> {
                if (tNode.getPid() == node.getId()) {
                    if (node.getChildren() == null) {
                        node.setChildren(new ArrayList<DynamicMenuDto>());
                        node.setRedirect(node.getPath()+"/"+tNode.getPath());//第一个子节点的path
                    }
                    node.getChildren().add(tNode);
                }
            });
        });
        return rootNodes;

    }

    private Boolean isParent(Long pid){
        return null == pid || pid==0L;
    }
}
