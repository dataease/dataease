package io.dataease.share.dao.auto.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author fit2cloud
 * @since 2024-06-21
 */
@TableName("core_share_ticket")
public class CoreShareTicket implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long id;

    /**
     * 分享uuid
     */
    private String uuid;

    /**
     * ticket
     */
    private String ticket;

    /**
     * ticket有效期
     */
    private Long exp;

    /**
     * ticket参数
     */
    private String args;

    /**
     * 首次访问时间
     */
    private Long accessTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public Long getExp() {
        return exp;
    }

    public void setExp(Long exp) {
        this.exp = exp;
    }

    public String getArgs() {
        return args;
    }

    public void setArgs(String args) {
        this.args = args;
    }

    public Long getAccessTime() {
        return accessTime;
    }

    public void setAccessTime(Long accessTime) {
        this.accessTime = accessTime;
    }

    @Override
    public String toString() {
        return "CoreShareTicket{" +
        "id = " + id +
        ", uuid = " + uuid +
        ", ticket = " + ticket +
        ", exp = " + exp +
        ", args = " + args +
        ", accessTime = " + accessTime +
        "}";
    }
}
