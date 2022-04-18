package io.datains.controller.panel.server;

import io.datains.controller.panel.api.StoreApi;
import io.datains.controller.sys.base.BaseGridRequest;
import io.datains.dto.panel.PanelStoreDto;
import io.datains.service.panel.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class StoreServer implements StoreApi {

    @Autowired
    private StoreService storeService;

    @Override
    public void store(String id) {
        storeService.save(id);
    }

    @Override
    public List<PanelStoreDto> list(BaseGridRequest request) {
        return storeService.query(request);
    }

    @Override
    public void remove(String panelId) {
        storeService.removeByPanelId(panelId);
    }

    @Override
    public Boolean hasStar(String id) {
        return storeService.count(id) > 0L;
    }
}
