package io.dataease.plugins.xpack.email.dto.request;

import java.io.Serializable;

import io.dataease.plugins.common.annotation.PluginResultMap;
import lombok.Data;

@PluginResultMap
@Data
public class XpackTaskCreateRequest implements Serializable {

    private Long taskId;

    private String taskName;

    private String taskType;

    private Long startTime;

    private Long endTime;

    private Integer rateType;

    private String rateVal;

    private Long creator;

    private Long createTime;
}
