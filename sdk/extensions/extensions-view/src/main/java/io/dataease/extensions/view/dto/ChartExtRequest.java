package io.dataease.extensions.view.dto;


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

    // private PermissionProxy proxy;

    private Long goPage;

    private Long pageSize;

    private Boolean excelExportFlag = false;

}
