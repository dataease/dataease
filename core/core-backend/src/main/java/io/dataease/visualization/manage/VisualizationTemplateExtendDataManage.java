package io.dataease.visualization.manage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.chart.dto.ChartViewDTO;
import io.dataease.exception.DEException;
import io.dataease.template.dao.auto.entity.VisualizationTemplateExtendData;
import io.dataease.template.dao.auto.mapper.VisualizationTemplateExtendDataMapper;
import io.dataease.utils.JsonUtil;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;



/**
 * @author : WangJiaHao
 * @date : 2023/11/13 13:25
 */
@Service
public class VisualizationTemplateExtendDataManage {

    @Resource
    private VisualizationTemplateExtendDataMapper extendDataMapper;

    public ChartViewDTO getChartDataInfo(Long viewId, ChartViewDTO view) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("view_id",viewId);
        List<VisualizationTemplateExtendData> extendDataList = extendDataMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(extendDataList)) {
            ChartViewDTO chartViewTemplate = JsonUtil.parseObject(extendDataList.get(0).getViewDetails(),ChartViewDTO.class);
            view.setData(chartViewTemplate.getData());
        } else {
            DEException.throwException("模板缓存数据中未获取指定视图数据：" + viewId);
        }
        return view;
    }
}
