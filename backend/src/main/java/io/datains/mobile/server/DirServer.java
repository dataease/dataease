package io.datains.mobile.server;

import io.datains.mobile.api.DirApi;
import io.datains.mobile.dto.DirItemDTO;
import io.datains.mobile.dto.DirRequest;
import io.datains.mobile.service.DirService;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
public class DirServer implements DirApi {

    @Resource
    private DirService dirService;

    @Override
    public List<DirItemDTO> query(DirRequest request) {
        return dirService.query(request);
    }
}
