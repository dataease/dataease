package io.datains.controller.panel.server;

import io.datains.base.domain.PanelShare;
import io.datains.controller.panel.api.ShareApi;
import io.datains.controller.request.panel.PanelShareFineDto;
import io.datains.controller.request.panel.PanelShareRemoveRequest;
import io.datains.controller.request.panel.PanelShareSearchRequest;
import io.datains.controller.sys.base.BaseGridRequest;
import io.datains.dto.panel.PanelShareDto;
import io.datains.dto.panel.PanelShareOutDTO;
import io.datains.dto.panel.PanelSharePo;
import io.datains.service.panel.ShareService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
public class ShareServer implements ShareApi {

    @Resource
    private ShareService shareService;

    @Override
    public List<PanelShareDto> treeList(@RequestBody BaseGridRequest request) {
        return shareService.queryTree(request);
    }

    @Override
    public List<PanelSharePo> shareOut() {
        return shareService.queryShareOut();
    }

    @Override
    public List<PanelShare> queryWithResourceId(@RequestBody PanelShareSearchRequest request) {
        return shareService.queryWithResource(request);
    }

    @Override
    public List<PanelShareOutDTO> queryTargets(@PathVariable("panelId") String panelId) {
        return shareService.queryTargets(panelId);
    }

    @Override
    public void fineSave(@RequestBody PanelShareFineDto panelShareFineDto) {
        shareService.fineSave(panelShareFineDto);
    }

    @Override
    public void removeShares(@RequestBody PanelShareRemoveRequest request) {
        shareService.removeShares(request);
    }
}
