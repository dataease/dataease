package io.dataease.security;

import org.apache.shiro.authc.UsernamePasswordToken;

public class MsUserToken extends UsernamePasswordToken {
    private String loginType;

    public MsUserToken() {
    }

    public MsUserToken(final String username, final String password, final String loginType) {
        super(username, password);
        this.loginType = loginType;
    }

    public String getLoginType() {
        return loginType;
    }

    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }

}
