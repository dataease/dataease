package io.dataease.visualization.dao.auto.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@Comment("跳转目标仪表板视图字段配置表")
@Entity
@Table(name = "visualization_link_jump_target_view_info")
public class VisualizationLinkJumpTargetViewInfo {
    @Id
    @Comment("主键")
    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Comment("visualization_link_jump_info 表的 ID")
    @Column(name = "link_jump_info_id")
    private Long linkJumpInfoId;

    @Comment("勾选字段设置的匹配字段，也可以不是勾选字段本身")
    @Column(name = "source_field_active_id")
    private Long sourceFieldActiveId;

    @Size(max = 50)
    @Comment("目标图表ID")
    @Column(name = "target_view_id", length = 50)
    private String targetViewId;

    @Size(max = 50)
    @Comment("目标字段ID")
    @Column(name = "target_field_id", length = 50)
    private String targetFieldId;

    @Comment("复制来源")
    @Column(name = "copy_from")
    private Long copyFrom;

    @Comment("复制来源ID")
    @Column(name = "copy_id")
    private Long copyId;

    @Size(max = 50)
    @Comment("联动目标类型 view 图表 filter 过滤组件 outParams 外部参数")
    @ColumnDefault("'view'")
    @Column(name = "target_type", length = 50)
    private String targetType;

}
