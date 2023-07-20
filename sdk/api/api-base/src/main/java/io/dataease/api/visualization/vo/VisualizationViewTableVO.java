package io.dataease.api.visualization.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.dataease.dto.dataset.DatasetTableFieldDTO;
import lombok.Data;

import java.util.List;


/**
 * @author : WangJiaHao
 * @date : 2023/7/19
 */
@Data
public class VisualizationViewTableVO {

    /**
     * ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String title;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long dvId;

    /**
     * 数据集表ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long tableId;

    /**
     * 图表类型
     */
    private String type;

    /**
     * 视图渲染方式
     */
    private String render;


    private List<DatasetTableFieldDTO> tableFields;

}
