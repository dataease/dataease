package io.dataease.api.chart.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.dataease.extensions.view.dto.ChartViewFieldDTO;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class ChartBaseVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -2445689467486374464L;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long chartId;

    private String chartType;

    private String chartName;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long resourceId;

    private String resourceType;

    private String resourceName;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long tableId;

    @JsonProperty("xAxis")
    private List<ChartViewFieldDTO> xAxis;

    /**
     * 横轴field ext
     */
    @JsonProperty("xAxisExt")
    private List<ChartViewFieldDTO> xAxisExt;

    /**
     * 纵轴field
     */
    @JsonProperty("yAxis")
    private List<ChartViewFieldDTO> yAxis;

    /**
     * 副轴
     */
    @JsonProperty("yAxisExt")
    private List<ChartViewFieldDTO> yAxisExt;

    /**
     * 堆叠项
     */
    private List<ChartViewFieldDTO> extStack;

    /**
     * 气泡大小
     */
    private List<ChartViewFieldDTO> extBubble;

    private List<ChartViewFieldDTO> flowMapStartName;

    private List<ChartViewFieldDTO> flowMapEndName;

    private List<ChartViewFieldDTO> extColor;

    private List<ChartViewFieldDTO> extLabel;

    private List<ChartViewFieldDTO> extTooltip;

}
