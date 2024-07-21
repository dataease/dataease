package io.dataease.visualization.utils;


import io.dataease.datasource.provider.ApiUtils;
import io.dataease.datasource.provider.ExcelUtils;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
import io.dataease.extensions.view.dto.ChartViewDTO;
import io.dataease.utils.JsonUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class VisualizationUtils {

   public static Map<Long,String> viewTransToStr(Map<Long, ChartViewDTO> source ){
       Map<Long,String> result = new HashMap<>();
       source.forEach((key,value) ->{
           result.put(key, (String)JsonUtil.toJSONString(value));
       });
       return result;
    }

    public static Map<Long,ChartViewDTO> viewTransToObj (Map<Long, String> source ){
        Map<Long,ChartViewDTO> result = new HashMap<>();
        source.forEach((key,value) ->{
            result.put(key, JsonUtil.parseObject(value,ChartViewDTO.class));
        });
        return result;
    }
}
