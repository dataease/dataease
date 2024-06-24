package io.dataease.api.xpack.share.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class TicketSwitchRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 7670768142874123370L;

    private String resourceId;

    private Boolean require = false;
}
