package io.dataease.dao.auto.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@Entity
@Comment("定时报告过自定义过滤组件信息")
@Table(name = "visualization_report_filter")
public class VisualizationReportFilter {
    @Id
    @NotNull
    @Comment("id")
    @Column(name = "id", nullable = false)
    private Long id;

    @Comment("定时报告id")
    @Column(name = "report_id")
    private Long reportId;

    @Comment("任务id")
    @Column(name = "task_id")
    private Long taskId;

    @Comment("资源id")
    @Column(name = "resource_id")
    private Long resourceId;

    @Size(max = 255)
    @Comment("资源类型")
    @Column(name = "dv_type")
    private String dvType;

    @Comment("组件id")
    @Column(name = "component_id")
    private Long componentId;

    @Comment("过滤项id")
    @Column(name = "filter_id")
    private Long filterId;

    @Comment("过滤组件内容")
    @Column(name = "filter_info", length = 16777216)
    private String filterInfo;

    @Comment("过滤组件版本")
    @Column(name = "filter_version")
    private Integer filterVersion;

    @Comment("创建时间")
    @Column(name = "create_time")
    private Long createTime;

    @Size(max = 255)
    @Comment("创建人")
    @Column(name = "create_user")
    private String createUser;

}
