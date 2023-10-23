package io.dataease.map.dao.auto.entity;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author fit2cloud
 * @since 2023-07-09
 */
public class Area implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 区域id,和文件对应
     */
    private String id;

    /**
     * 区域级别，从高到低country,province,city,district,更细的待定
     */
    private String level;

    /**
     * 区域名称
     */
    private String name;

    /**
     * 父级区域id
     */
    private String pid;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    @Override
    public String toString() {
        return "Area{" +
        "id = " + id +
        ", level = " + level +
        ", name = " + name +
        ", pid = " + pid +
        "}";
    }
}
