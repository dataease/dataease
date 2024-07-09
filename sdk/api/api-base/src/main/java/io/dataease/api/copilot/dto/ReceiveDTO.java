package io.dataease.api.copilot.dto;

import lombok.Data;

import java.util.List;

/**
 * @Author Junjun
 */
@Data
public class ReceiveDTO {
    private String sql;
    private List<HistoryDTO> history;
    private String apiMessage;
    private Boolean sqlOk;
    private Boolean chartOk;
    private ChartDTO chart;
}
