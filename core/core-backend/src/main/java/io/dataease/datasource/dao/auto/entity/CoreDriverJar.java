package io.dataease.datasource.dao.auto.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 驱动详情
 * </p>
 *
 * @author fit2cloud
 * @since 2023-03-22
 */
@TableName("core_driver_jar")
public class CoreDriverJar implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private String id;

    /**
     * 驱动主键
     */
    private String deDriverId;

    /**
     * 名称
     */
    private String fileName;

    /**
     * 版本
     */
    private String version;

    /**
     * 驱动类
     */
    private String driverClass;

    private String transName;

    private Boolean isTransName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDeDriverId() {
        return deDriverId;
    }

    public void setDeDriverId(String deDriverId) {
        this.deDriverId = deDriverId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }

    public String getTransName() {
        return transName;
    }

    public void setTransName(String transName) {
        this.transName = transName;
    }

    public Boolean getIsTransName() {
        return isTransName;
    }

    public void setIsTransName(Boolean isTransName) {
        this.isTransName = isTransName;
    }

    @Override
    public String toString() {
        return "CoreDriverJar{" +
        "id = " + id +
        ", deDriverId = " + deDriverId +
        ", fileName = " + fileName +
        ", version = " + version +
        ", driverClass = " + driverClass +
        ", transName = " + transName +
        ", isTransName = " + isTransName +
        "}";
    }
}
