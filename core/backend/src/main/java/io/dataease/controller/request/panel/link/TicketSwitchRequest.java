package io.dataease.controller.request.panel.link;

import lombok.Data;

import java.io.Serializable;

@Data
public class TicketSwitchRequest implements Serializable {

    private String resourceId;

    private Boolean require = false;
}
