package io.dataease.api.copilot.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @Author Junjun
 */
@Data
public class MsgDTO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long datasetGroupId;

    private String msgType;

    private String engineType;

    private String schemaSql;

    private String question;

    private List<HistoryDTO> history;

    private String copilotSql;

    private String apiMsg;

    private Integer sqlOk;

    private Integer chartOk;

    private ChartDTO chart;

    private Long createTime;

    private Map<String, Object> chartData;

    private String execSql;

    private Integer msgStatus;

    private String errMsg;
}
