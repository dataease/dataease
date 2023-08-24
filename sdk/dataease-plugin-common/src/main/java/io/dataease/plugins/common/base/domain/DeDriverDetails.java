package io.dataease.plugins.common.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class DeDriverDetails implements Serializable {
    private String id;

    private String deDriverId;

    private String fileName;

    private String version;

    private String driverClass;

    private String transName;

    private Boolean isTransName;

    private static final long serialVersionUID = 1L;
}