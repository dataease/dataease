package io.dataease.api.menu;

import io.dataease.api.menu.vo.MenuVO;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;


public interface MenuApi {

    @GetMapping("/query")
    List<MenuVO> query();
}
