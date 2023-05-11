package io.dataease.chart.manage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.chart.dto.ChartViewDTO;
import io.dataease.chart.dao.auto.entity.CoreChartView;
import io.dataease.chart.dao.auto.mapper.CoreChartViewMapper;
import io.dataease.exception.DEException;
import io.dataease.utils.BeanUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author Junjun
 */
@Component
public class ChartViewManege {
    @Resource
    private CoreChartViewMapper coreChartViewMapper;

    public ChartViewDTO save(ChartViewDTO chartViewDTO) {
        Long id = chartViewDTO.getId();
        if (id == null) {
            DEException.throwException("no chart id");
        }
        CoreChartView coreChartView = coreChartViewMapper.selectById(id);
        CoreChartView record = new CoreChartView();
        BeanUtils.copyBean(record, chartViewDTO);
        if (ObjectUtils.isEmpty(coreChartView)) {
            coreChartViewMapper.insert(record);
        } else {
            coreChartViewMapper.updateById(record);
        }
        return chartViewDTO;
    }

    public void delete(Long id) {
        coreChartViewMapper.deleteById(id);
    }

    public void deleteByPanel(Long panelId, List<Long> chartIds) {
        QueryWrapper<CoreChartView> wrapper = new QueryWrapper<>();
        wrapper.eq("scene_id", panelId);
        wrapper.notIn("id", chartIds);
        coreChartViewMapper.delete(wrapper);
    }

    public ChartViewDTO getDetails(Long id) {
        CoreChartView coreChartView = coreChartViewMapper.selectById(id);
        if (ObjectUtils.isEmpty(coreChartView)) {
            return null;
        }
        ChartViewDTO dto = new ChartViewDTO();
        BeanUtils.copyBean(dto, coreChartView);
        return dto;
    }

    /**
     * sceneId 为仪表板或者数据大屏id
     * */
    public List<ChartViewDTO> listBySceneId(Long sceneId) {
        QueryWrapper<CoreChartView> wrapper = new QueryWrapper<>();
        wrapper.eq("scene_id", sceneId);
        return transChart(coreChartViewMapper.selectList(wrapper));
    }

    public List<ChartViewDTO> transChart(List<CoreChartView> list) {
        if (ObjectUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream().map(ele -> {
            ChartViewDTO dto = new ChartViewDTO();
            BeanUtils.copyBean(dto, ele);
            return dto;
        }).collect(Collectors.toList());
    }
}
