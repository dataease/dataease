package io.dataease.menu.server;

import io.dataease.api.menu.MenuApi;
import io.dataease.api.menu.vo.MenuVO;
import io.dataease.i18n.I18n;
import io.dataease.menu.manage.MenuManage;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/menu")
public class MenuServer implements MenuApi {

    @Resource
    private MenuManage menuManage;

    @I18n
    @Override
    public List<MenuVO> query() {
        return menuManage.query();
    }
}
