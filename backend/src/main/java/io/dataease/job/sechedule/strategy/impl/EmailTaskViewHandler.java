package io.dataease.job.sechedule.strategy.impl;

import io.dataease.auth.entity.SysUserEntity;

import io.dataease.plugins.common.entity.GlobalTaskInstance;

import io.dataease.plugins.xpack.email.dto.response.XpackEmailTemplateDTO;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


@Service("emailTaskViewHandler")
public class EmailTaskViewHandler extends EmailTaskHandler {



    @Async("priorityExecutor")
    public void sendReport(GlobalTaskInstance taskInstance, XpackEmailTemplateDTO emailTemplateDTO, SysUserEntity user) {
        super.sendReport(taskInstance, emailTemplateDTO, user);
    }
}
