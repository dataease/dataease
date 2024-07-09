package io.dataease.copilot.dao.auto.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author fit2cloud
 * @since 2024-07-08
 */
@TableName("core_copilot_config")
public class CoreCopilotConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long id;

    private String copilotUrl;

    private String username;

    private String pwd;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCopilotUrl() {
        return copilotUrl;
    }

    public void setCopilotUrl(String copilotUrl) {
        this.copilotUrl = copilotUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    @Override
    public String toString() {
        return "CoreCopilotConfig{" +
        "id = " + id +
        ", copilotUrl = " + copilotUrl +
        ", username = " + username +
        ", pwd = " + pwd +
        "}";
    }
}
