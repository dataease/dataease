package io.dataease.xpack.permissions.org.dao.auto.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author fit2cloud
 * @since 2023-03-02
 */
@TableName("per_org")
public class PerOrg implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 组织ID
     */
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 上级组织
     */
    private Long pid;

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

    public Long getPid() {
        return pid;
    }

    public void setPid(Long pid) {
        this.pid = pid;
    }

    @Override
    public String toString() {
        return "PerOrg{" +
        "id = " + id +
        ", name = " + name +
        ", pid = " + pid +
        "}";
    }
}
