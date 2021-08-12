package io.dataease.plugins.server;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.dataease.commons.utils.PageUtils;
import io.dataease.commons.utils.Pager;
import io.dataease.plugins.common.entity.XpackGridRequest;
import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.xpack.role.dto.response.XpackRoleDto;
import io.dataease.plugins.xpack.role.dto.response.XpackRoleItemDto;
import io.dataease.plugins.xpack.role.service.RoleXpackService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
@Api(tags = "xpack：角色管理")
@RequestMapping("/plugin/role")
@RestController
public class XRoleServer {


    @ApiOperation("新增角色")
    @PostMapping("/create")
    public void create(@RequestBody XpackRoleDto role){
        RoleXpackService roleXpackService = SpringContextUtil.getBean(RoleXpackService.class);
        roleXpackService.save(role);
    }


    @ApiOperation("删除角色")
    @PostMapping("/delete/{roleId}")
    public void delete(@PathVariable("roleId") Long roleId){
        RoleXpackService roleXpackService = SpringContextUtil.getBean(RoleXpackService.class);
        roleXpackService.delete(roleId);
    }


    @ApiOperation("更新角色")
    @PostMapping("/update")
    public void update(@RequestBody XpackRoleDto role){
        RoleXpackService roleXpackService = SpringContextUtil.getBean(RoleXpackService.class);
        roleXpackService.update(role);
    }

    @ApiOperation("分页查询")
    @PostMapping("/roleGrid/{goPage}/{pageSize}")
    public Pager<List<XpackRoleDto>> roleGrid(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody XpackGridRequest request) {
        RoleXpackService roleXpackService = SpringContextUtil.getBean(RoleXpackService.class);
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        Pager<List<XpackRoleDto>> listPager = PageUtils.setPageInfo(page, roleXpackService.query(request));
        return listPager;
    }

    @ApiIgnore
    @PostMapping("/all")
    public List<XpackRoleItemDto> all() {
        RoleXpackService roleXpackService = SpringContextUtil.getBean(RoleXpackService.class);
        return roleXpackService.allRoles();
    }
}
