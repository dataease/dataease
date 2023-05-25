package io.dataease.api.ds.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class TaskDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1175287571828910222L;

    private String updateType;
    private String syncRate;
    private Long simple_cron_value;
    private String simple_cron_type;
    private Long startTime;
    private Long endTime;
    private Long endLimit;
    private String cron;
}
