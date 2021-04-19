package io.dataease.controller.panel.server;

import io.dataease.base.domain.ChartView;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.controller.panel.api.ViewApi;
import io.dataease.controller.sys.base.BaseGridRequest;
import io.dataease.controller.sys.base.ConditionEntity;
import io.dataease.dto.panel.PanelViewDto;
import io.dataease.dto.panel.po.PanelViewPo;
import io.dataease.service.chart.ChartViewService;
import io.dataease.service.panel.PanelViewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class ViewServer implements ViewApi {

    @Autowired
    private PanelViewService panelViewService;

    @Autowired
    private ChartViewService chartViewService;

    /**
     * 为什么查两次？
     * 因为left join 会导致全表扫描
     * 查两次在索引合理情况下 效率比查询一次高
     * @return
     */
    @Override
    public List<PanelViewDto> tree(@RequestBody BaseGridRequest request) {
        List<ConditionEntity> conditions = new ArrayList<>();
        ConditionEntity condition = new ConditionEntity();
        condition.setField("create_by");
        condition.setOperator("eq");
        condition.setValue(AuthUtils.getUser().getUsername());
        conditions.add(condition);
        request.setConditions(conditions);
        List<PanelViewPo> groups = panelViewService.groups(request);
        List<PanelViewPo> views = panelViewService.views(request);
        List<PanelViewDto> panelViewDtos = panelViewService.buildTree(groups, views);
        return panelViewDtos;
    }

    @Override
    public List<ChartView> viewsWithIds(@RequestBody List<String> viewIds) {

        return chartViewService.viewsByIds(viewIds);
    }
}
