package io.dataease.chart.manage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.chart.dto.ChartFieldCompareDTO;
import io.dataease.api.chart.dto.ChartViewDTO;
import io.dataease.api.chart.dto.ChartViewFieldDTO;
import io.dataease.chart.dao.auto.entity.CoreChartView;
import io.dataease.chart.dao.auto.mapper.CoreChartViewMapper;
import io.dataease.dataset.dao.auto.entity.CoreDatasetTableField;
import io.dataease.dataset.dao.auto.mapper.CoreDatasetTableFieldMapper;
import io.dataease.exception.DEException;
import io.dataease.utils.BeanUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author Junjun
 */
@Component
public class ChartViewManege {
    @Resource
    private CoreChartViewMapper coreChartViewMapper;
    @Resource
    private ChartDataManage chartDataManage;
    @Resource
    private CoreDatasetTableFieldMapper coreDatasetTableFieldMapper;

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

    public ChartViewDTO getChart(Long id) throws Exception {
        ChartViewDTO details = getDetails(id);
        if (details == null) {
            return null;
        }
        return chartDataManage.calcData(details);
    }

    public Map<String, List<ChartViewFieldDTO>> listByDQ(Long id) {
        QueryWrapper<CoreDatasetTableField> wrapper = new QueryWrapper<>();
        wrapper.eq("dataset_group_id", id);
        List<ChartViewFieldDTO> list = transDTO(coreDatasetTableFieldMapper.selectList(wrapper));
        List<ChartViewFieldDTO> dimensionList = list.stream().filter(ele -> StringUtils.equalsIgnoreCase(ele.getGroupType(), "d")).collect(Collectors.toList());
        List<ChartViewFieldDTO> quotaList = list.stream().filter(ele -> StringUtils.equalsIgnoreCase(ele.getGroupType(), "q")).collect(Collectors.toList());

        quotaList.add(createCountField(id));

        Map<String, List<ChartViewFieldDTO>> map = new LinkedHashMap<>();
        map.put("dimensionList", dimensionList);
        map.put("quotaList", quotaList);
        return map;
    }

    public ChartViewFieldDTO createCountField(Long id) {
        ChartViewFieldDTO dto = new ChartViewFieldDTO();
        dto.setId(-1L);
        dto.setDatasetGroupId(id);
        dto.setOriginName("*");
        dto.setName("记录数*");
        dto.setDataeaseName("*");
        dto.setType("INT");
        dto.setChecked(true);
        dto.setColumnIndex(999);
        dto.setDeType(2);
        dto.setExtField(1);
        dto.setGroupType("q");
        dto.setSummary("count");
        return dto;
    }

    public List<ChartViewFieldDTO> transDTO(List<CoreDatasetTableField> list) {
        return list.stream().map(ele -> {
            ChartViewFieldDTO dto = new ChartViewFieldDTO();
            if (ele == null) return null;
            BeanUtils.copyBean(dto, ele);
            if (StringUtils.equalsIgnoreCase("d", dto.getGroupType())) {
                dto.setDateStyle("y_M_d");
                dto.setDatePattern("date_sub");
            }
            if (StringUtils.equalsIgnoreCase("q", dto.getGroupType())) {
                dto.setChartType("bar");
                if (dto.getId() == -1L || dto.getDeType() == 0 || dto.getDeType() == 1) {
                    dto.setSummary("count");
                } else {
                    dto.setSummary("sum");
                }

                ChartFieldCompareDTO chartFieldCompareDTO = new ChartFieldCompareDTO();
                chartFieldCompareDTO.setType("none");
                dto.setCompareCalc(chartFieldCompareDTO);
            }

            dto.setSort("none");
            dto.setFilter(Collections.emptyList());
            return dto;
        }).collect(Collectors.toList());
    }

}
