package io.dataease.visualization.dao.auto.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author fit2cloud
 * @since 2023-08-24
 */
@TableName("core_store")
public class CoreStore implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long id;

    /**
     * 资源ID
     */
    private Long resourceId;

    /**
     * 用户ID
     */
    private Long uid;

    /**
     * 资源类型
     */
    private Integer resourceType;

    /**
     * 收藏时间
     */
    private Long time;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public Integer getResourceType() {
        return resourceType;
    }

    public void setResourceType(Integer resourceType) {
        this.resourceType = resourceType;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "CoreStore{" +
        "id = " + id +
        ", resourceId = " + resourceId +
        ", uid = " + uid +
        ", resourceType = " + resourceType +
        ", time = " + time +
        "}";
    }
}
