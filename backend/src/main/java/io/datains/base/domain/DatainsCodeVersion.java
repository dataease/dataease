package io.datains.base.domain;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Data
public class DatainsCodeVersion implements Serializable {
    private Integer installedRank;

    private String description;

    private Date installedOn;

    private Boolean success;

    private static final long serialVersionUID = 1L;
}