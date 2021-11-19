package io.dataease.job.sechedule.strategy.impl;


import io.dataease.auth.service.AuthUserService;
import io.dataease.commons.utils.CommonBeanFactory;
import io.dataease.job.sechedule.strategy.TaskHandler;

import io.dataease.plugins.common.entity.GlobalTaskEntity;
import io.dataease.plugins.common.entity.GlobalTaskInstance;
import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.xpack.email.service.EmailXpackService;
import org.quartz.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Service
public class EmailTaskHandler extends TaskHandler implements Job {


    private static final Integer RUNING = 0;




    @Override
    protected JobDataMap jobDataMap(GlobalTaskEntity taskEntity) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("taskEntity", taskEntity);
        return jobDataMap;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // 插件没有加载 空转
        if(!CommonBeanFactory.getBean(AuthUserService.class).pluginLoaded()) return;

        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        GlobalTaskEntity taskEntity = (GlobalTaskEntity)jobDataMap.get("taskEntity");

        GlobalTaskInstance taskInstance = buildInstance(taskEntity);
        Long instanceId = saveInstance(taskInstance);
        taskInstance.setInstanceId(instanceId);
        proxy().printImage(taskEntity, taskInstance);
    }


    public EmailTaskHandler proxy() {
        return CommonBeanFactory.getBean(EmailTaskHandler.class);
    }

    public Long saveInstance(GlobalTaskInstance taskInstance){
        EmailXpackService emailXpackService = SpringContextUtil.getBean(EmailXpackService.class);
        return emailXpackService.saveInstance(taskInstance);
    }

    private GlobalTaskInstance buildInstance(GlobalTaskEntity taskEntity) {
        GlobalTaskInstance taskInstance = new GlobalTaskInstance();
        taskInstance.setTaskId(taskEntity.getTaskId());
        taskInstance.setStatus(RUNING);
        taskInstance.setExecuteTime(System.currentTimeMillis());
        return taskInstance;
    }

    @Async
    public void printImage(GlobalTaskEntity taskEntity, GlobalTaskInstance taskInstance) {
        EmailXpackService emailXpackService = SpringContextUtil.getBean(EmailXpackService.class);
        emailXpackService.executeJob(taskEntity, taskInstance);

    }



}
