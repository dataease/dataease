package io.dataease.notice.domain;

import lombok.Data;

@Data
public class Mail {
    // 发送给谁
    private String to;

    // 发送主题
    private String subject;

    // 发送内容
    private String content;

    // 附件地址
    private String filePath;
}
