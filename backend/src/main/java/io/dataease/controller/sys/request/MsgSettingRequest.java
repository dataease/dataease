package io.dataease.controller.sys.request;


import lombok.Data;

import java.io.Serializable;

@Data
public class MsgSettingRequest implements Serializable {

    private Long typeId;

    private Long channelId;
}
