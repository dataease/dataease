package io.dataease.service.panel;

import io.dataease.base.domain.PanelShare;
import io.dataease.base.domain.PanelShareExample;
import io.dataease.base.mapper.PanelShareMapper;
import io.dataease.base.mapper.ext.ExtPanelShareMapper;
import io.dataease.base.mapper.ext.query.GridExample;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.CommonBeanFactory;
import io.dataease.controller.request.panel.PanelShareRequest;
import io.dataease.controller.sys.base.BaseGridRequest;
import io.dataease.controller.sys.base.ConditionEntity;
import io.dataease.dto.panel.PanelShareDto;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class ShareService {

    @Autowired(required = false)
    private PanelShareMapper mapper;


    @Resource
    private ExtPanelShareMapper extPanelShareMapper;

    @Transactional
    public void save(PanelShareRequest request){

        //1.先根据仪表板删除所有已经分享的
        List<String> panelIds = request.getPanelIds();
        List<Long> userIds = request.getUserIds();
        // 使用原生对象会导致事物失效 所以这里需要使用spring代理对象
        if (CollectionUtils.isNotEmpty(panelIds)){
            ShareService proxy = CommonBeanFactory.getBean(ShareService.class);
            panelIds.forEach(proxy::delete);
        }
        if (CollectionUtils.isEmpty(userIds)) return;
        long now = System.currentTimeMillis();
        List<PanelShare> shares = panelIds.stream().flatMap(panelId ->
            userIds.stream().map(userId -> {
                PanelShare share = new PanelShare();
                share.setCreateTime(now);
                share.setPanelGroupId(panelId);
                share.setUserId(userId);
                return share;
            })
        ).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(shares)){
            extPanelShareMapper.batchInsert(shares);
        }
    }

    /**
     * panel_group_id建了索引 效率不会很差
     * @param panel_group_id
     */
    @Transactional
    public void delete(String panel_group_id){
        PanelShareExample example = new PanelShareExample();
        example.createCriteria().andPanelGroupIdEqualTo(panel_group_id);
        mapper.deleteByExample(example);
    }


    public List<PanelShareDto> queryTree(BaseGridRequest request){
        Long userId = AuthUtils.getUser().getUserId();
        ConditionEntity condition = new ConditionEntity();
        condition.setField("s.user_id");
        condition.setOperator("eq");
        condition.setValue(userId);
        request.setConditions(new ArrayList<ConditionEntity>(){{add(condition);}});
        GridExample example = request.convertExample();
        List<PanelShareDto> datas = extPanelShareMapper.query(example);
        return convertTree(datas);
    }

    //List构建Tree
    private List<PanelShareDto> convertTree(List<PanelShareDto> datas){
        Map<String, List<PanelShareDto>> map = datas.stream().collect(Collectors.groupingBy(PanelShareDto::getCreator));
        List<PanelShareDto> roots = new ArrayList<>();
        return map.entrySet().stream().map(entry -> PanelShareDto.builder().name(entry.getKey()).children(entry.getValue()).build()).collect(Collectors.toList());
    }


}
