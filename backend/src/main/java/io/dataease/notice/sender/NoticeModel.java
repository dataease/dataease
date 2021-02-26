package io.dataease.notice.sender;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class NoticeModel {
    /**
     * 保存 测试id
     */
    private String testId;
    /**
     * 保存状态
     */
    private String status;
    /**
     * Event
     */
    private String event;
    /**
     * 消息主题
     */
    private String subject;
    /**
     * 消息内容
     */
    private String context;
    private String successContext;
    private String failedContext;

    /**
     * html 消息模版
     */
    private String mailTemplate;
    private String failedMailTemplate;
    private String successMailTemplate;

    /**
     * 保存特殊的用户
     */
    private List<String> relatedUsers;

    /**
     * 模版里的参数信息
     */
    private Map<String, Object> paramMap;
}
