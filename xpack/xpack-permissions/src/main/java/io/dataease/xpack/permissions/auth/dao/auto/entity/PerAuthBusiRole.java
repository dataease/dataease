package io.dataease.xpack.permissions.auth.dao.auto.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author fit2cloud
 * @since 2023-04-19
 */
@TableName("per_auth_busi_role")
public class PerAuthBusiRole implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 授权ID
     */
    private Long id;

    /**
     * 目标ID
     */
    private Long rId;

    /**
     * 资源ID
     */
    private Long resourceId;

    /**
     * 资源类型
     */
    private Integer resourceType;

    /**
     * 权重
     */
    private Integer weight;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getrId() {
        return rId;
    }

    public void setrId(Long rId) {
        this.rId = rId;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public Integer getResourceType() {
        return resourceType;
    }

    public void setResourceType(Integer resourceType) {
        this.resourceType = resourceType;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "PerAuthBusiRole{" +
        "id = " + id +
        ", rId = " + rId +
        ", resourceId = " + resourceId +
        ", resourceType = " + resourceType +
        ", weight = " + weight +
        "}";
    }
}
