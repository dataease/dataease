package io.dataease.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class TaskInstance implements Serializable{

    private String taskId;

    private Long executeTime;

    private Long finishTime;

    private String status;

    private String info;

    private String qrtzInstance;

}
