package io.dataease.api.report.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.dataease.api.visualization.vo.VisualizationReportFilterVO;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class ReportCreator implements Serializable {
    @Serial
    private static final long serialVersionUID = 376997744239219719L;

    private String name;

    private String title;

    private String content;

    private Integer rtid;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long rid;

    private Integer format;

    private List<Long> viewIdList;

    private Integer viewDataRange;

    private String pixel;

    private List<Integer> reciFlagList;

    private List<Long> uidList;

    private List<Long> ridList;

    private List<String> emailList;

    private List<String> larkGroupList;

    private Integer extWaitTime;

    private Integer rateType;

    private String rateVal;

    private Long startTime;

    private Long endTime;

    private Boolean retryEnable;

    private Integer retryLimit;

    private Integer retryInterval;

    private List<VisualizationReportFilterVO> reportFilter;
}
