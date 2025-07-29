package io.dataease.visualization.manage;

import io.dataease.extensions.view.dto.ChartViewDTO;
import io.dataease.exception.DEException;
import io.dataease.template.dao.auto.entity.VisualizationTemplateExtendData;
import io.dataease.template.dao.auto.mapper.VisualizationTemplateExtendDataRepository;
import io.dataease.utils.JsonUtil;
import io.dataease.utils.LogUtil;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * @author : WangJiaHao
 * @date : 2023/11/13 13:25
 */
@Service
public class VisualizationTemplateExtendDataManage {

    @Resource
    private VisualizationTemplateExtendDataRepository extendDataRepository;

    public ChartViewDTO getChartDataInfo(Long viewId, ChartViewDTO view) {
        List<VisualizationTemplateExtendData> extendDataList = extendDataRepository.findByViewId(viewId);
        if (CollectionUtils.isNotEmpty(extendDataList)) {
            try{
                ChartViewDTO chartViewTemplate = JsonUtil.parseObject(extendDataList.get(0).getViewDetails(),ChartViewDTO.class);
                if(chartViewTemplate != null){
                    view.setData(chartViewTemplate.getData());
                }
            }catch (Exception e){
                LogUtil.error("未获取内置数据："+viewId);
            }

        } else {
            DEException.throwException("模板缓存数据中未获取指定图表数据：" + viewId);
        }
        return view;
    }
}
