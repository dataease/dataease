package io.dataease.api.visualization;

import io.dataease.api.visualization.request.VisualizationBackgroundRequest;
import io.dataease.api.visualization.vo.VisualizationBackgroundVO;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

/**
 * @author : WangJiaHao
 * @date : 2023/6/12 19:19
 */
public interface VisualizationBackgroundApi {
    @GetMapping("/findAll")
    Map<String, List<VisualizationBackgroundVO>> findAll();
}
