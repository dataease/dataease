package io.dataease.service.sys;

import io.dataease.base.domain.SysMenu;
import io.dataease.base.domain.SysMenuExample;
import io.dataease.base.mapper.SysMenuMapper;
import io.dataease.base.mapper.ext.ExtMenuMapper;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.controller.sys.request.MenuCreateRequest;
import io.dataease.controller.sys.request.MenuDeleteRequest;
import io.dataease.controller.sys.response.MenuNodeResponse;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MenuService {



    private final static Integer DEFAULT_SUBCOUNT = 0;
    public final static Long MENU_ROOT_PID = 0L;

    @Resource
    private SysMenuMapper sysMenuMapper;


    @Resource
    private ExtMenuMapper extMenuMapper;

    public List<SysMenu> nodesByPid(Long pid){
        SysMenuExample example = new SysMenuExample();
        SysMenuExample.Criteria criteria = example.createCriteria();
        if (ObjectUtils.isEmpty(pid)){
            criteria.andPidEqualTo(MENU_ROOT_PID);
        }else {
            criteria.andPidEqualTo(pid);
        }
        example.setOrderByClause("menu_sort");
        List<SysMenu> sysMenus = sysMenuMapper.selectByExample(example);
        return sysMenus;
    }

    @Transactional
    public boolean add(MenuCreateRequest menuCreateRequest){
        SysMenu sysMenu = BeanUtils.copyBean(new SysMenu(), menuCreateRequest);
        if (menuCreateRequest.isTop()){
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
            Long pid = null;
            if ((pid = sysMenu.getPid()) != MENU_ROOT_PID ){
                //这里需要更新上级节点SubCount
                extMenuMapper.incrementalSubcount(pid);
            }
            if (insert == 1){
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    @Transactional
    public int delete(MenuDeleteRequest request){
        Long pid = request.getPid();
        if (pid != MENU_ROOT_PID){
            extMenuMapper.decreasingSubcount(pid);
        }
        Long menuId = request.getMenuId();

        return sysMenuMapper.deleteByPrimaryKey(menuId);
    }



    @Transactional
    public int update(MenuCreateRequest menuCreateRequest){
        SysMenu sysMenu = BeanUtils.copyBean(new SysMenu(), menuCreateRequest);
        if (menuCreateRequest.isTop()){
            sysMenu.setPid(MENU_ROOT_PID);
        }

        sysMenu.setUpdateBy(null);
        sysMenu.setUpdateTime(System.currentTimeMillis());
        Long menuId = sysMenu.getMenuId();
        SysMenu menu_old = sysMenuMapper.selectByPrimaryKey(menuId);
        //如果PID发生了改变
        //判断oldPid是否是跟节点PID ？ nothing : parent.subcount-1
        //判断newPid是否是跟节点PID ？ nothing : parent.subcount+1
        if (menu_old.getPid() != sysMenu.getPid()){
            Long oldPid = menu_old.getPid();
            if (oldPid != MENU_ROOT_PID){
                extMenuMapper.decreasingSubcount(oldPid);
            }
            if (sysMenu.getPid() != MENU_ROOT_PID){
                extMenuMapper.incrementalSubcount(sysMenu.getPid());
            }
        }
        return sysMenuMapper.updateByPrimaryKeySelective(sysMenu);
    }

    public List<MenuNodeResponse> childs(Long pid){
        Set<SysMenu> childs = getChilds(nodesByPid(pid), new HashSet());
        List<SysMenu> menus = childs.stream().collect(Collectors.toList());
        List<MenuNodeResponse> responses = convert(menus);
        return responses;
    }

    private Set<SysMenu> getChilds(List<SysMenu> lists, Set<SysMenu> sets){
        lists.forEach(menu -> {
            sets.add(menu);
            List<SysMenu> kidMenus = nodesByPid(menu.getMenuId());
            if (CollectionUtils.isNotEmpty(kidMenus)){
                getChilds(kidMenus, sets);
            }
        });
        return sets;
    }

    public List<MenuNodeResponse> convert(List<SysMenu> menus){
        return menus.stream().map(node -> {
            MenuNodeResponse menuNodeResponse = BeanUtils.copyBean(new MenuNodeResponse(), node);
            menuNodeResponse.setHasChildren(node.getSubCount() > 0);
            menuNodeResponse.setTop(node.getPid() == MENU_ROOT_PID);
            return menuNodeResponse;
        }).collect(Collectors.toList());
    }


}
