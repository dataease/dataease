package io.dataease.api.chart.request;

import io.dataease.api.chart.dto.ChartExtFilterDTO;
import io.dataease.api.chart.dto.PermissionProxy;
import lombok.Data;

import java.util.List;

/**
 * @Author gin
 */
@Data
public class ChartExtRequest {
    private List<ChartExtFilterDTO> filter;

    private List<ChartExtFilterDTO> linkageFilters;

    private List<ChartExtFilterDTO> outerParamsFilters;

    private List<ChartDrillRequest> drill;

    private String queryFrom;

    private String resultMode;

    private Integer resultCount;

    private boolean cache = true;

    private Long user = null;

    private PermissionProxy proxy;

    private Long goPage;

    private Long pageSize;

    private Boolean excelExportFlag = false;

}
