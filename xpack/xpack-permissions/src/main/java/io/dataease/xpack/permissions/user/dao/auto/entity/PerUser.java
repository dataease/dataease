package io.dataease.xpack.permissions.user.dao.auto.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author fit2cloud
 * @since 2023-04-12
 */
@TableName("per_user")
public class PerUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 账号
     */
    private String account;

    private String pwd;

    /**
     * 名称
     */
    private String name;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机前缀
     */
    private String phonePrefix;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 启用
     */
    private Boolean enable;

    /**
     * 来源
     */
    private Integer origin;

    /**
     * 创建人ID
     */
    private Long creatorId;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 语言
     */
    private String language;

    /**
     * 默认组织ID
     */
    private Long defaultOid;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhonePrefix() {
        return phonePrefix;
    }

    public void setPhonePrefix(String phonePrefix) {
        this.phonePrefix = phonePrefix;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public Integer getOrigin() {
        return origin;
    }

    public void setOrigin(Integer origin) {
        this.origin = origin;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Long getDefaultOid() {
        return defaultOid;
    }

    public void setDefaultOid(Long defaultOid) {
        this.defaultOid = defaultOid;
    }

    @Override
    public String toString() {
        return "PerUser{" +
        "id = " + id +
        ", account = " + account +
        ", pwd = " + pwd +
        ", name = " + name +
        ", email = " + email +
        ", phonePrefix = " + phonePrefix +
        ", phone = " + phone +
        ", enable = " + enable +
        ", origin = " + origin +
        ", creatorId = " + creatorId +
        ", createTime = " + createTime +
        ", language = " + language +
        ", defaultOid = " + defaultOid +
        "}";
    }
}
