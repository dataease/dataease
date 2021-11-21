package io.dataease.job.sechedule.strategy.impl;


import io.dataease.auth.entity.SysUserEntity;
import io.dataease.auth.entity.TokenInfo;
import io.dataease.auth.service.AuthUserService;
import io.dataease.auth.util.JWTUtils;
import io.dataease.commons.utils.CommonBeanFactory;
import io.dataease.commons.utils.LogUtil;
import io.dataease.job.sechedule.strategy.TaskHandler;
import io.dataease.plugins.common.entity.GlobalTaskEntity;
import io.dataease.plugins.common.entity.GlobalTaskInstance;
import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.xpack.email.dto.response.XpackEmailTemplateDTO;
import io.dataease.plugins.xpack.email.service.EmailXpackService;
import io.dataease.service.FileService;
import io.dataease.service.system.EmailService;
import org.apache.commons.lang3.ObjectUtils;
import org.quartz.*;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;


@Service
public class EmailTaskHandler extends TaskHandler implements Job {


    private static final Integer RUNING = 0;
    private static final Integer SUCCESS = 1;
    private static final Integer ERROR = -1;



    @Override
    protected JobDataMap jobDataMap(GlobalTaskEntity taskEntity) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("taskEntity", taskEntity);
        EmailXpackService emailXpackService = SpringContextUtil.getBean(EmailXpackService.class);
        XpackEmailTemplateDTO emailTemplateDTO = emailXpackService.emailTemplate(taskEntity.getTaskId());
        jobDataMap.put("emailTemplate", emailTemplateDTO);

        SysUserEntity creator = CommonBeanFactory.getBean(AuthUserService.class).getUserById(taskEntity.getCreator());
        jobDataMap.put("creator", creator);
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

        XpackEmailTemplateDTO emailTemplate = (XpackEmailTemplateDTO) jobDataMap.get("emailTemplate");
        SysUserEntity creator = (SysUserEntity) jobDataMap.get("creator");
        proxy().sendReport(taskInstance, emailTemplate, creator);

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

    private void changeStatus(GlobalTaskInstance taskInstance, Integer status) {
        taskInstance.setStatus(status);
        if(status == SUCCESS) {
            taskInstance.setFinishTime(System.currentTimeMillis());
        }
        EmailXpackService emailXpackService = SpringContextUtil.getBean(EmailXpackService.class);
        emailXpackService.saveInstance(taskInstance);
    }



    @Async
    public void sendReport(GlobalTaskInstance taskInstance, XpackEmailTemplateDTO emailTemplateDTO, SysUserEntity user) {
        EmailXpackService emailXpackService = SpringContextUtil.getBean(EmailXpackService.class);
        String fileId = emailXpackService.print(panelUrl(emailTemplateDTO.getPanelId()), tokenByUser(user));
        // 下面继续执行发送邮件的略记
        String recipients = emailTemplateDTO.getRecipients();
        byte[] content = emailTemplateDTO.getContent();
        EmailService emailService = SpringContextUtil.getBean(EmailService.class);
        String contentStr = "";
        byte[] bytes = null;
        if (ObjectUtils.isNotEmpty(content)) {
            try {
                contentStr = new String(content, "UTF-8");
                FileService fileService = SpringContextUtil.getBean(FileService.class);
                bytes = fileService.loadFileAsBytes(fileId);
                emailService.sendWithImage(recipients, emailTemplateDTO.getTitle(), contentStr, bytes);
                changeStatus(taskInstance, SUCCESS);
            } catch (Exception e) {
                changeStatus(taskInstance, ERROR);
                LogUtil.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }

    }



    private String tokenByUser(SysUserEntity user) {
        TokenInfo tokenInfo = TokenInfo.builder().userId(user.getUserId()).username(user.getUsername()).build();
        String token = JWTUtils.sign(tokenInfo, user.getPassword());

        return token;
    }

    private String panelUrl(String panelId) {
        InetAddress ip = null;
        String hostAddress = null;
        try {
            ip = InetAddress.getLocalHost();
            hostAddress = ip.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        Environment environment = SpringContextUtil.getBean(Environment.class);
        Integer port = environment.getProperty("server.port", Integer.class);
        String url = "http://" + hostAddress + ":"+port + "/#/preview/" + panelId;

        return url;
    }

}
