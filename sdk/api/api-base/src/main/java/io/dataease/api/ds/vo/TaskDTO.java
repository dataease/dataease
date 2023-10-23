package io.dataease.api.ds.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class TaskDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1175287571828910222L;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;
    private String updateType;
    private String syncRate;
    private Long simpleCronValue;
    private String simpleCronType;
    private Long startTime;
    private Long endTime;
    private String endLimit;
    private String cron;
}
