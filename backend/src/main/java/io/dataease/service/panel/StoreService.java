package io.dataease.service.panel;

import io.dataease.base.domain.PanelStore;
import io.dataease.base.mapper.PanelStoreMapper;
import io.dataease.base.mapper.ext.ExtPanelStoreMapper;
import io.dataease.base.mapper.ext.query.GridExample;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.controller.sys.base.BaseGridRequest;
import io.dataease.controller.sys.base.ConditionEntity;
import io.dataease.dto.panel.PanelStoreDto;
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

    public void save(String panelGroupId){
        Long userId = AuthUtils.getUser().getUserId();
        PanelStore panelStore = new PanelStore();
        panelStore.setCreateTime(System.currentTimeMillis());
        panelStore.setPanelGroupId(panelGroupId);
        panelStore.setUserId(userId);
        panelStoreMapper.insert(panelStore);
    }



    public void remove(Long storeId){
        panelStoreMapper.deleteByPrimaryKey(storeId);
    }

    /**
     * 按照当前用户ID查询收藏仪表板
     * @param request
     * @return
     */
    public List<PanelStoreDto> query(BaseGridRequest request){
        Long userId = AuthUtils.getUser().getUserId();
        ConditionEntity condition = new ConditionEntity();
        condition.setField("s.user_id");
        condition.setOperator("eq");
        condition.setValue(userId);
        request.setConditions(new ArrayList<ConditionEntity>(){{add(condition);}});
        GridExample example = request.convertExample();
        List<PanelStoreDto> stores = extPanelStoreMapper.query(example);
        return stores;
    }

}
