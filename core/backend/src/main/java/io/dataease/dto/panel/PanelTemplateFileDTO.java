package io.dataease.dto.panel;

import io.dataease.plugins.common.base.domain.PanelTemplateWithBLOBs;
import lombok.Data;

/**
 * Author: wangjiahao
 * Date: 2022/7/14
 * Description:
 */
@Data
public class PanelTemplateFileDTO extends PanelTemplateWithBLOBs {

    private String panelStyle;

    private String panelData;

    private String staticResource;

}
