package io.dataease.controller.request.chart;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: wangjiahao
 * Date: 2022/4/26
 * Description:
 */
@Data
public class ChartCopyBatchRequest {

    private String panelId;

    private Map<String,String> sourceAndTargetIds;

}
