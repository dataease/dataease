package io.dataease.mobile.server;

import io.dataease.mobile.api.DirApi;
import io.dataease.mobile.dto.DirItemDTO;
import io.dataease.mobile.dto.DirRequest;
import io.dataease.mobile.service.DirService;
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
