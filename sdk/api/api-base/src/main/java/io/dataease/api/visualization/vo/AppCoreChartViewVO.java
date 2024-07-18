package io.dataease.api.visualization.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

@Data
public class AppCoreChartViewVO implements Serializable {

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
    private String xAxis;

    /**
     * table-row
     */
    private String xAxisExt;

    /**
     * 纵轴field
     */
    private String yAxis;

    /**
     * 副轴
     */
    private String yAxisExt;

    /**
     * 堆叠项
     */
    private String extStack;

    /**
     * 气泡大小
     */
    private String extBubble;

    /**
     * 动态标签
     */
    private String extLabel;

    /**
     * 动态提示
     */
    private String extTooltip;

    /**
     * 图形属性
     */
    private String customAttr;

    /**
     * 组件样式
     */
    private String customStyle;

    /**
     * 结果过滤
     */
    private String customFilter;

    /**
     * 钻取字段
     */
    private String drillFields;

    /**
     * 高级
     */
    private String senior;

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
    private String viewFields;

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
     * 复制来源
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long copyFrom;

    /**
     * 复制ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long copyId;

    /**
     * 区间条形图开启时间纬度开启聚合
     */
    private Boolean aggregate;
}
