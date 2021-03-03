package io.dataease.auth.api;


import io.dataease.auth.api.dto.DynamicMenuDto;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;

@Api(tags = "权限：动态菜单")
@RequestMapping("/api/dynamicMenu")
public interface DynamicMenuApi {

    /**
     * 根据heads中获取的token 获取username 获取对应权限的菜单
     * @return
     */
    @PostMapping("/menus")
    List<DynamicMenuDto> menus();

}
