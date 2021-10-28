package io.dataease.dto.panel.linkJump;

import lombok.Data;

/**
 * Author: wangjiahao
 * Date: 2021/10/28
 * Description:
 */
@Data
public class PanelLinkJumpBaseRequest {

    private String sourcePanelId;

    private String sourceViewId;

    private String sourceFieldId;

    private String targetPanelId;

}
