package io.dataease.visualization.dao.auto.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@Entity
@Table(name = "snapshot_visualization_link_jump")
public class SnapshotVisualizationLinkJump {
    @Id
    @Comment("主键")
    @Column(name = "id", nullable = false)
    private Long id;

    @Comment("源仪表板ID")
    @Column(name = "source_dv_id")
    private Long sourceDvId;

    @Comment("源图表ID")
    @Column(name = "source_view_id")
    private Long sourceViewId;

    @Size(max = 4000)
    @Comment("跳转信息")
    @Column(name = "link_jump_info", length = 4000)
    private String linkJumpInfo;

    @Comment("是否启用")
    @Column(name = "checked")
    private Boolean checked;

    @Comment("复制来源")
    @Column(name = "copy_from")
    private Long copyFrom;

    @Comment("复制来源ID")
    @Column(name = "copy_id")
    private Long copyId;

}
