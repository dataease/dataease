package io.dataease.api.commons;


import lombok.Data;

import java.io.Serializable;

@Data
public class BaseRspModel implements Serializable {

    private Boolean success = true;

    private String requestId;

    private Object responseInfo;


}
