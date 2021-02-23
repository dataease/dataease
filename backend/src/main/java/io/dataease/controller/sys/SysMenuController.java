package io.dataease.controller.sys;


import io.dataease.base.domain.SysMenu;
import io.dataease.commons.utils.BeanUtils;

import io.dataease.controller.sys.request.MenuCreateRequest;
import io.dataease.controller.sys.request.MenuDeleteRequest;
import io.dataease.controller.sys.response.MenuNodeResponse;
import io.dataease.service.sys.MenuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Api(tags = "系统：菜单管理")
@RequestMapping("/api/menu")
public class SysMenuController {

    @Resource
    private MenuService menuService;

    @ApiOperation("查询跟节点菜单")
    @PostMapping("/childNodes/{pid}")
    public List<MenuNodeResponse> childNodes(@PathVariable("pid") Long pid){
        List<SysMenu> nodes = menuService.nodesByPid(pid);
        List<MenuNodeResponse> nodeResponses = nodes.stream().map(node -> {
            MenuNodeResponse menuNodeResponse = BeanUtils.copyBean(new MenuNodeResponse(), node);
            menuNodeResponse.setHasChildren(node.getSubCount() > 0);
            menuNodeResponse.setTop(node.getPid() == menuService.MENU_ROOT_PID);
            return menuNodeResponse;
        }).collect(Collectors.toList());
        return nodeResponses;
    }

    @ApiOperation("新增菜单")
    @PostMapping("/create")
    public void create(@RequestBody MenuCreateRequest request){
        menuService.add(request);
    }

    @ApiOperation("删除菜单")
    @PostMapping("/delete")
    public void delete(@RequestBody MenuDeleteRequest request){
        menuService.delete(request);
    }
    @ApiOperation("更新菜单")
    @PostMapping("/update")
    public void update(@RequestBody MenuCreateRequest menu){
        menuService.update(menu);
    }


}
