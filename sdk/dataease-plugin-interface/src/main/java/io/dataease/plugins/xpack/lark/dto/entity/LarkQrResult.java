package io.dataease.plugins.xpack.lark.dto.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class LarkQrResult implements Serializable {

    private String client_id;
    private String redirect_uri;
    private String state;
}
