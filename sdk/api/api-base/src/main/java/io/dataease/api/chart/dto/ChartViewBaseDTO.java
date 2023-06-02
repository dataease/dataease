package io.dataease.api.chart.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 视图base dto，与core_table_view一致
 *
 * @author fit2cloud
 * @since 2023-04-23
 */
@Data
public class ChartViewBaseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 场景ID chart_type为private的时候 是仪表板id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long sceneId;

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

    /**
     * 展示结果
     */
    private Integer resultCount;

    /**
     * 展示模式
     */
    private String resultMode;

    /**
     * 横轴field
     */
    private List<ChartViewFieldDTO> xAxis;

    /**
     * 横轴field ext
     */
    private List<ChartViewFieldDTO> xAxisExt;

    /**
     * 纵轴field
     */
    private List<ChartViewFieldDTO> yAxis;

    /**
     * 副轴
     */
    private List<ChartViewFieldDTO> yAxisExt;

    /**
     * 堆叠项
     */
    private List<ChartViewFieldDTO> extStack;

    /**
     * 气泡大小
     */
    private List<ChartViewFieldDTO> extBubble;

    /**
     * 图形属性
     */
    private Map<String, Object> customAttr;

    /**
     * 组件样式
     */
    private Map<String, Object> customStyle;

    /**
     * 结果过滤
     */
    private List<ChartFieldCustomFilterDTO> customFilter;

    /**
     * 钻取字段
     */
    private List<ChartViewFieldDTO> drillFields;

    /**
     * 高级
     */
    private Map<String, Object> senior;

    /**
     * 创建人ID
     */
    private String createBy;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 更新时间
     */
    private Long updateTime;

    /**
     * 缩略图
     */
    private String snapshot;

    /**
     * 样式优先级 panel 仪表板 view 视图
     */
    private String stylePriority;

    /**
     * 视图类型 public 公共 历史可复用的视图，private 私有 专属某个仪表板
     */
    private String chartType;

    /**
     * 是否插件
     */
    private Boolean isPlugin;

    /**
     * 数据来源 template 模板数据 dataset 数据集数据
     */
    private String dataFrom;

    /**
     * 视图字段集合
     */
    private List<ChartViewFieldDTO> viewFields;

    /**
     * 是否开启刷新
     */
    private Boolean refreshViewEnable;

    /**
     * 刷新时间单位
     */
    private String refreshUnit;

    /**
     * 刷新时间
     */
    private Integer refreshTime;

}
