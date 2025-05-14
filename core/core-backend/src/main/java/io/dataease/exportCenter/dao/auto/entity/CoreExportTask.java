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

    @Comment("导出来源ID")
    @Column(name = "export_from")
    private Long exportFrom;

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Double getFileSize() {
        return fileSize;
    }

    public void setFileSize(Double fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileSizeUnit() {
        return fileSizeUnit;
    }

    public void setFileSizeUnit(String fileSizeUnit) {
        this.fileSizeUnit = fileSizeUnit;
    }

    public String getExportFrom() {
        return exportFrom;
    }

    public void setExportFrom(String exportFrom) {
        this.exportFrom = exportFrom;
    }

    public String getExportStatus() {
        return exportStatus;
    }

    public void setExportStatus(String exportStatus) {
        this.exportStatus = exportStatus;
    }

    public String getExportFromType() {
        return exportFromType;
    }

    public void setExportFromType(String exportFromType) {
        this.exportFromType = exportFromType;
    }

    public Long getExportTime() {
        return exportTime;
    }

    public void setExportTime(Long exportTime) {
        this.exportTime = exportTime;
    }

    public String getExportProgress() {
        return exportProgress;
    }

    public void setExportProgress(String exportProgress) {
        this.exportProgress = exportProgress;
    }

    public String getExportMachineName() {
        return exportMachineName;
    }

    public void setExportMachineName(String exportMachineName) {
        this.exportMachineName = exportMachineName;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "CoreExportTask{" +
        "id = " + id +
        ", userId = " + userId +
        ", fileName = " + fileName +
        ", fileSize = " + fileSize +
        ", fileSizeUnit = " + fileSizeUnit +
        ", exportFrom = " + exportFrom +
        ", exportStatus = " + exportStatus +
        ", exportFromType = " + exportFromType +
        ", exportTime = " + exportTime +
        ", exportProgress = " + exportProgress +
        ", exportMachineName = " + exportMachineName +
        ", params = " + params +
        ", msg = " + msg +
        "}";
    }
}
