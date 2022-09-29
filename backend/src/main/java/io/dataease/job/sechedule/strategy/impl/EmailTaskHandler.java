package io.dataease.job.sechedule.strategy.impl;

import io.dataease.auth.entity.SysUserEntity;
import io.dataease.auth.entity.TokenInfo;
import io.dataease.auth.service.AuthUserService;
import io.dataease.auth.service.impl.AuthUserServiceImpl;
import io.dataease.auth.util.JWTUtils;
import io.dataease.dto.PermissionProxy;
import io.dataease.ext.ExtTaskMapper;
import io.dataease.commons.utils.CommonBeanFactory;
import io.dataease.commons.utils.CronUtils;
import io.dataease.commons.utils.LogUtil;
import io.dataease.commons.utils.ServletUtils;
import io.dataease.job.sechedule.ScheduleManager;
import io.dataease.job.sechedule.strategy.TaskHandler;
import io.dataease.plugins.common.base.domain.SysUserAssist;
import io.dataease.plugins.common.entity.GlobalTaskEntity;
import io.dataease.plugins.common.entity.GlobalTaskInstance;
import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.xpack.dingtalk.dto.entity.DingtalkMsgResult;
import io.dataease.plugins.xpack.dingtalk.service.DingtalkXpackService;
import io.dataease.plugins.xpack.email.dto.request.XpackEmailTaskRequest;
import io.dataease.plugins.xpack.email.dto.request.XpackPixelEntity;
import io.dataease.plugins.xpack.email.dto.response.XpackEmailTemplateDTO;
import io.dataease.plugins.xpack.email.service.EmailXpackService;
import io.dataease.plugins.xpack.lark.dto.entity.LarkMsgResult;
import io.dataease.plugins.xpack.lark.service.LarkXpackService;
import io.dataease.plugins.xpack.wecom.dto.entity.WecomMsgResult;
import io.dataease.plugins.xpack.wecom.service.WecomXpackService;
import io.dataease.service.chart.ViewExportExcel;
import io.dataease.service.sys.SysUserService;
import io.dataease.service.system.EmailService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("emailTaskHandler")
public class EmailTaskHandler extends TaskHandler implements Job {

    private static final Integer RUNING = 0;
    private static final Integer SUCCESS = 1;
    private static final Integer ERROR = -1;

    @Resource
    private AuthUserServiceImpl authUserServiceImpl;

    @Resource
    private ViewExportExcel viewExportExcel;

    @Override
    protected JobDataMap jobDataMap(GlobalTaskEntity taskEntity) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("taskEntity", taskEntity);
        EmailXpackService emailXpackService = SpringContextUtil.getBean(EmailXpackService.class);
        XpackEmailTemplateDTO emailTemplateDTO = emailXpackService.emailTemplate(taskEntity.getTaskId());
        jobDataMap.put("emailTemplate", emailTemplateDTO);
        SysUserEntity creator = authUserServiceImpl.getUserByIdNoCache(taskEntity.getCreator());
        jobDataMap.put("creator", creator);
        return jobDataMap;
    }

    public EmailTaskHandler proxy(String handlerName) {
        return (EmailTaskHandler) CommonBeanFactory.getBean(handlerName);
    }

    @Override
    protected Boolean taskIsRunning(Long taskId) {
        ExtTaskMapper extTaskMapper = CommonBeanFactory.getBean(ExtTaskMapper.class);
        return extTaskMapper.runningCount(taskId) > 0;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // 插件没有加载 空转
        if (!CommonBeanFactory.getBean(AuthUserService.class).pluginLoaded())
            return;

        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        GlobalTaskEntity taskEntity = (GlobalTaskEntity) jobDataMap.get("taskEntity");
        ScheduleManager scheduleManager = SpringContextUtil.getBean(ScheduleManager.class);
        if (CronUtils.taskExpire(taskEntity.getEndTime())) {
            removeTask(scheduleManager, taskEntity);
            return;
        }
        if (taskIsRunning(taskEntity.getTaskId())) {
            LogUtil.info("Skip synchronization task: {} ,due to task status is {}",
                    taskEntity.getTaskId(), "running");
            return;
        }

        GlobalTaskInstance taskInstance = buildInstance(taskEntity);
        Long instanceId = saveInstance(taskInstance);
        taskInstance.setInstanceId(instanceId);

        XpackEmailTemplateDTO emailTemplate = (XpackEmailTemplateDTO) jobDataMap.get("emailTemplate");
        SysUserEntity creator = (SysUserEntity) jobDataMap.get("creator");
        LogUtil.info("start execute send panel report task...");
        proxy(taskEntity.getTaskType()).sendReport(taskInstance, emailTemplate, creator);

    }

    @Override
    public void resetRunningInstance(Long taskId) {
        ExtTaskMapper extTaskMapper = CommonBeanFactory.getBean(ExtTaskMapper.class);
        extTaskMapper.resetRunnings(taskId);
    }

    public Long saveInstance(GlobalTaskInstance taskInstance) {
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

    protected void success(GlobalTaskInstance taskInstance) {
        taskInstance.setStatus(SUCCESS);
        taskInstance.setFinishTime(System.currentTimeMillis());
        EmailXpackService emailXpackService = SpringContextUtil.getBean(EmailXpackService.class);
        emailXpackService.saveInstance(taskInstance);
    }

    protected void removeInstance(GlobalTaskInstance taskInstance) {
        EmailXpackService emailXpackService = SpringContextUtil.getBean(EmailXpackService.class);
        Optional.ofNullable(taskInstance).ifPresent(instance ->
                Optional.ofNullable(taskInstance.getInstanceId()).ifPresent(instanceId ->
                        emailXpackService.delInstance(instanceId)));
    }

    protected void error(GlobalTaskInstance taskInstance, Throwable t) {
        taskInstance.setStatus(ERROR);
        taskInstance.setInfo(t.getMessage());
        EmailXpackService emailXpackService = SpringContextUtil.getBean(EmailXpackService.class);
        emailXpackService.saveInstance(taskInstance);
    }

    @Async("priorityExecutor")
    public void sendReport(GlobalTaskInstance taskInstance, XpackEmailTemplateDTO emailTemplateDTO, SysUserEntity user) {

        EmailXpackService emailXpackService = SpringContextUtil.getBean(EmailXpackService.class);
        AuthUserServiceImpl userService = SpringContextUtil.getBean(AuthUserServiceImpl.class);
        SysUserService sysUserService = SpringContextUtil.getBean(SysUserService.class);
        try {
            XpackEmailTaskRequest taskForm = emailXpackService.taskForm(taskInstance.getTaskId());
            if (ObjectUtils.isEmpty(taskForm) || CronUtils.taskExpire(taskForm.getEndTime())) {
                removeInstance(taskInstance);
                return;
            }
            String panelId = emailTemplateDTO.getPanelId();
            String url = panelUrl(panelId);
            String token = tokenByUser(user);
            XpackPixelEntity xpackPixelEntity = buildPixel(emailTemplateDTO);
            LogUtil.info("url is " + url);
            LogUtil.info("token is " + token);
            byte[] bytes = emailXpackService.printData(url, token, xpackPixelEntity);
            LogUtil.info("picture of " + url + " is finished");
            // 下面继续执行发送邮件的
            String recipients = emailTemplateDTO.getRecipients();
            String reciUsers = emailTemplateDTO.getReciUsers();
            List<String> reciLists = null;
            if (StringUtils.isNotBlank(reciUsers)) {
                String emailUsers = Arrays.stream(reciUsers.split(",")).map(userService::getUserByName).filter(tempUser -> StringUtils.isNotBlank(tempUser.getEmail())).map(SysUserEntity::getEmail).collect(Collectors.joining(","));
                if (StringUtils.isNotBlank(emailUsers)) {
                    if (StringUtils.isNotBlank(recipients)) {
                        recipients += "," + emailUsers;
                    } else {
                        recipients = emailUsers;
                    }
                }
                reciLists = Arrays.stream(reciUsers.split(",")).collect(Collectors.toList());
            }


            byte[] content = emailTemplateDTO.getContent();
            EmailService emailService = SpringContextUtil.getBean(EmailService.class);

            String contentStr = "";
            if (ObjectUtils.isNotEmpty(content)) {
                contentStr = new String(content, "UTF-8");
            }

            List<File> files = null;
            String viewIds = emailTemplateDTO.getViewIds();
            if (StringUtils.isNotBlank(viewIds)) {
                List<String> viewIdList = Arrays.asList(viewIds.split(",")).stream().filter(StringUtils::isNotBlank).map(s -> (s.trim())).collect(Collectors.toList());
                PermissionProxy proxy = new PermissionProxy();
                proxy.setUserId(user.getUserId());
                files = viewExportExcel.export(panelId, viewIdList, proxy);
            }

            List<String> channels = null;
            String recisetting = emailTemplateDTO.getRecisetting();
            if (StringUtils.isBlank(recisetting)) {
                channels = new ArrayList<>();
                channels.add("email");
            } else {
                channels = Arrays.stream(recisetting.split(",")).collect(Collectors.toList());
            }

            List<String> errorMsgs = new ArrayList<>();
            for (int i = 0; i < channels.size(); i++) {
                String channel = channels.get(i);
                switch (channel) {
                    case "email":
                        if (StringUtils.isNotBlank(recipients))
                            try {
                                emailService.sendWithImageAndFiles(recipients, emailTemplateDTO.getTitle(), contentStr, bytes, files);
                            } catch (Exception e) {
                                errorMsgs.add("email: " + e.getMessage());
                            }
                        break;
                    case "wecom":
                        if (SpringContextUtil.getBean(AuthUserService.class).supportWecom()) {
                            List<String> wecomUsers = new ArrayList<>();
                            for (int j = 0; j < reciLists.size(); j++) {
                                String reci = reciLists.get(j);
                                SysUserEntity userBySub = userService.getUserByName(reci);
                                if (ObjectUtils.isEmpty(userBySub)) continue;
                                Long userId = userBySub.getUserId();
                                SysUserAssist sysUserAssist = sysUserService.assistInfo(userId);
                                if (ObjectUtils.isEmpty(sysUserAssist) || StringUtils.isBlank(sysUserAssist.getWecomId()))
                                    continue;
                                wecomUsers.add(sysUserAssist.getWecomId());
                            }

                            if (CollectionUtils.isNotEmpty(wecomUsers)) {
                                WecomXpackService wecomXpackService = SpringContextUtil.getBean(WecomXpackService.class);
                                WecomMsgResult wecomMsgResult = wecomXpackService.pushOaMsg(wecomUsers, emailTemplateDTO.getTitle(), contentStr, bytes, files);
                                if (wecomMsgResult.getErrcode() != 0) {
                                    errorMsgs.add("wecom: " + wecomMsgResult.getErrmsg());
                                }
                            }

                        }
                        break;
                    case "dingtalk":
                        if (SpringContextUtil.getBean(AuthUserService.class).supportDingtalk()) {
                            List<String> dingTalkUsers = new ArrayList<>();
                            for (int j = 0; j < reciLists.size(); j++) {
                                String reci = reciLists.get(j);
                                SysUserEntity userBySub = userService.getUserByName(reci);
                                if (ObjectUtils.isEmpty(userBySub)) continue;
                                Long userId = userBySub.getUserId();
                                SysUserAssist sysUserAssist = sysUserService.assistInfo(userId);
                                if (ObjectUtils.isEmpty(sysUserAssist) || StringUtils.isBlank(sysUserAssist.getDingtalkId()))
                                    continue;
                                dingTalkUsers.add(sysUserAssist.getDingtalkId());
                            }

                            if (CollectionUtils.isNotEmpty(dingTalkUsers)) {
                                DingtalkXpackService dingtalkXpackService = SpringContextUtil.getBean(DingtalkXpackService.class);
                                DingtalkMsgResult dingtalkMsgResult = dingtalkXpackService.pushOaMsg(dingTalkUsers, emailTemplateDTO.getTitle(), contentStr, bytes, files);
                                if (dingtalkMsgResult.getErrcode() != 0) {
                                    errorMsgs.add("dingtalk: " + dingtalkMsgResult.getErrmsg());
                                }
                            }

                        }
                        break;
                    case "lark":
                        if (SpringContextUtil.getBean(AuthUserService.class).supportLark()) {
                            List<String> larkUsers = new ArrayList<>();
                            for (int j = 0; j < reciLists.size(); j++) {
                                String reci = reciLists.get(j);
                                SysUserEntity userBySub = userService.getUserByName(reci);
                                if (ObjectUtils.isEmpty(userBySub)) continue;
                                Long userId = userBySub.getUserId();
                                SysUserAssist sysUserAssist = sysUserService.assistInfo(userId);
                                if (ObjectUtils.isEmpty(sysUserAssist) || StringUtils.isBlank(sysUserAssist.getLarkId()))
                                    continue;
                                larkUsers.add(sysUserAssist.getLarkId());
                            }

                            if (CollectionUtils.isNotEmpty(larkUsers)) {
                                LarkXpackService larkXpackService = SpringContextUtil.getBean(LarkXpackService.class);
                                LarkMsgResult larkMsgResult = larkXpackService.pushOaMsg(larkUsers, emailTemplateDTO.getTitle(), contentStr, bytes, files);
                                if (larkMsgResult.getCode() != 0) {
                                    errorMsgs.add("lark: " + larkMsgResult.getMsg());
                                }
                            }

                        }
                        break;
                    default:
                        break;
                }
            }
            if (CollectionUtils.isNotEmpty(errorMsgs)) {
                String msg = errorMsgs.stream().collect(Collectors.joining(" \n "));
                Exception exception = new RuntimeException(msg);
                throw exception;
            }
            success(taskInstance);
        } catch (Exception e) {
            error(taskInstance, e);
            LogUtil.error(e.getMessage(), e);
        }
    }


    private XpackPixelEntity buildPixel(XpackEmailTemplateDTO emailTemplateDTO) {
        XpackPixelEntity pixelEntity = new XpackPixelEntity();
        String pixelStr = emailTemplateDTO.getPixel();
        if (StringUtils.isBlank(pixelStr))
            return null;
        String[] arr = pixelStr.split("\\*");
        if (arr.length != 2)
            return null;
        try {
            int x = Integer.parseInt(arr[0].trim());
            int y = Integer.parseInt(arr[1].trim());
            pixelEntity.setX(String.valueOf(x));
            pixelEntity.setY(String.valueOf(y));
            return pixelEntity;
        } catch (Exception e) {
            return null;
        }
    }

    private String tokenByUser(SysUserEntity user) {
        TokenInfo tokenInfo = TokenInfo.builder().userId(user.getUserId()).username(user.getUsername()).build();
        String token = JWTUtils.sign(tokenInfo, user.getPassword());

        return token;
    }

    private String panelUrl(String panelId) {
        String domain = ServletUtils.domain();
        return domain + "/#/previewScreenShot/" + panelId + "/true";
    }

}
