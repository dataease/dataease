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
@TableName("per_auth_menu")
public class PerAuthMenu implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 授权ID
     */
    private Long id;

    /**
     * 角色ID
     */
    private Long rid;

    /**
     * 资源ID
     */
    private Long resourceId;

    /**
     * 权重0无1查看2授权
     */
    private Integer weight;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRid() {
        return rid;
    }

    public void setRid(Long rid) {
        this.rid = rid;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "PerAuthMenu{" +
        "id = " + id +
        ", rid = " + rid +
        ", resourceId = " + resourceId +
        ", weight = " + weight +
        "}";
    }
}
