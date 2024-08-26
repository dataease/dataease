package io.dataease.font.dao.auto.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author fit2cloud
 * @since 2024-08-23
 */
@TableName("core_font")
public class CoreFont implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long id;

    /**
     * 字体名称
     */
    private String name;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件转换名称
     */
    private String fileTransName;

    /**
     * 是否默认
     */
    private Boolean isDefault;

    /**
     * 是否内置
     */
    private Boolean isBuiltin;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileTransName() {
        return fileTransName;
    }

    public void setFileTransName(String fileTransName) {
        this.fileTransName = fileTransName;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public Boolean getIsBuiltin() {
        return isBuiltin;
    }

    public void setIsBuiltin(Boolean isBuiltin) {
        this.isBuiltin = isBuiltin;
    }

    @Override
    public String toString() {
        return "CoreFont{" +
        "id = " + id +
        ", name = " + name +
        ", fileName = " + fileName +
        ", fileTransName = " + fileTransName +
        ", isDefault = " + isDefault +
        ", isBuiltin = " + isBuiltin +
        "}";
    }
}
