package io.dataease.controller.request.panel;

import io.dataease.plugins.common.base.domain.PanelAppTemplateWithBLOBs;
import lombok.Data;

/**
 * Author: wangjiahao
 * Date: 2022/9/8
 * Description:
 */
@Data
public class PanelAppTemplateRequest extends PanelAppTemplateWithBLOBs {
    private String optType;
}
