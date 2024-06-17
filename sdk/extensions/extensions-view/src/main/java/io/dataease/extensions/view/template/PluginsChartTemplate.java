package io.dataease.extensions.view.template;

import io.dataease.extensions.view.dto.ChartViewDTO;
import io.dataease.extensions.view.dto.ChartViewFieldDTO;
import io.dataease.extensions.view.dto.DatasetTableFieldDTO;
import io.dataease.extensions.view.model.SQLMeta;

import java.util.List;
import java.util.Map;

public abstract class PluginsChartTemplate {

    public abstract String getConfig();



    public abstract Map<String, List<ChartViewFieldDTO>> formatChartAxis(ChartViewDTO view);

    /*public Map<String, List<ChartViewFieldDTO>> formatChartAxis(ChartViewDTO view) {
        Map<String, List<ChartViewFieldDTO>> result = new HashMap<>();

        List<ChartViewFieldDTO> xAxisBase = new ArrayList<>(view.getXAxis());
        result.put("xAxisBase", xAxisBase);
        List<ChartViewFieldDTO> xAxis = new ArrayList<>(view.getXAxis());
        result.put("xAxis", xAxis);
        List<ChartViewFieldDTO> xAxisExt = new ArrayList<>(view.getXAxisExt());
        result.put("xAxisExt", xAxisExt);
        List<ChartViewFieldDTO> yAxis = new ArrayList<>(view.getYAxis());
        result.put("yAxis", yAxis);
        List<ChartViewFieldDTO> extStack = new ArrayList<>(view.getExtStack());
        result.put("extStack", extStack);
        List<ChartViewFieldDTO> extBubble = new ArrayList<>(view.getExtBubble());
        result.put("extBubble", extBubble);
        List<ChartViewFieldDTO> drill = new ArrayList<>(view.getDrillFields());
        result.put("drill", drill);
        List<ChartViewFieldDTO> viewFields = new ArrayList<>(view.getViewFields());
        result.put("viewFields", viewFields);
        return result;
    }*/

    public abstract ChartViewDTO calcResult(SQLMeta sqlMeta, List<ChartViewFieldDTO> xaxis, List<ChartViewFieldDTO> yaxis,
                                            List<DatasetTableFieldDTO> allFields, boolean crossDs, Map<Long, String> dsTypeMap);

}
