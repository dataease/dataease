package io.dataease.controller.panel.server;

import io.dataease.base.domain.PanelShare;
import io.dataease.controller.panel.api.ShareApi;
import io.dataease.controller.request.panel.PanelShareRequest;
import io.dataease.controller.sys.base.BaseGridRequest;
import io.dataease.dto.panel.PanelShareDto;
import io.dataease.service.panel.ShareService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
public class ShareServer implements ShareApi {

    @Resource
    private ShareService shareService;
    @Override
    public void share(@RequestBody PanelShareRequest request) {
        shareService.save(request);
    }

    @Override
    public List<PanelShareDto> treeList(@RequestBody BaseGridRequest request) {
        return shareService.queryTree(request);
    }


    @Override
    public List<PanelShare> queryWithResourceId(@RequestBody BaseGridRequest request) {
        return shareService.queryWithResource(request);
    }
}
