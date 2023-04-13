package io.dataease.controller.request.panel;

import io.dataease.controller.request.chart.ChartExtRequest;
import io.dataease.dto.PermissionProxy;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Author: wangjiahao
 * Date: 2022/4/8
 * Description:
 */
@Data
public class PanelViewDetailsRequest {

    private String viewId;

    private String viewName;

    private String[] header;

    private Integer[] excelTypes;

    private List<Object[]> details;

    private String snapshot;

    private int snapshotWidth;

    private int snapshotHeight;

    private ViewDetailField[] detailFields;

    private ChartExtRequest componentFilterInfo;

    private List<String> excelHeaderKeys;

    @ApiModelProperty(hidden = true)
    private PermissionProxy proxy;

    private Long userId;

}
