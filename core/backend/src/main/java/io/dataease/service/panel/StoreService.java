package io.dataease.service.panel;

import io.dataease.commons.utils.AuthUtils;
import io.dataease.dto.panel.PanelStoreDto;
import io.dataease.ext.ExtPanelStoreMapper;
import io.dataease.plugins.common.base.domain.PanelStore;
import io.dataease.plugins.common.base.domain.PanelStoreExample;
import io.dataease.plugins.common.base.mapper.PanelStoreMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class StoreService {

    @Resource
    private PanelStoreMapper panelStoreMapper;

    @Resource
    private ExtPanelStoreMapper extPanelStoreMapper;

    public void save(String panelGroupId) {
        Long userId = AuthUtils.getUser().getUserId();
        PanelStore panelStore = new PanelStore();
        panelStore.setCreateTime(System.currentTimeMillis());
        panelStore.setPanelGroupId(panelGroupId);
        panelStore.setUserId(userId);
        panelStoreMapper.insert(panelStore);
    }

    public void removeByPanelId(String panelId) {
        Long userId = AuthUtils.getUser().getUserId();
        PanelStoreExample panelStoreExample = new PanelStoreExample();
        panelStoreExample.createCriteria().andPanelGroupIdEqualTo(panelId).andUserIdEqualTo(userId);
        panelStoreMapper.deleteByExample(panelStoreExample);
    }

    public List<PanelStoreDto> query() {
        Long userId = AuthUtils.getUser().getUserId();
        return extPanelStoreMapper.query(userId);
    }

    public Long count(String panelId) {
        PanelStoreExample example = new PanelStoreExample();
        example.createCriteria().andUserIdEqualTo(AuthUtils.getUser().getUserId()).andPanelGroupIdEqualTo(panelId);
        return panelStoreMapper.countByExample(example);
    }


}
