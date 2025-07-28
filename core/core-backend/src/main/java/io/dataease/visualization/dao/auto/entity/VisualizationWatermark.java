package io.dataease.visualization.dao.auto.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@Comment("仪表板水印设置表")
@Entity
@Table(name = "visualization_watermark")
public class VisualizationWatermark {
    @Id
    @Size(max = 50)
    @Comment("主键")
    @Column(name = "id", nullable = false, length = 50)
    private String id;

    @Size(max = 255)
    @Comment("版本号")
    @Column(name = "version")
    private String version;

    @Column(name = "setting_content", length = 16777216)
    private String settingContent;

    @Size(max = 255)
    @Comment("创建人")
    @Column(name = "create_by")
    private String createBy;

    @Comment("创建时间")
    @Column(name = "create_time")
    private Long createTime;

    public VisualizationWatermark(String id, String version, String settingContent, String createBy, Long createTime) {
        this.id = id;
        this.version = version;
        this.settingContent = settingContent;
        this.createBy = createBy;
        this.createTime = createTime;
    }

    // 无参构造函数，JPA 需要
    public VisualizationWatermark() {
    }
}
