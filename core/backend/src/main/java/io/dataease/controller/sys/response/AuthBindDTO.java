package io.dataease.controller.sys.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class AuthBindDTO implements Serializable {

    private Boolean wecomBound = false;

    private Boolean dingtalkBound = false;

    private Boolean larkBound = false;

    private Boolean larksuiteBound = false;
}
