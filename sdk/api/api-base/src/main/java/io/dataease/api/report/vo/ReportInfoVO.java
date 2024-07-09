package io.dataease.api.report.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class ReportInfoVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 2287397931951305464L;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long taskId;

    private String name;

    private String title;

    private String content;

    private Integer rtid;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long rid;

    private Integer format;

    private List<String> viewIdList;

    private Integer viewDataRange;

    private String pixel;

    private List<Integer> reciFlagList;

    private List<String> uidList;

    private List<String> ridList;

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
}
