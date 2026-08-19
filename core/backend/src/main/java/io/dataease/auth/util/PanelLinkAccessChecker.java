package io.dataease.auth.util;

import io.dataease.dto.chart.ChartViewDTO;
import io.dataease.ext.ExtChartViewMapper;
import io.dataease.plugins.common.base.domain.DatasetTableField;
import io.dataease.plugins.common.base.domain.PanelViewExample;
import io.dataease.plugins.common.base.mapper.PanelViewMapper;
import io.dataease.service.dataset.DataSetTableFieldsService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 分享链接数据访问范围校验。
 * <p>
 * 分享页 token 仅绑定到"面板"（resourceId），所有分享数据接口必须在取数前校验
 * 所请求的视图/字段确实属于该分享面板（panel_view 绑定 + 面板内视图所用数据集的字段），
 * 防止持有任一有效分享 token 越权读取全系统其它面板/数据集数据。
 */
@Component
public class PanelLinkAccessChecker {

    @Resource
    private PanelViewMapper panelViewMapper;

    @Resource
    private ExtChartViewMapper extChartViewMapper;

    @Resource
    private DataSetTableFieldsService dataSetTableFieldsService;

    /**
     * 视图是否属于该分享面板
     */
    public boolean viewBelongsPanel(String panelId, String viewId) {
        if (StringUtils.isBlank(panelId) || StringUtils.isBlank(viewId)) {
            return false;
        }
        PanelViewExample example = new PanelViewExample();
        example.createCriteria().andPanelIdEqualTo(panelId).andChartViewIdEqualTo(viewId);
        return panelViewMapper.countByExample(example) > 0;
    }

    /**
     * 字段是否全部属于该分享面板内视图所用数据集的字段集
     */
    public boolean fieldsBelongPanel(String panelId, List<String> fieldIds) {
        if (StringUtils.isBlank(panelId) || CollectionUtils.isEmpty(fieldIds)) {
            return false;
        }
        List<ChartViewDTO> views = extChartViewMapper.searchViewsWithPanelId(panelId);
        Set<String> tableIds = views.stream().map(ChartViewDTO::getTableId)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        if (tableIds.isEmpty()) {
            return false;
        }
        for (String fieldId : fieldIds) {
            if (StringUtils.isBlank(fieldId)) {
                return false;
            }
            DatasetTableField field = dataSetTableFieldsService.selectByPrimaryKey(fieldId);
            if (field == null || !tableIds.contains(field.getTableId())) {
                return false;
            }
        }
        return true;
    }
}
