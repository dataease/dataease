package io.dataease.datasource.dao.auto.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 驱动
 * </p>
 *
 * @author fit2cloud
 * @since 2023-04-18
 */
@TableName("core_driver")
public class CoreDriver implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 创健时间
     */
    private Long createTime;

    /**
     * 数据源类型
     */
    private String type;

    /**
     * 驱动类
     */
    private String driverClass;

    /**
     * 描述
     */
    private String describe;

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

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    @Override
    public String toString() {
        return "CoreDriver{" +
        "id = " + id +
        ", name = " + name +
        ", createTime = " + createTime +
        ", type = " + type +
        ", driverClass = " + driverClass +
        ", describe = " + describe +
        "}";
    }
}
