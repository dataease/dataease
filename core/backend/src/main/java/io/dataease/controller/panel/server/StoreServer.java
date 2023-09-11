package io.dataease.controller.panel.server;

import io.dataease.controller.panel.api.StoreApi;
import io.dataease.dto.panel.PanelStoreDto;
import io.dataease.service.panel.StoreService;
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
    public List<PanelStoreDto> list() {
        return storeService.query();
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
