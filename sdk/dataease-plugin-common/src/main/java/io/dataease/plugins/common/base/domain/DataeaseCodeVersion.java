package io.dataease.plugins.common.base.domain;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Data
public class DataeaseCodeVersion implements Serializable {
    private Integer installedRank;

    private String description;

    private Date installedOn;

    private Boolean success;

    private static final long serialVersionUID = 1L;
}