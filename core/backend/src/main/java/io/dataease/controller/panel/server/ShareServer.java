package io.dataease.controller.panel.server;

import io.dataease.plugins.common.base.domain.PanelShare;
import io.dataease.controller.panel.api.ShareApi;
import io.dataease.controller.request.panel.PanelShareFineDto;
import io.dataease.controller.request.panel.PanelShareRemoveRequest;
import io.dataease.controller.request.panel.PanelShareSearchRequest;
import io.dataease.controller.sys.base.BaseGridRequest;
import io.dataease.dto.panel.PanelShareDto;
import io.dataease.dto.panel.PanelShareOutDTO;
import io.dataease.dto.panel.PanelSharePo;
import io.dataease.service.panel.ShareService;
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

    @Override
    public void removePanelShares(String panelId) {
        shareService.removeSharesyPanel(panelId);
    }
}
