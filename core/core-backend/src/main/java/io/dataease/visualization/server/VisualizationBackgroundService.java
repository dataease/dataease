package io.dataease.visualization.server;

import io.dataease.api.visualization.VisualizationBackgroundApi;
import io.dataease.api.visualization.vo.VisualizationBackgroundVO;
import io.dataease.i18n.Translator;
import io.dataease.utils.BeanUtils;
import io.dataease.visualization.dao.auto.entity.VisualizationBackground;
import io.dataease.visualization.dao.auto.mapper.VisualizationBackgroundRepository;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author : WangJiaHao
 * @date : 2023/6/12 19:31
 */
@RestController
@RequestMapping("/visualizationBackground")
public class VisualizationBackgroundService implements VisualizationBackgroundApi {
    @Resource
    VisualizationBackgroundRepository visualizationBackgroundRepository;

    @Override
    public Map<String, List<VisualizationBackgroundVO>> findAll() {
        List<VisualizationBackground> result = visualizationBackgroundRepository.findAll();
        return result.stream().map(vb -> {
            VisualizationBackgroundVO vbVO = new VisualizationBackgroundVO();
            BeanUtils.copyBean(vbVO, vb);
            vbVO.setName(Translator.get("i18n_board") + vbVO.getName());
            return vbVO;
        }).collect(Collectors.groupingBy(VisualizationBackgroundVO::getClassification));
    }
}
