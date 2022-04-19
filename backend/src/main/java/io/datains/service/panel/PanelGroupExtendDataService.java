package io.datains.service.panel;

import com.alibaba.fastjson.JSONObject;
import io.datains.base.domain.PanelGroupExtendData;
import io.datains.base.domain.PanelGroupExtendDataExample;
import io.datains.base.mapper.PanelGroupExtendDataMapper;
import io.datains.dto.chart.ChartViewDTO;
import io.datains.exception.DataInsException;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Author: wangjiahao
 * Date: 2022/3/15
 * Description:
 */
@Service
public class PanelGroupExtendDataService {

    @Resource
    private PanelGroupExtendDataMapper panelGroupExtendDataMapper;

    public ChartViewDTO getChartDataInfo(String viewId, ChartViewDTO view){
        PanelGroupExtendDataExample extendDataExample = new PanelGroupExtendDataExample();
        extendDataExample.createCriteria().andViewIdEqualTo(viewId);
        List<PanelGroupExtendData>  extendDataList = panelGroupExtendDataMapper.selectByExampleWithBLOBs(extendDataExample);
        if(CollectionUtils.isNotEmpty(extendDataList)){
            ChartViewDTO chartViewTemplate = JSONObject.parseObject(extendDataList.get(0).getViewDetails(),ChartViewDTO.class);
            view.setData(chartViewTemplate.getData());
        }else{
            DataInsException.throwException("模板缓存数据中未获取指定视图数据："+viewId);
        }
        return view;
    }


}
