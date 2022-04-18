package io.datains.mobile.server;

import io.datains.commons.utils.Pager;
import io.datains.mobile.api.HomeApi;
import io.datains.mobile.dto.HomeItemDTO;
import io.datains.mobile.dto.HomeItemShareDTO;
import io.datains.mobile.dto.HomeRequest;
import io.datains.mobile.service.HomeService;
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
