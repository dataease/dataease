package io.dataease.startup.dao.auto.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 项目启动任务
 * </p>
 *
 * @author fit2cloud
 * @since 2024-05-15
 */
@TableName("core_sys_startup_job")
public class CoreSysStartupJob implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private String id;

    /**
     * 任务名称
     */
    private String name;

    /**
     * 任务状态
     */
    private String status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "CoreSysStartupJob{" +
        "id = " + id +
        ", name = " + name +
        ", status = " + status +
        "}";
    }
}
