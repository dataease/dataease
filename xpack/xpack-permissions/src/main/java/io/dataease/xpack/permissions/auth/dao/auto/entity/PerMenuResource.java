package io.dataease.xpack.permissions.auth.dao.auto.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author fit2cloud
 * @since 2023-04-18
 */
@TableName("per_menu_resource")
public class PerMenuResource implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 资源ID
     */
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 上级资源ID
     */
    private Long pid;

    /**
     * 寻根路径
     */
    private String rootWay;

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

    public String getRootWay() {
        return rootWay;
    }

    public void setRootWay(String rootWay) {
        this.rootWay = rootWay;
    }

    @Override
    public String toString() {
        return "PerMenuResource{" +
        "id = " + id +
        ", name = " + name +
        ", pid = " + pid +
        ", rootWay = " + rootWay +
        "}";
    }
}
