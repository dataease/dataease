package io.dataease.dto;

import io.dataease.base.domain.PanelGroupExtendData;
import lombok.Data;
import org.pentaho.di.core.util.UUIDUtil;

/**
 * Author: wangjiahao
 * Date: 2022/3/14
 * Description:
 */
@Data
public class PanelGroupExtendDataDTO extends PanelGroupExtendData {
    public PanelGroupExtendDataDTO(String panelId,String viewId,String viewDetails) {
        super();
        super.setId(UUIDUtil.getUUIDAsString());
        super.setPanelId(panelId);
        super.setViewId(viewId);
        super.setViewDetails(viewDetails);
    }

    public PanelGroupExtendDataDTO() {
        super();
    }
}
