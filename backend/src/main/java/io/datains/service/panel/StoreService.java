package io.datains.service.panel;

import io.datains.base.domain.PanelStore;
import io.datains.base.domain.PanelStoreExample;
import io.datains.base.mapper.PanelStoreMapper;
import io.datains.base.mapper.ext.ExtPanelStoreMapper;
import io.datains.base.mapper.ext.query.GridExample;
import io.datains.commons.utils.AuthUtils;
import io.datains.controller.sys.base.BaseGridRequest;
import io.datains.controller.sys.base.ConditionEntity;
import io.datains.dto.panel.PanelStoreDto;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
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

    public List<PanelStoreDto> query(BaseGridRequest request) {
        Long userId = AuthUtils.getUser().getUserId();
        ConditionEntity condition = new ConditionEntity();
        condition.setField("s.user_id");
        condition.setOperator("eq");
        condition.setValue(userId);
        request.setConditions(new ArrayList<ConditionEntity>() {{
            add(condition);
        }});
        GridExample example = request.convertExample();
        return extPanelStoreMapper.query(example);
    }

    public Long count(String panelId) {
        PanelStoreExample example = new PanelStoreExample();
        example.createCriteria().andUserIdEqualTo(AuthUtils.getUser().getUserId()).andPanelGroupIdEqualTo(panelId);
        return panelStoreMapper.countByExample(example);
    }


}
