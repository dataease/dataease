package io.dataease.controller.request.chart;

import io.dataease.plugins.common.base.domain.ChartViewCacheWithBLOBs;
import lombok.Data;

/**
 * Author: wangjiahao
 * Date: 2022/3/10
 * Description:
 */
@Data
public class ChartViewCacheRequest  extends ChartViewCacheWithBLOBs {

    private String savePosition = "cache";
}
