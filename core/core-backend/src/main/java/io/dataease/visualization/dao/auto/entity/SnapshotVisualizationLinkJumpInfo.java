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
@Entity
@Table(name = "snapshot_visualization_link_jump_info")
public class SnapshotVisualizationLinkJumpInfo {
    @Id
    @Comment("主键")
    @Column(name = "id", nullable = false)
    private Long id;

    @Comment("link jump ID")
    @Column(name = "link_jump_id")
    private Long linkJumpId;

    @Size(max = 255)
    @Comment("关联类型 inner 内部仪表板，outer 外部链接")
    @Column(name = "link_type")
    private String linkType;

    @Size(max = 255)
    @Comment("跳转类型 _blank 新开页面 _self 当前窗口")
    @Column(name = "jump_type")
    private String jumpType;

    @Comment("关联仪表板ID")
    @Column(name = "target_dv_id")
    private Long targetDvId;

    @Comment("字段ID")
    @Column(name = "source_field_id")
    private Long sourceFieldId;

    @Size(max = 4000)
    @Comment("内容 linkType = outer时使用")
    @Column(name = "content", length = 4000)
    private String content;

    @Comment("是否可用")
    @Column(name = "checked")
    private Boolean checked;

    @Comment("是否附加点击参数")
    @Column(name = "attach_params")
    private Boolean attachParams;

    @Comment("复制来源")
    @Column(name = "copy_from")
    private Long copyFrom;

    @Comment("复制来源ID")
    @Column(name = "copy_id")
    private Long copyId;

    @Size(max = 255)
    @Comment("窗口大小large middle small")
    @ColumnDefault("'middle'")
    @Column(name = "window_size")
    private String windowSize;

}
