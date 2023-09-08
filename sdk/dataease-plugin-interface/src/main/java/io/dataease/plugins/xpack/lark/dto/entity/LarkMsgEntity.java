package io.dataease.plugins.xpack.lark.dto.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class LarkMsgEntity implements Serializable {

    private String message_id;
    private String root_id;
    private String parent_id;
    private String msg_type;
    private String create_time;
    private String update_time;
    private Boolean deleted;
    private Boolean updated;
    private String chat_id;
    private String upper_message_id;
}
