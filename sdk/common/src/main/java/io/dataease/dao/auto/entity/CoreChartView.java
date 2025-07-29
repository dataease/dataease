package io.dataease.dao.auto.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@Comment("组件视图表")
@Entity
@Table(name = "core_chart_view")
public class CoreChartView {
    @Id
    @Comment("ID")
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 1024)
    @Comment("标题")
    @Column(name = "title", length = 1024)
    private String title;

    @NotNull
    @Comment("场景ID chart_type为private的时候 是仪表板id")
    @Column(name = "scene_id", nullable = false)
    private Long sceneId;

    @Comment("数据集表ID")
    @Column(name = "table_id")
    private Long tableId;

    @Size(max = 50)
    @Comment("图表类型")
    @Column(name = "type", length = 50)
    private String type;

    @Size(max = 50)
    @Comment("视图渲染方式")
    @Column(name = "render", length = 50)
    private String render;

    @Comment("展示结果")
    @Column(name = "result_count")
    private Integer resultCount;

    @Size(max = 50)
    @Comment("展示模式")
    @Column(name = "result_mode", length = 50)
    private String resultMode;

    @Column(name = "x_axis", length = 16777216)
    private String xAxis;

    @Column(name = "x_axis_ext", length = 16777216)
    private String xAxisExt;

    @Column(name = "y_axis", length = 16777216)
    private String yAxis;

    @Column(name = "y_axis_ext", length = 16777216)
    private String yAxisExt;

    @Column(name = "ext_stack", length = 16777216)
    private String extStack;

    @Column(name = "ext_bubble", length = 16777216)
    private String extBubble;

    @Column(name = "ext_label", length = 16777216)
    private String extLabel;

    @Column(name = "ext_tooltip", length = 16777216)
    private String extTooltip;

    @Column(name = "custom_attr", length = 16777216)
    private String customAttr;

    @Column(name = "custom_attr_mobile", length = 16777216)
    private String customAttrMobile;

    @Column(name = "custom_style", length = 16777216)
    private String customStyle;

    @Column(name = "custom_style_mobile", length = 16777216)
    private String customStyleMobile;

    @Column(name = "custom_filter", length = 16777216)
    private String customFilter;

    @Column(name = "drill_fields", length = 16777216)
    private String drillFields;

    @Column(name = "senior", length = 16777216)
    private String senior;

    @Size(max = 50)
    @Comment("创建人ID")
    @Column(name = "create_by", length = 50)
    private String createBy;

    @Comment("创建时间")
    @Column(name = "create_time")
    private Long createTime;

    @Comment("更新时间")
    @Column(name = "update_time")
    private Long updateTime;

    @Column(name = "snapshot", length = 16777216)
    private String snapshot;

    @Size(max = 255)
    @Comment("样式优先级 panel 仪表板 view 视图")
    @ColumnDefault("'panel'")
    @Column(name = "style_priority")
    private String stylePriority;

    @Size(max = 255)
    @Comment("视图类型 public 公共 历史可复用的视图，private 私有 专属某个仪表板")
    @ColumnDefault("'private'")
    @Column(name = "chart_type")
    private String chartType;

    @Comment("是否插件")
    @Column(name = "is_plugin")
    private Boolean isPlugin;

    @Size(max = 255)
    @Comment("数据来源 template 模板数据 dataset 数据集数据")
    @ColumnDefault("'dataset'")
    @Column(name = "data_from")
    private String dataFrom;

    @Column(name = "view_fields", length = 16777216)
    private String viewFields;

    @Comment("是否开启刷新")
    @ColumnDefault("false")
    @Column(name = "refresh_view_enable")
    private Boolean refreshViewEnable;

    @Size(max = 255)
    @Comment("刷新时间单位")
    @ColumnDefault("'minute'")
    @Column(name = "refresh_unit")
    private String refreshUnit;

    @Comment("刷新时间")
    @ColumnDefault("5")
    @Column(name = "refresh_time")
    private Integer refreshTime;

    @ColumnDefault("false")
    @Column(name = "linkage_active")
    private Boolean linkageActive;

    @Column(name = "jump_active")
    private Boolean jumpActive;

    @Column(name = "copy_from")
    private Long copyFrom;

    @Column(name = "copy_id")
    private Long copyId;

    @Comment("区间条形图开启时间纬度开启聚合")
    @Column(name = "aggregate")
    private Boolean aggregate;

    @Column(name = "flow_map_start_name", length = 16777216)
    private String flowMapStartName;

    @Column(name = "flow_map_end_name", length = 16777216)
    private String flowMapEndName;

    @Column(name = "ext_color", length = 16777216)
    private String extColor;

    @Column(name = "sort_priority", length = 16777216)
    private String sortPriority;

}
