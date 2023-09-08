package io.dataease.plugins.xpack.cas.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CasSaveResult implements Serializable {

    private Boolean needLogout;

    private Boolean casEnable;
}
