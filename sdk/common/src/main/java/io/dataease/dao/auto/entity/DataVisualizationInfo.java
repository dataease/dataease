package io.dataease.dao.auto.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@Comment("可视化大屏信息表")
@Entity
@Table(name = "data_visualization_info")
public class DataVisualizationInfo {
    @Id
    @Comment("主键")
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 255)
    @Comment("名称")
    @Column(name = "name")
    private String name;

    @Comment("父id")
    @Column(name = "pid")
    private Long pid;

    @Comment("所属组织id")
    @Column(name = "org_id")
    private Long orgId;

    @Comment("层级")
    @Column(name = "level")
    private Integer level;

    @Size(max = 255)
    @Comment("节点类型  folder or panel 目录或者文件夹")
    @Column(name = "node_type")
    private String nodeType;

    @Size(max = 50)
    @Comment("类型")
    @Column(name = "type", length = 50)
    private String type;

    @Lob
    @Column(name = "canvas_style_data", length = 16777216)
    private String canvasStyleData;

    @Lob
    @Column(name = "component_data", length = 16777216)
    private String componentData;

    @Comment("移动端布局0-关闭 1-开启")
    @ColumnDefault("0")
    @Column(name = "mobile_layout")
    private Byte mobileLayout;

    @Comment("状态 0-未发布 1-已发布")
    @ColumnDefault("1")
    @Column(name = "status")
    private Integer status;

    @Comment("是否单独打开水印 0-关闭 1-开启")
    @ColumnDefault("0")
    @Column(name = "self_watermark_status")
    private Integer selfWatermarkStatus;

    @Comment("排序")
    @ColumnDefault("0")
    @Column(name = "sort")
    private Integer sort;

    @Comment("创建时间")
    @Column(name = "create_time")
    private Long createTime;

    @Size(max = 255)
    @Comment("创建人")
    @Column(name = "create_by")
    private String createBy;

    @Comment("更新时间")
    @Column(name = "update_time")
    private Long updateTime;

    @Size(max = 255)
    @Comment("更新人")
    @Column(name = "update_by")
    private String updateBy;

    @Size(max = 255)
    @Comment("备注")
    @Column(name = "remark")
    private String remark;

    @Size(max = 255)
    @Comment("数据来源")
    @Column(name = "source")
    private String source;

    @Comment("删除标志")
    @ColumnDefault("0")
    @Column(name = "delete_flag")
    private Boolean deleteFlag;

    @Comment("删除时间")
    @Column(name = "delete_time")
    private Long deleteTime;

    @Size(max = 255)
    @Comment("删除人")
    @Column(name = "delete_by")
    private String deleteBy;

    @Comment("可视化资源版本")
    @ColumnDefault("3")
    @Column(name = "version")
    private Integer version;

    @Size(max = 50)
    @Comment("内容标识")
    @ColumnDefault("'0'")
    @Column(name = "content_id", length = 50)
    private String contentId;

    @Size(max = 50)
    @Comment("内容检查标识")
    @ColumnDefault("'1'")
    @Column(name = "check_version", length = 50)
    private String checkVersion;

}
