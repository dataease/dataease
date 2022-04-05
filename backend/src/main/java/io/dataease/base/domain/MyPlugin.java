package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class MyPlugin implements Serializable {
    private Long pluginId;

    private String name;

    private String store;

    private Boolean free;

    private Integer cost;

    private String category;

    private String descript;

    private String version;

    private Integer installType;

    private String creator;

    private Boolean loadMybatis;

    private Long releaseTime;

    private Long installTime;

    private String moduleName;

    private String icon;

    private String require = "1.9.0";

    private static final long serialVersionUID = 1L;
}