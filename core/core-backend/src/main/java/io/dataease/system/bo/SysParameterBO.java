package io.dataease.system.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class SysParameterBO implements Serializable {

    private String key;

    private String val;

    private String type;

    private String sort;
}
