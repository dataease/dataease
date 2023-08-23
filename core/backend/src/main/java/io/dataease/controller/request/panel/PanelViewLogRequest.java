package io.dataease.controller.request.panel;

import lombok.Data;

import java.io.Serializable;

@Data
public class PanelViewLogRequest implements Serializable {
    private String panelId;

    private Boolean mobile = false;
}
