package io.dataease.plugins.common.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class SysMsgChannel implements Serializable {
    private Long msgChannelId;

    private String channelName;

    private String serviceName;

    private static final long serialVersionUID = 1L;
}