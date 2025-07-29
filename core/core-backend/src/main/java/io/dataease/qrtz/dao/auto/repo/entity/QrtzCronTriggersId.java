package io.dataease.qrtz.dao.auto.repo.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class QrtzCronTriggersId implements Serializable {

    private String schedName;
    private String triggerName;
    private String triggerGroup;
}
