package io.dataease.plugins.xpack.email.dto.request;

import java.io.Serializable;

import lombok.Data;

@Data
public class XpackEmailViewRequest implements Serializable{

    private String panelId;

    private String content;

    private String pixel;
    
}
