package io.dataease.exportCenter.dao.auto.entity;

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
@Comment("下载任务列表")
@Entity
@Table(name = "core_export_download_task")
public class CoreExportDownloadTask {
    @Id
    @Size(max = 255)
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "create_time")
    private Long createTime;

    @Column(name = "valid_time")
    private Long validTime;

}
