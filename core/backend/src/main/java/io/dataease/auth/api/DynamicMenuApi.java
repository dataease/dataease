package io.dataease.auth.api;


import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.auth.api.dto.DynamicMenuDto;
import io.dataease.controller.handler.annotation.I18n;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Api(tags = "登录：动态菜单")
@ApiSupport(order = 20)
@RequestMapping("/api/dynamicMenu")
public interface DynamicMenuApi {

    /**
     * 根据heads中获取的token 获取username 获取对应权限的菜单
     *
     * @return
     */
    @ApiOperation("查询")
    @PostMapping("/menus")
    @I18n
    List<DynamicMenuDto> menus();

}
