package io.dataease.copilot.dao.auto.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@Entity
@Table(name = "core_copilot_msg")
public class CoreCopilotMsg {
    @Id
    @Comment("ID")
    @Column(name = "id", nullable = false)
    private Long id;

    @Comment("用户ID")
    @Column(name = "user_id")
    private Long userId;

    @Comment("数据集ID")
    @Column(name = "dataset_group_id")
    private Long datasetGroupId;

    @Size(max = 255)
    @Comment("user or api")
    @Column(name = "msg_type")
    private String msgType;

    @Size(max = 255)
    @Comment("mysql oracle ...")
    @Column(name = "engine_type")
    private String engineType;

    @Column(name = "schema_sql", length = 16777216)
    private String schemaSql;

    @Column(name = "question", length = 16777216)
    private String question;

    @Column(name = "history", length = 16777216)
    private String history;

    @Column(name = "copilot_sql", length = 16777216)
    private String copilotSql;

    @Column(name = "api_msg", length = 16777216)
    private String apiMsg;

    @Comment("sql 状态")
    @Column(name = "sql_ok")
    private Integer sqlOk;

    @Comment("chart 状态")
    @Column(name = "chart_ok")
    private Integer chartOk;

    @Column(name = "chart", length = 16777216)
    private String chart;

    @Column(name = "chart_data", length = 16777216)
    private String chartData;

    @Column(name = "exec_sql", length = 16777216)
    private String execSql;

    @Comment("msg状态，0失败 1成功")
    @Column(name = "msg_status")
    private Integer msgStatus;

    @Column(name = "err_msg", length = 16777216)
    private String errMsg;

    @Comment("创建时间")
    @Column(name = "create_time")
    private Long createTime;

}
