package io.dataease.extensions.view.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.dataease.extensions.view.filter.FilterTreeObj;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 图表base dto，与core_table_view一致
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
     * 图表渲染方式
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

    /**
     * 动态标签
     */
    private List<ChartViewFieldDTO> extLabel;

    /**
     * 动态提示
     */
    private List<ChartViewFieldDTO> extTooltip;

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
    private FilterTreeObj customFilter;

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
     * 样式优先级 panel 仪表板 view 图表
     */
    private String stylePriority;

    /**
     * 图表类型 public 公共 历史可复用的图表，private 私有 专属某个仪表板
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
     * 图表字段集合
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

    /**
     * 是否开启联动
     */
    private Boolean linkageActive;

    /**
     * 是否开启跳转
     */
    private Boolean jumpActive;

    /**
     * 区间条形图开启时间纬度开启聚合
     */
    private Boolean aggregate;

    /**
     * 流向地图起点名称
     */
    private List<ChartViewFieldDTO> flowMapStartName;

    /**
     * 流向地图终点名称
     */
    private List<ChartViewFieldDTO> flowMapEndName;

}
