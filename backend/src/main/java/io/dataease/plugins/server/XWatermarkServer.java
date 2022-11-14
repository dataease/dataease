package io.dataease.plugins.server;

import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.xpack.watermark.WatermarkService;
import io.dataease.plugins.xpack.watermark.dto.PanelWatermarkDTO;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

/**
 * Author: wangjiahao
 * Date: 2022/11/11
 * Description:
 */
@ApiIgnore
@RequestMapping("/plugin/watermark")
@RestController
public class XWatermarkServer {

    @ApiOperation("查询水印配置")
    @GetMapping("/find")
    public PanelWatermarkDTO find() {
        WatermarkService userXpackService = SpringContextUtil.getBean(WatermarkService.class);
        return userXpackService.getWatermarkInfo();
    }

    @ApiOperation("保存水印配置")
    @PostMapping("/save")
    public void save(PanelWatermarkDTO panelWatermark) {
        WatermarkService userXpackService = SpringContextUtil.getBean(WatermarkService.class);
        userXpackService.saveWatermarkInfo(panelWatermark);
    }
}
