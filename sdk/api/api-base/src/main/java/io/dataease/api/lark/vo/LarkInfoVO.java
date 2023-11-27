package io.dataease.api.lark.vo;



import lombok.Data;

import java.io.Serializable;

@Data
public class LarkInfoVO implements Serializable {

    private String appId;

    private String appSecret;

    private String callBack;

    private Boolean enable = false;
}
