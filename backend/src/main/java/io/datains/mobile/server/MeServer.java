package io.datains.mobile.server;


import io.datains.mobile.api.MeApi;
import io.datains.mobile.dto.MeItemDTO;
import io.datains.mobile.service.MeService;
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
