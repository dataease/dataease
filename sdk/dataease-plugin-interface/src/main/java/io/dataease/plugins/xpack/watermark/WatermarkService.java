package io.dataease.plugins.xpack.watermark;

import io.dataease.plugins.common.service.PluginMenuService;
import io.dataease.plugins.xpack.watermark.dto.PanelWatermarkDTO;

import java.util.List;

/**
 * Author: wangjiahao
 * Date: 2022/11/10
 * Description:
 */
public abstract class WatermarkService  extends PluginMenuService {

    public abstract PanelWatermarkDTO getWatermarkInfo();

    public abstract void saveWatermarkInfo(PanelWatermarkDTO panelWatermark);


}
