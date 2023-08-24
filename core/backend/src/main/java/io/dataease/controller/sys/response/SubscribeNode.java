package io.dataease.controller.sys.response;


import lombok.Data;

import java.io.Serializable;

@Data
public class SubscribeNode implements Serializable {

    private static final long serialVersionUID = -1680823237289721438L;

    private Long typeId;

    private Long channelId;

    public Boolean match(Long type, Long channel) {
        return type == typeId && channel == channelId;
    }
}
