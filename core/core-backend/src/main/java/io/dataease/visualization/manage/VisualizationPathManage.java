package io.dataease.visualization.manage;

import io.dataease.chart.manage.ChartViewManege;
import io.dataease.dao.auto.entity.DataVisualizationInfo;
import io.dataease.extensions.view.dto.ChartViewDTO;
import io.dataease.visualization.dao.auto.mapper.DataVisualizationInfoRepository;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 可视化资源路径只读服务：计算图表/仪表板等资源的绝对路径。
 * 不依赖任何 *Server。
 */
@Component
public class VisualizationPathManage {

    @Resource
    private ChartViewManege chartViewManege;
    @Resource
    private DataVisualizationInfoRepository dataVisualizationInfoRepository;

    public String getAbsPath(Long id) {
        ChartViewDTO viewDTO = chartViewManege.findChartViewAround(String.valueOf(id));
        if (viewDTO == null) {
            return null;
        }
        if (viewDTO.getPid() == null) {
            return viewDTO.getTitle();
        }
        List<DataVisualizationInfo> parents = getParents(viewDTO.getPid());
        StringBuilder stringBuilder = new StringBuilder();
        parents.forEach(ele -> {
            if (ObjectUtils.isNotEmpty(ele)) {
                stringBuilder.append(ele.getName()).append("/");
            }
        });
        stringBuilder.append(viewDTO.getTitle());
        return stringBuilder.toString();
    }

    public List<DataVisualizationInfo> getParents(Long id) {
        List<DataVisualizationInfo> list = new ArrayList<>();
        DataVisualizationInfo dataVisualizationInfo = dataVisualizationInfoRepository.findById(id).orElse(null);
        list.add(dataVisualizationInfo);
        if (dataVisualizationInfo.getPid().equals(dataVisualizationInfo.getId())) {
            return list;
        }
        getParent(list, dataVisualizationInfo);
        Collections.reverse(list);
        return list;
    }

    public void getParent(List<DataVisualizationInfo> list, DataVisualizationInfo dataVisualizationInfo) {
        if (ObjectUtils.isNotEmpty(dataVisualizationInfo) && dataVisualizationInfo.getPid() != null && !dataVisualizationInfo.getPid().equals(dataVisualizationInfo.getId())) {
            DataVisualizationInfo d = dataVisualizationInfoRepository.findById(dataVisualizationInfo.getPid()).orElse(null);
            list.add(d);
            getParent(list, d);
        }
    }
}
