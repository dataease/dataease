package io.dataease.xpack.permissions.user.dao.auto.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author fit2cloud
 * @since 2023-03-31
 */
@TableName("per_user_role")
public class PerUserRole implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关联ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long uid;

    /**
     * 角色ID
     */
    private Long rid;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public Long getRid() {
        return rid;
    }

    public void setRid(Long rid) {
        this.rid = rid;
    }

    @Override
    public String toString() {
        return "PerUserRole{" +
        "id = " + id +
        ", uid = " + uid +
        ", rid = " + rid +
        "}";
    }
}
