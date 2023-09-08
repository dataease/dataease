package io.dataease.plugins.xpack.wecom.dto.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class WecomMsgResult extends BaseResult implements Serializable {

    private String invaliduser;

    private String invalidparty;

    private String invalidtag;

    private String unlicenseduser;

    private String msgid;

    private String response_code;
}
