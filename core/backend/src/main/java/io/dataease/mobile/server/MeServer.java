package io.dataease.mobile.server;


import io.dataease.mobile.api.MeApi;
import io.dataease.mobile.dto.MeItemDTO;
import io.dataease.mobile.service.MeService;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;

@RestController
public class MeServer implements MeApi {

    @Resource
    private MeService meService;
    @Override
    public MeItemDTO query() {
        return meService.personInfo();
    }
}
