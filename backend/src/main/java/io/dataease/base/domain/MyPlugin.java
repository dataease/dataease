package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class MyPlugin implements Serializable {
    private Long pluginId;

    private String name;

    private Boolean free;

    private Integer cost;

    private String descript;

    private String version;

    private Integer installType;

    private String creator;

    private Long releaseTime;

    private Long installTime;

    private String moduleName;

    private String beanName;

    private String icon;

    private static final long serialVersionUID = 1L;
}