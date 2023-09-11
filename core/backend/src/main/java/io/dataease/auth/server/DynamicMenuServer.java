package io.dataease.auth.server;

import io.dataease.auth.api.DynamicMenuApi;
import io.dataease.auth.api.dto.DynamicMenuDto;
import io.dataease.auth.service.DynamicMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DynamicMenuServer implements DynamicMenuApi {
    @Autowired
    private DynamicMenuService dynamicMenuService;

    @Override
    public List<DynamicMenuDto> menus() {
        return dynamicMenuService.load(null);
    }
}
