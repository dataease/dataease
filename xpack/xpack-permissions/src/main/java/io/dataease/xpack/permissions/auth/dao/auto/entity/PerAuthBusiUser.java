package io.dataease.xpack.permissions.auth.dao.auto.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author fit2cloud
 * @since 2023-04-11
 */
@TableName("per_auth_busi_user")
public class PerAuthBusiUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 授权ID
     */
    private Long id;

    /**
     * 目标ID
     */
    private Long uId;

    /**
     * 资源ID
     */
    private Long resourceId;

    /**
     * 资源类型
     */
    private Integer resourceType;

    /**
     * 可读
     */
    private Boolean read;

    /**
     * 可编辑
     */
    private Boolean manage;

    /**
     * 导出
     */
    private Boolean export;

    /**
     * 授权
     */
    private Boolean auth;

    /**
     * 预留操作
     */
    private Boolean extOpt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getuId() {
        return uId;
    }

    public void setuId(Long uId) {
        this.uId = uId;
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

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public Boolean getManage() {
        return manage;
    }

    public void setManage(Boolean manage) {
        this.manage = manage;
    }

    public Boolean getExport() {
        return export;
    }

    public void setExport(Boolean export) {
        this.export = export;
    }

    public Boolean getAuth() {
        return auth;
    }

    public void setAuth(Boolean auth) {
        this.auth = auth;
    }

    public Boolean getExtOpt() {
        return extOpt;
    }

    public void setExtOpt(Boolean extOpt) {
        this.extOpt = extOpt;
    }

    @Override
    public String toString() {
        return "PerAuthBusiUser{" +
        "id = " + id +
        ", uId = " + uId +
        ", resourceId = " + resourceId +
        ", resourceType = " + resourceType +
        ", read = " + read +
        ", manage = " + manage +
        ", export = " + export +
        ", auth = " + auth +
        ", extOpt = " + extOpt +
        "}";
    }
}
