package io.dataease.exportCenter.dao.auto.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 导出任务表
 * </p>
 *
 * @author fit2cloud
 * @since 2024-06-12
 */
@TableName("core_export_task")
public class CoreExportTask implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private Long userId;

    private String fileName;

    private Double fileSize;

    private String fileSizeUnit;

    private String exportFrom;

    private String exportStatus;

    private String exportFromType;

    private Long exportTime;

    private String exportProgress;

    private String exportMachineName;

    /**
     * 过滤参数
     */
    private String params;

    /**
     * 错误信息
     */
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
