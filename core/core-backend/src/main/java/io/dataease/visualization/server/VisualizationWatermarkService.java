package io.dataease.visualization.server;

import io.dataease.api.visualization.VisualizationWatermarkApi;
import io.dataease.api.visualization.request.VisualizationWatermarkRequest;
import io.dataease.api.visualization.vo.VisualizationWatermarkVO;
import io.dataease.utils.BeanUtils;
import io.dataease.visualization.dao.auto.entity.VisualizationWatermark;
import io.dataease.visualization.dao.auto.mapper.VisualizationWatermarkRepository;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : WangJiaHao
 * @date : 2024/1/10 16:59
 */
@RestController
@RequestMapping("/watermark")
public class VisualizationWatermarkService implements VisualizationWatermarkApi {

    private final static String DEFAULT_ID = "system_default";

    @Resource
    private VisualizationWatermarkRepository watermarkRepository;

    @Override
    public VisualizationWatermarkVO getWatermarkInfo() {
        VisualizationWatermark watermark = watermarkRepository.findById(DEFAULT_ID).orElse(null);
        VisualizationWatermarkVO watermarkVO = new VisualizationWatermarkVO();
        return BeanUtils.copyBean(watermarkVO, watermark);
    }

    @Override
    public void saveWatermarkInfo(VisualizationWatermarkRequest watermarkRequest) {
        VisualizationWatermark watermark = new VisualizationWatermark();
        BeanUtils.copyBean(watermark, watermarkRequest);
        watermark.setId(DEFAULT_ID);
        watermarkRepository.saveAndFlush(watermark);
    }
}
