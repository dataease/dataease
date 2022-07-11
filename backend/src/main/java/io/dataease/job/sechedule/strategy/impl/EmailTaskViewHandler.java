package io.dataease.job.sechedule.strategy.impl;

import io.dataease.auth.entity.SysUserEntity;
import io.dataease.commons.utils.CronUtils;
import io.dataease.commons.utils.LogUtil;
import io.dataease.dto.PermissionProxy;
import io.dataease.plugins.common.entity.GlobalTaskInstance;
import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.xpack.email.dto.request.XpackEmailTaskRequest;
import io.dataease.plugins.xpack.email.dto.response.XpackEmailTemplateDTO;
import io.dataease.plugins.xpack.email.service.EmailXpackService;
import io.dataease.service.chart.ViewExportExcel;
import io.dataease.service.system.EmailService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service("emailTaskViewHandler")
public class EmailTaskViewHandler extends EmailTaskHandler {

    @Resource
    private ViewExportExcel viewExportExcel;

    @Async("priorityExecutor")
    public void sendReport(GlobalTaskInstance taskInstance, XpackEmailTemplateDTO emailTemplateDTO,
            SysUserEntity user) {
        EmailXpackService emailXpackService = SpringContextUtil.getBean(EmailXpackService.class);
        List<File> files = null;
        try {
            XpackEmailTaskRequest taskForm = emailXpackService.taskForm(taskInstance.getTaskId());
            if (ObjectUtils.isEmpty(taskForm) || CronUtils.taskExpire(taskForm.getEndTime())) {
                removeInstance(taskInstance);
                return;
            }
            String panelId = emailTemplateDTO.getPanelId();

            // 下面继续执行发送邮件的
            byte[] content = emailTemplateDTO.getContent();
            EmailService emailService = SpringContextUtil.getBean(EmailService.class);

            String contentStr = "";
            if (ObjectUtils.isNotEmpty(content)) {
                contentStr = new String(content, "UTF-8");
            }
            String viewIds = emailTemplateDTO.getPixel();
            List<String> viewIdList = Arrays.asList(viewIds.split(",")).stream().map(s -> (s.trim()))
                    .collect(Collectors.toList());
            PermissionProxy proxy = new PermissionProxy();
            proxy.setUserId(user.getUserId());
            files = viewExportExcel.export(panelId, viewIdList, proxy);
            emailService.sendWithFiles(emailTemplateDTO.getRecipients(), emailTemplateDTO.getTitle(), contentStr, files);
            success(taskInstance);
        } catch (Exception e) {
            error(taskInstance, e);
            LogUtil.error(e.getMessage(), e);
        } finally {
            if (CollectionUtils.isNotEmpty(files)) {
                for (int i = 0; i < files.size(); i++) {
                    File file = files.get(i);
                    if (file.exists()) {
                        file.delete();
                    }
                }
            }
        }
    }
}
