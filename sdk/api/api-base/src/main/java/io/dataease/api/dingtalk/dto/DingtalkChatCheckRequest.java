package io.dataease.api.dingtalk.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class DingtalkChatCheckRequest implements Serializable {
    private String chatId;
}
