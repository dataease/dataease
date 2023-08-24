package io.dataease.auth.entity;

import lombok.Data;

@Data
public class AccountLockStatus {

    private Boolean locked = false;

    private String username;

    private Long unlockTime;

    private Integer relieveTimes;

    private Integer remainderTimes;
}
