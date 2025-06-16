package io.dataease.exportCenter.dao.auto.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 下载任务列表
 * </p>
 *
 * @author fit2cloud
 * @since 2025-06-16
 */
@TableName("core_export_download_task")
public class CoreExportDownloadTask implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private Long createTime;

    private Long validTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getValidTime() {
        return validTime;
    }

    public void setValidTime(Long validTime) {
        this.validTime = validTime;
    }

    @Override
    public String toString() {
        return "CoreExportDownloadTask{" +
        "id = " + id +
        ", createTime = " + createTime +
        ", validTime = " + validTime +
        "}";
    }
}
