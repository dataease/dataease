package io.dataease.service.panel;

import com.alibaba.fastjson.JSONObject;
import io.dataease.base.domain.PanelGroupExtendData;
import io.dataease.base.domain.PanelGroupExtendDataExample;
import io.dataease.base.mapper.PanelGroupExtendDataMapper;
import io.dataease.commons.constants.CommonConstants;
import io.dataease.dto.chart.ChartViewDTO;
import io.dataease.exception.DataEaseException;
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

    public ChartViewDTO getChartDataInfo(String viewId,ChartViewDTO view){
        PanelGroupExtendDataExample extendDataExample = new PanelGroupExtendDataExample();
        extendDataExample.createCriteria().andViewIdEqualTo(viewId);
        List<PanelGroupExtendData>  extendDataList = panelGroupExtendDataMapper.selectByExampleWithBLOBs(extendDataExample);
        if(CollectionUtils.isNotEmpty(extendDataList)){
            ChartViewDTO chartViewTemplate = JSONObject.parseObject(extendDataList.get(0).getViewDetails(),ChartViewDTO.class);
            view.setData(chartViewTemplate.getData());
        }else{
            DataEaseException.throwException("模板缓存数据中未获取指定视图数据："+viewId);
        }
        return view;
    }


}
