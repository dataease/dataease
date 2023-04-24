package io.dataease.api.chart.dto;

import lombok.Data;

@Data
public class PageInfo {
    private Long goPage;
    private Long pageSize;
    private String dsVersion;
}
