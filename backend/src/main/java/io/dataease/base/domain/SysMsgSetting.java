package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class SysMsgSetting implements Serializable {
    private Long msgSettingId;

    private Long userId;

    private Long typeId;

    private Long channelId;

    private Boolean enable;

    private static final long serialVersionUID = 1L;

    public Boolean match(SysMsgSetting setting) {
        return setting.getTypeId() == typeId && setting.getChannelId() == channelId;
    }
}