package io.dataease.api.menu;

import io.dataease.api.menu.vo.MenuVO;
import io.dataease.auth.DeApiPath;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@DeApiPath("/menu")
public interface MenuApi {

    @GetMapping("/query")
    List<MenuVO> query();
}
