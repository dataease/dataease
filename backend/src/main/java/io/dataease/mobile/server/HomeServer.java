package io.dataease.mobile.server;

import io.dataease.mobile.api.HomeApi;
import io.dataease.mobile.dto.HomeItemDTO;
import io.dataease.mobile.service.HomeService;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import java.util.List;

@RestController
public class HomeServer implements HomeApi {

    @Resource
    private HomeService homeService;

    @Override
    public List<HomeItemDTO> query(Integer type) {
        return homeService.query(type);
    }

    @Override
    public Object detail(String id) {
        return null;
    }
}
