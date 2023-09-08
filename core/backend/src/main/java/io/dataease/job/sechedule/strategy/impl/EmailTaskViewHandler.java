package io.dataease.job.sechedule.strategy.impl;

import io.dataease.auth.entity.SysUserEntity;

import io.dataease.plugins.common.entity.GlobalTaskInstance;


import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Service("emailTaskViewHandler")
public class EmailTaskViewHandler extends EmailTaskHandler {


    @Async("priorityExecutor")
    public void sendReport(GlobalTaskInstance taskInstance, SysUserEntity user, Boolean isTempTask) {
        super.sendReport(taskInstance, user, isTempTask);
    }
}
