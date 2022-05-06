package io.dataease.service.sys;

import io.dataease.commons.utils.BeanUtils;
import io.dataease.controller.sys.base.BaseGridRequest;
import io.dataease.controller.sys.request.MenuCreateRequest;
import io.dataease.controller.sys.request.MenuDeleteRequest;
import io.dataease.controller.sys.request.SimpleTreeNode;
import io.dataease.controller.sys.response.MenuNodeResponse;
import io.dataease.controller.sys.response.MenuTreeNode;
import io.dataease.ext.ExtMenuMapper;
import io.dataease.ext.ExtSysMenuMapper;
import io.dataease.plugins.common.base.domain.SysMenu;
import io.dataease.plugins.common.base.domain.SysMenuExample;
import io.dataease.plugins.common.base.mapper.SysMenuMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MenuService {

    private final static Integer DEFAULT_SUBCOUNT = 0;
    public final static Long MENU_ROOT_PID = 0L;

    @Resource
    private SysMenuMapper sysMenuMapper;

    @Resource
    private ExtSysMenuMapper extSysMenuMapper;

    @Resource
    private ExtMenuMapper extMenuMapper;

    public List<SysMenu> nodesByPid(Long pid) {
        SysMenuExample example = new SysMenuExample();
        SysMenuExample.Criteria criteria = example.createCriteria();
        if (ObjectUtils.isEmpty(pid)) {
            criteria.andPidEqualTo(MENU_ROOT_PID);
        } else {
            criteria.andPidEqualTo(pid);
        }
        example.setOrderByClause("menu_sort");
        return sysMenuMapper.selectByExample(example);
    }

    @Transactional
    public boolean add(MenuCreateRequest menuCreateRequest) {
        SysMenu sysMenu = BeanUtils.copyBean(new SysMenu(), menuCreateRequest);
        if (menuCreateRequest.isTop()) {
            sysMenu.setPid(MENU_ROOT_PID);
        }
        long now = System.currentTimeMillis();
        sysMenu.setCreateTime(now);
        sysMenu.setUpdateTime(now);
        sysMenu.setCreateBy(null);
        sysMenu.setUpdateBy(null);
        sysMenu.setSubCount(DEFAULT_SUBCOUNT);
        try {
            int insert = sysMenuMapper.insert(sysMenu);
            Long pid;
            if (!(pid = sysMenu.getPid()).equals(MENU_ROOT_PID)) {
                //这里需要更新上级节点SubCount
                extMenuMapper.incrementalSubcount(pid);
            }
            if (insert == 1) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Transactional
    public int delete(MenuDeleteRequest request) {
        Long pid = request.getPid();
        if (!pid.equals(MENU_ROOT_PID)) {
            extMenuMapper.decreasingSubcount(pid);
        }
        Long menuId = request.getMenuId();

        return sysMenuMapper.deleteByPrimaryKey(menuId);
    }

    @Transactional
    public int update(MenuCreateRequest menuCreateRequest) {
        SysMenu sysMenu = BeanUtils.copyBean(new SysMenu(), menuCreateRequest);
        if (menuCreateRequest.isTop()) {
            sysMenu.setPid(MENU_ROOT_PID);
        }

        sysMenu.setUpdateBy(null);
        sysMenu.setUpdateTime(System.currentTimeMillis());
        Long menuId = sysMenu.getMenuId();
        SysMenu menu_old = sysMenuMapper.selectByPrimaryKey(menuId);
        //如果PID发生了改变
        //判断oldPid是否是跟节点PID ？ nothing : parent.subcount-1
        //判断newPid是否是跟节点PID ？ nothing : parent.subcount+1
        if (!menu_old.getPid().equals(sysMenu.getPid())) {
            Long oldPid = menu_old.getPid();
            if (!oldPid.equals(MENU_ROOT_PID)) {
                extMenuMapper.decreasingSubcount(oldPid);
            }
            if (!sysMenu.getPid().equals(MENU_ROOT_PID)) {
                extMenuMapper.incrementalSubcount(sysMenu.getPid());
            }
        }
        return sysMenuMapper.updateByPrimaryKeySelective(sysMenu);
    }

    public List<MenuNodeResponse> childs(Long pid) {
        Set<SysMenu> childs = getChilds(nodesByPid(pid), new HashSet());
        List<SysMenu> menus = new ArrayList<>(childs);
        return convert(menus);
    }

    public List<MenuTreeNode> searchTree(Long menuId) {
        List<SysMenu> roots = nodesByPid(0L);
        if (menuId.equals(MENU_ROOT_PID)) return roots.stream().map(this::format).collect(Collectors.toList());
        SysMenu sysMenu = sysMenuMapper.selectByPrimaryKey(menuId);
        if (roots.stream().anyMatch(node -> node.getMenuId().equals(menuId)))
            return roots.stream().map(this::format).collect(Collectors.toList());
        SysMenu current = sysMenu;
        MenuTreeNode currentNode = format(sysMenu);
        while (!current.getPid().equals(MENU_ROOT_PID)) {
            SysMenu parent = sysMenuMapper.selectByPrimaryKey(current.getPid()); //pid上有索引 所以效率不会太差
            MenuTreeNode parentNode = format(parent);
            parentNode.setChildren(currentNode.toList());
            current = parent;
            currentNode = parentNode;
        }
        MenuTreeNode targetRootNode = currentNode;
        return roots.stream().map(node -> node.getMenuId().equals(targetRootNode.getId()) ? targetRootNode : format(node)).collect(Collectors.toList());
    }

    private Set<SysMenu> getChilds(List<SysMenu> lists, Set<SysMenu> sets) {
        lists.forEach(menu -> {
            sets.add(menu);
            List<SysMenu> kidMenus = nodesByPid(menu.getMenuId());
            if (CollectionUtils.isNotEmpty(kidMenus)) {
                getChilds(kidMenus, sets);
            }
        });
        return sets;
    }

    private MenuTreeNode format(SysMenu sysMenu) {
        MenuTreeNode menuTreeNode = new MenuTreeNode();
        menuTreeNode.setId(sysMenu.getMenuId());
        menuTreeNode.setLabel(sysMenu.getTitle());
        menuTreeNode.setHasChildren(false);
        Optional.ofNullable(sysMenu.getMenuSort()).ifPresent(num -> menuTreeNode.setHasChildren(num > 0));
        return menuTreeNode;
    }

    public List<MenuNodeResponse> convert(List<SysMenu> menus) {
        return menus.stream().map(node -> {
            MenuNodeResponse menuNodeResponse = BeanUtils.copyBean(new MenuNodeResponse(), node);
            menuNodeResponse.setHasChildren(node.getSubCount() > 0);
            menuNodeResponse.setTop(node.getPid().equals(MENU_ROOT_PID));
            return menuNodeResponse;
        }).collect(Collectors.toList());
    }


    public List<SysMenu> nodesTreeByCondition(BaseGridRequest request) {
        List<SimpleTreeNode> allNodes = allNodes();
        List<SimpleTreeNode> targetNodes = nodeByCondition(request);
        if (org.apache.commons.collections.CollectionUtils.isEmpty(targetNodes)) {
            return new ArrayList<>();
        }
        List<Long> ids = upTree(allNodes, targetNodes);
        SysMenuExample example = new SysMenuExample();
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(ids)) {
            SysMenuExample.Criteria criteria = example.createCriteria();
            criteria.andMenuIdIn(ids);
        }
        return sysMenuMapper.selectByExample(example);
    }

    public List<SimpleTreeNode> allNodes() {
        return extSysMenuMapper.allNodes();
    }

    public List<SimpleTreeNode> nodeByCondition(BaseGridRequest request) {
        return extSysMenuMapper.nodesByExample(request.convertExample());
    }

    /**
     * 找出目标节点所在路径上的所有节点 向上找
     *
     * @param allNodes    所有节点
     * @param targetNodes 目标节点
     * @return
     */
    private List<Long> upTree(List<SimpleTreeNode> allNodes, List<SimpleTreeNode> targetNodes) {
        final Map<Long, SimpleTreeNode> map = allNodes.stream().collect(Collectors.toMap(SimpleTreeNode::getId, node -> node));
        return targetNodes.parallelStream().flatMap(targetNode -> {
            //向上逐级找爹
            List<Long> ids = new ArrayList<>();
            SimpleTreeNode node = targetNode;
            while (node != null) {
                ids.add(node.getId());
                Long pid = node.getPid();
                node = map.get(pid);
            }
            return ids.stream();
        }).distinct().collect(Collectors.toList());
    }


}
