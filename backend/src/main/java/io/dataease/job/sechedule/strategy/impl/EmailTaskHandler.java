package io.dataease.job.sechedule.strategy.impl;

import cn.hutool.core.io.FileUtil;
import io.dataease.auth.entity.SysUserEntity;
import io.dataease.auth.entity.TokenInfo;
import io.dataease.auth.service.AuthUserService;
import io.dataease.auth.service.impl.AuthUserServiceImpl;
import io.dataease.auth.util.JWTUtils;
import io.dataease.commons.utils.*;
import io.dataease.dto.PermissionProxy;
import io.dataease.dto.chart.ViewOption;
import io.dataease.ext.ExtTaskMapper;
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
import io.dataease.plugins.xpack.larksuite.dto.response.LarksuiteMsgResult;
import io.dataease.plugins.xpack.larksuite.service.LarksuiteXpackService;
import io.dataease.plugins.xpack.wecom.dto.entity.WecomMsgResult;
import io.dataease.plugins.xpack.wecom.service.WecomXpackService;
import io.dataease.service.chart.ChartViewService;
import io.dataease.service.chart.ViewExportExcel;
import io.dataease.service.sys.SysUserService;
import io.dataease.service.system.EmailService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("emailTaskHandler")
public class EmailTaskHandler extends TaskHandler implements Job {

    private static final Integer RUNNING = 0;
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
        Boolean isTempTask = (Boolean) jobDataMap.getOrDefault(IS_TEMP_TASK, false);
        GlobalTaskEntity taskEntity = (GlobalTaskEntity) jobDataMap.get("taskEntity");
        ScheduleManager scheduleManager = SpringContextUtil.getBean(ScheduleManager.class);
        if (!isTempTask && (CronUtils.taskExpire(taskEntity.getEndTime()) || !taskEntity.getStatus())) {
            removeTask(scheduleManager, taskEntity);
            return;
        }
        if (!isTempTask && taskIsRunning(taskEntity.getTaskId())) {
            LogUtil.info("Skip synchronization task: {} ,due to task status is {}",
                    taskEntity.getTaskId(), "running");
            return;
        }

        GlobalTaskInstance taskInstance = buildInstance(taskEntity);
        Long instanceId = saveInstance(taskInstance);
        taskInstance.setInstanceId(instanceId);

        SysUserEntity creator = (SysUserEntity) jobDataMap.get("creator");
        LogUtil.info("start execute send panel report task...");
        proxy(taskEntity.getTaskType()).sendReport(taskInstance, creator, isTempTask);
        if (isTempTask) {
            removeTask(scheduleManager, taskEntity);
        }

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
        taskInstance.setStatus(RUNNING);
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
    public void sendReport(GlobalTaskInstance taskInstance, SysUserEntity user, Boolean isTempTask) {

        EmailXpackService emailXpackService = SpringContextUtil.getBean(EmailXpackService.class);
        AuthUserServiceImpl userService = SpringContextUtil.getBean(AuthUserServiceImpl.class);
        SysUserService sysUserService = SpringContextUtil.getBean(SysUserService.class);
        List<File> files = null;
        String token = null;
        try {
            XpackEmailTemplateDTO emailTemplateDTO = emailXpackService.emailTemplate(taskInstance.getTaskId());
            XpackEmailTaskRequest taskForm = emailXpackService.taskForm(taskInstance.getTaskId());
            if (ObjectUtils.isEmpty(taskForm) || (!isTempTask && (CronUtils.taskExpire(taskForm.getEndTime()) || !emailXpackService.status(taskInstance.getTaskId())))) {
                removeInstance(taskInstance);
                return;
            }
            String panelId = emailTemplateDTO.getPanelId();
            String url = panelUrl(panelId);
            token = tokenByUser(user);
            XpackPixelEntity xpackPixelEntity = buildPixel(emailTemplateDTO);
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
                contentStr = HtmlUtils.htmlUnescape(new String(content, "UTF-8"));
            }


            String viewIds = emailTemplateDTO.getViewIds();
            ChartViewService chartViewService = SpringContextUtil.getBean(ChartViewService.class);
            List<ViewOption> viewOptions = chartViewService.viewOptions(panelId);
            if (StringUtils.isNotBlank(viewIds) && CollectionUtils.isNotEmpty(viewOptions)) {
                List<String> viewOptionIdList = viewOptions.stream().map(ViewOption::getId).collect(Collectors.toList());
                String viewDataRange = emailTemplateDTO.getViewDataRange();
                Boolean justExportView = StringUtils.isBlank(viewDataRange) || StringUtils.equals("view", viewDataRange);
                List<String> viewIdList = Arrays.asList(viewIds.split(",")).stream().map(s -> s.trim()).filter(viewId -> StringUtils.isNotBlank(viewId) && viewOptionIdList.contains(viewId)).collect(Collectors.toList());
                PermissionProxy proxy = new PermissionProxy();
                proxy.setUserId(user.getUserId());
                files = viewExportExcel.export(panelId, viewIdList, proxy, justExportView, taskInstance.getTaskId().toString());
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
                                Integer panelFormat = emailTemplateDTO.getPanelFormat();
                                if (ObjectUtils.isEmpty(panelFormat) || panelFormat == 0) {
                                    byte[] bytes = emailXpackService.printData(url, token, xpackPixelEntity);
                                    emailService.sendWithImageAndFiles(recipients, emailTemplateDTO.getTitle(), contentStr, bytes, files);
                                } else {
                                    byte[] bytes = emailXpackService.printPdf(url, token, xpackPixelEntity, false, true);
                                    emailService.sendPdfWithFiles(recipients, emailTemplateDTO.getTitle(), contentStr, bytes, files);
                                }

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
                                byte[] bytes = emailXpackService.printData(url, token, xpackPixelEntity);
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
                                byte[] bytes = emailXpackService.printData(url, token, xpackPixelEntity);
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
                                byte[] bytes = emailXpackService.printData(url, token, xpackPixelEntity);
                                LarkMsgResult larkMsgResult = larkXpackService.pushOaMsg(larkUsers, emailTemplateDTO.getTitle(), contentStr, bytes, files);
                                if (larkMsgResult.getCode() != 0) {
                                    errorMsgs.add("lark: " + larkMsgResult.getMsg());
                                }
                            }

                        }
                        break;
                    case "larksuite":
                        if (SpringContextUtil.getBean(AuthUserService.class).supportLarksuite()) {
                            List<String> larksuiteUsers = new ArrayList<>();
                            for (int j = 0; j < reciLists.size(); j++) {
                                String reci = reciLists.get(j);
                                SysUserEntity userBySub = userService.getUserByName(reci);
                                if (ObjectUtils.isEmpty(userBySub)) continue;
                                Long userId = userBySub.getUserId();
                                SysUserAssist sysUserAssist = sysUserService.assistInfo(userId);
                                if (ObjectUtils.isEmpty(sysUserAssist) || StringUtils.isBlank(sysUserAssist.getLarksuiteId()))
                                    continue;
                                larksuiteUsers.add(sysUserAssist.getLarksuiteId());
                            }

                            if (CollectionUtils.isNotEmpty(larksuiteUsers)) {
                                LarksuiteXpackService larksuiteXpackService = SpringContextUtil.getBean(LarksuiteXpackService.class);
                                byte[] bytes = emailXpackService.printData(url, token, xpackPixelEntity);
                                LarksuiteMsgResult larksuiteMsgResult = larksuiteXpackService.pushOaMsg(larksuiteUsers, emailTemplateDTO.getTitle(), contentStr, bytes, files);
                                if (larksuiteMsgResult.getCode() != 0) {
                                    errorMsgs.add("larksuite: " + larksuiteMsgResult.getMsg());
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
        } finally {
            if (StringUtils.isNotBlank(token)) {
                TokenCacheUtils.add(token, user.getUserId());
            }
            if (CollectionUtils.isNotEmpty(files)) {
                files.forEach(file -> {
                    if (file.exists()) {
                        FileUtil.del(file);
                    }
                });
            }
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
        String token = JWTUtils.sign(tokenInfo, user.getPassword(), false);

        return token;
    }

    private String panelUrl(String panelId) {
        String domain = ServletUtils.domain();
        return domain + "/#/previewScreenShot/" + panelId + "/true";
    }

}
