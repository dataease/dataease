package io.dataease.plugins.common.entity;

import java.io.Serializable;

import lombok.Data;

@Data
public class GlobalTaskInstance implements Serializable{

    private Long instanceId;

    private Long taskId;

    private Long executeTime;

    private Long finishTime;

    private Integer status;

    private String info;
    
}
