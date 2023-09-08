package io.dataease.plugins.xpack.lark.dto.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class LarkInfo implements Serializable {

    private String app_id;

    private String app_secret;

    private String redirect_uri;

    private String open;
}
