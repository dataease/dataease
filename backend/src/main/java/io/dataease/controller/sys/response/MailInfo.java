package io.dataease.controller.sys.response;

import java.io.Serializable;

import lombok.Data;

@Data
public class MailInfo implements Serializable{

    private String host;
    private String port;
    private String account;
    private String password;
    private String ssl;
    private String tls;
    private String recipient;
    
}
