package io.dataease.plugins.xpack.proxy.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ProxyInfo implements Serializable {

    private String httpProxy;

    private String proxyAccount;

    private String proxyPwd;
}
