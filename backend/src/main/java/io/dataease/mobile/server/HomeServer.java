package io.dataease.mobile.server;

import io.dataease.commons.utils.Pager;
import io.dataease.mobile.api.HomeApi;
import io.dataease.mobile.dto.HomeItemDTO;
import io.dataease.mobile.dto.HomeItemShareDTO;
import io.dataease.mobile.dto.HomeRequest;
import io.dataease.mobile.service.HomeService;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import java.util.List;

@RestController
public class HomeServer implements HomeApi {

    @Resource
    private HomeService homeService;

    @Override
    public Pager<List<HomeItemDTO>> query(HomeRequest request) {
        return homeService.query(request);
    }

    @Override
    public Object detail(String id) {
        return null;
    }

    @Override
    public Pager<List<HomeItemShareDTO>> queryShares(HomeRequest request) {

        return homeService.queryShares(request);
    }

}
