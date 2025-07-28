package io.dataease.exportCenter.dao.auto.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@Comment("导出任务表")
@Entity
@Table(name = "core_export_task")
public class CoreExportTask {
    @Id
    @Size(max = 255)
    @Comment("主键")
    @Column(name = "id", nullable = false)
    private String id;

    @NotNull
    @Comment("用户ID")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Size(max = 2048)
    @Comment("文件名称")
    @Column(name = "file_name", length = 2048)
    private String fileName;

    @Comment("文件大小")
    @Column(name = "file_size")
    private Double fileSize;

    @Size(max = 255)
    @Comment("单位")
    @Column(name = "file_size_unit")
    private String fileSizeUnit;

    @Size(max = 255)
    @Comment("导出来源ID")
    @Column(name = "export_from")
    private String exportFrom;

    @Size(max = 255)
    @Comment("导出状态")
    @Column(name = "export_status")
    private String exportStatus;

    @Size(max = 255)
    @Comment("导出来源类型")
    @Column(name = "export_from_type")
    private String exportFromType;

    @Comment("导出时间")
    @Column(name = "export_time")
    private Long exportTime;

    @Size(max = 255)
    @Comment("导出进度")
    @Column(name = "export_progress")
    private String exportProgress;

    @Size(max = 512)
    @Comment("导出机器名称")
    @Column(name = "export_machine_name", length = 512)
    private String exportMachineName;

    @NotNull
    @Column(name = "params", nullable = false, length = 16777216)
    private String params;

    @Column(name = "msg", length = 16777216)
    private String msg;

}
