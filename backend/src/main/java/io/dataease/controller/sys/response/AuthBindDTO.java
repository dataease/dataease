package io.dataease.controller.sys.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class AuthBindDTO implements Serializable {

    private Boolean wecomBinded = false;

    private Boolean dingtalkBinded = false;

    private Boolean larkBinded = false;
}
