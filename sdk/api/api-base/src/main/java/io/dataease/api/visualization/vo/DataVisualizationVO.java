package io.dataease.api.visualization.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.dataease.api.chart.dto.ChartViewDTO;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class DataVisualizationVO  implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 父id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long pid;

    /**
     * 所属组织id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orgId;

    /**
     * 层级
     */
    private Integer level;

    /**
     * 节点类型  folder or panel 目录或者文件夹
     */
    private String nodeType;

    /**
     * 类型
     */
    private String type;

    /**
     * 样式数据
     */
    private String canvasStyleData;

    /**
     * 组件数据
     */
    private String componentData;

    /**
     * 移动端布局
     */
    private String mobileLayout;

    /**
     * 状态 0-未发布 1-已发布
     */
    private Integer status;

    /**
     * 是否单独打开水印 0-关闭 1-开启
     */
    private Integer selfWatermarkStatus;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新时间
     */
    private Long updateTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 备注
     */
    private String remark;

    /**
     * 数据来源
     */
    private String source;

    /**
     * 视图基本信息
     */
    private Map<Long,ChartViewDTO> canvasViewInfo = new HashMap<>();
}
