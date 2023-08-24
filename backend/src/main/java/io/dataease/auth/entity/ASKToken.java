package io.dataease.auth.entity;

import org.apache.shiro.authc.AuthenticationToken;

public class ASKToken implements AuthenticationToken {

    private String accessKey;

    private String signature;

    public ASKToken(String accessKey, String signature) {
        this.accessKey = accessKey;
        this.signature = signature;
    }

    @Override
    public Object getPrincipal() {
        return accessKey;
    }

    @Override
    public Object getCredentials() {
        return signature;
    }
}
