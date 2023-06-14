package io.dataease.visualization.server;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.visualization.VisualizationBackgroundApi;
import io.dataease.api.visualization.vo.VisualizationBackgroundVO;
import io.dataease.utils.BeanUtils;
import io.dataease.visualization.dao.auto.entity.VisualizationBackground;
import io.dataease.visualization.dao.auto.mapper.VisualizationBackgroundMapper;
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
    VisualizationBackgroundMapper mapper;

    @Override
    public Map<String, List<VisualizationBackgroundVO>> findAll() {
        List<VisualizationBackground> result = mapper.selectList(new QueryWrapper<>());
        return result.stream().map(vb ->{
            VisualizationBackgroundVO vbVO = new VisualizationBackgroundVO();
            BeanUtils.copyBean(vbVO,vb);
            return vbVO;
        }).collect(Collectors.groupingBy(VisualizationBackgroundVO::getClassification));
    }
}
