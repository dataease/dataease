package io.dataease.xpack.permissions.auth.dao.auto.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author fit2cloud
 * @since 2023-06-02
 */
@TableName("per_busi_resource")
public class PerBusiResource implements Serializable {

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
     * 类型ID
     */
    private Integer rtId;

    /**
     * 所属组织ID
     */
    private Long orgId;

    /**
     * 上级资源ID
     */
    private Long pid;

    /**
     * 寻根路径
     */
    private String rootWay;

    /**
     * 叶子结点
     */
    private Boolean leaf;

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

    public Integer getRtId() {
        return rtId;
    }

    public void setRtId(Integer rtId) {
        this.rtId = rtId;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
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

    public Boolean getLeaf() {
        return leaf;
    }

    public void setLeaf(Boolean leaf) {
        this.leaf = leaf;
    }

    @Override
    public String toString() {
        return "PerBusiResource{" +
        "id = " + id +
        ", name = " + name +
        ", rtId = " + rtId +
        ", orgId = " + orgId +
        ", pid = " + pid +
        ", rootWay = " + rootWay +
        ", leaf = " + leaf +
        "}";
    }
}
