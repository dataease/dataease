package io.dataease.share.dao.auto.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 公共链接
 * </p>
 *
 * @author fit2cloud
 * @since 2024-06-21
 */
@TableName("xpack_share")
public class XpackShare implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long id;

    /**
     * 创建人
     */
    private Long creator;

    /**
     * 创建时间
     */
    private Long time;

    /**
     * 过期时间
     */
    private Long exp;

    /**
     * uuid
     */
    private String uuid;

    /**
     * 密码
     */
    private String pwd;

    /**
     * 资源ID
     */
    private Long resourceId;

    /**
     * 组织ID
     */
    private Long oid;

    /**
     * 业务类型
     */
    private Integer type;

    /**
     * 自动生成密码
     */
    private Boolean autoPwd;

    /**
     * ticket必须
     */
    private Boolean ticketRequire;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCreator() {
        return creator;
    }

    public void setCreator(Long creator) {
        this.creator = creator;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Long getExp() {
        return exp;
    }

    public void setExp(Long exp) {
        this.exp = exp;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public Long getOid() {
        return oid;
    }

    public void setOid(Long oid) {
        this.oid = oid;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Boolean getAutoPwd() {
        return autoPwd;
    }

    public void setAutoPwd(Boolean autoPwd) {
        this.autoPwd = autoPwd;
    }

    public Boolean getTicketRequire() {
        return ticketRequire;
    }

    public void setTicketRequire(Boolean ticketRequire) {
        this.ticketRequire = ticketRequire;
    }

    @Override
    public String toString() {
        return "XpackShare{" +
        "id = " + id +
        ", creator = " + creator +
        ", time = " + time +
        ", exp = " + exp +
        ", uuid = " + uuid +
        ", pwd = " + pwd +
        ", resourceId = " + resourceId +
        ", oid = " + oid +
        ", type = " + type +
        ", autoPwd = " + autoPwd +
        ", ticketRequire = " + ticketRequire +
        "}";
    }
}
