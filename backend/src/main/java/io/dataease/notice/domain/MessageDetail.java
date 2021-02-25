package io.dataease.notice.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MessageDetail {
    private List<String> userIds = new ArrayList<>();
    private String event;
    private String taskType;
    private String webhook;
    private String type;
    private String identification;
    private String organizationId;
    private Boolean isSet;
    private String testId;
    private Long createTime;
    private String template;
}
