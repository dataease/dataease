package io.dataease.notice.sender;

import io.dataease.base.domain.SysUser;
import io.dataease.commons.constants.NoticeConstants;
import io.dataease.commons.utils.LogUtil;
import io.dataease.notice.domain.MessageDetail;
import io.dataease.notice.domain.UserDetail;
import io.dataease.service.sys.SysUserService;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractNoticeSender implements NoticeSender {
    @Resource
    private SysUserService sysUserService;

    protected String getContext(MessageDetail messageDetail, NoticeModel noticeModel) {
        // 如果配置了模版就直接使用模版
        if (StringUtils.isNotBlank(messageDetail.getTemplate())) {
            return getContent(messageDetail.getTemplate(), noticeModel.getParamMap());
        }
        // 处理 userIds 中包含的特殊值
        List<String> realUserIds = getRealUserIds(messageDetail.getUserIds(), noticeModel.getRelatedUsers(), messageDetail.getEvent());
        messageDetail.setUserIds(realUserIds);

        // 处理 WeCom Ding context
        String context = "";
        switch (messageDetail.getEvent()) {
            case NoticeConstants.Event.CREATE:
            case NoticeConstants.Event.UPDATE:
            case NoticeConstants.Event.DELETE:
            case NoticeConstants.Event.COMMENT:
                context = noticeModel.getContext();
                break;
            case NoticeConstants.Event.EXECUTE_FAILED:
                context = noticeModel.getFailedContext();
                break;
            case NoticeConstants.Event.EXECUTE_SUCCESSFUL:
                context = noticeModel.getSuccessContext();
                break;
            default:
                break;
        }
        return context;
    }

    protected String getHtmlContext(MessageDetail messageDetail, NoticeModel noticeModel) {
        // 如果配置了模版就直接使用模版
        if (StringUtils.isNotBlank(messageDetail.getTemplate())) {
            return getContent(messageDetail.getTemplate(), noticeModel.getParamMap());
        }
        // 处理 userIds 中包含的特殊值
        List<String> realUserIds = getRealUserIds(messageDetail.getUserIds(), noticeModel.getRelatedUsers(), messageDetail.getEvent());
        messageDetail.setUserIds(realUserIds);

        // 处理 mail context
        String context = "";
        try {
            switch (messageDetail.getEvent()) {
                case NoticeConstants.Event.CREATE:
                case NoticeConstants.Event.UPDATE:
                case NoticeConstants.Event.DELETE:
                case NoticeConstants.Event.COMMENT:
                    URL resource = this.getClass().getResource("/mail/" + noticeModel.getMailTemplate() + ".html");
                    context = IOUtils.toString(resource, StandardCharsets.UTF_8);
                    break;
                case NoticeConstants.Event.EXECUTE_FAILED:
                    URL resource1 = this.getClass().getResource("/mail/" + noticeModel.getFailedMailTemplate() + ".html");
                    context = IOUtils.toString(resource1, StandardCharsets.UTF_8);
                    break;
                case NoticeConstants.Event.EXECUTE_SUCCESSFUL:
                    URL resource2 = this.getClass().getResource("/mail/" + noticeModel.getSuccessMailTemplate() + ".html");
                    context = IOUtils.toString(resource2, StandardCharsets.UTF_8);
                    break;
                default:
                    break;
            }
        } catch (IOException e) {
            LogUtil.error(e);
        }
        return getContent(context, noticeModel.getParamMap());
    }

    protected String getContent(String template, Map<String, Object> context) {
        if (MapUtils.isNotEmpty(context)) {
            for (String k : context.keySet()) {
                if (context.get(k) != null) {
                    template = RegExUtils.replaceAll(template, "\\$\\{" + k + "}", context.get(k).toString());
                } else {
                    template = RegExUtils.replaceAll(template, "\\$\\{" + k + "}", "未设置");
                }
            }
        }
        return template;
    }

    protected List<String> getUserPhones(List<String> userIds) {
        List<Long> userIdLists = userIds.stream().map(Long::parseLong).collect(Collectors.toList());
        List<SysUser> list = sysUserService.users(userIdLists);
        //List<UserDetail> list = userService.queryTypeByIds(userIds);
        List<String> phoneList = new ArrayList<>();
        list.forEach(u -> phoneList.add(u.getPhone()));
        LogUtil.info("收件人地址: " + phoneList);
        return phoneList.stream().distinct().collect(Collectors.toList());
    }

    protected List<String> getUserEmails(List<String> userIds) {
        /*List<UserDetail> list = userService.queryTypeByIds(userIds);*/
        List<Long> userIdLists = userIds.stream().map(Long::parseLong).collect(Collectors.toList());
        List<SysUser> list = sysUserService.users(userIdLists);
        List<String> phoneList = new ArrayList<>();
        list.forEach(u -> phoneList.add(u.getEmail()));
        LogUtil.info("收件人地址: " + phoneList);
        return phoneList.stream().distinct().collect(Collectors.toList());
    }

    private List<String> getRealUserIds(List<String> userIds, List<String> relatedUsers, String event) {
        List<String> toUserIds = new ArrayList<>();
        for (String userId : userIds) {
            switch (userId) {
                case NoticeConstants.RelatedUser.EXECUTOR:
                    if (StringUtils.equals(NoticeConstants.Event.CREATE, event)) {
                        toUserIds.addAll(relatedUsers);
                    }
                    break;
                case NoticeConstants.RelatedUser.FOUNDER:
                    if (StringUtils.equals(NoticeConstants.Event.UPDATE, event)
                            || StringUtils.equals(NoticeConstants.Event.DELETE, event)) {
                        toUserIds.addAll(relatedUsers);
                    }
                    break;
                case NoticeConstants.RelatedUser.MAINTAINER:
                    if (StringUtils.equals(NoticeConstants.Event.COMMENT, event)) {
                        toUserIds.addAll(relatedUsers);
                    }
                    break;
                default:
                    toUserIds.add(userId);
                    break;
            }
        }

        return toUserIds;
    }
}
