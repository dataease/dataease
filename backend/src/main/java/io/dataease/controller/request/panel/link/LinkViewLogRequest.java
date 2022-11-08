package io.dataease.controller.request.panel.link;

import io.dataease.controller.request.panel.PanelViewLogRequest;
import lombok.Data;

import java.io.Serializable;

@Data
public class LinkViewLogRequest extends PanelViewLogRequest implements Serializable {

    private Long userId;
}
