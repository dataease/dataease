package io.dataease.plugins.common.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class SysBackgroundImage implements Serializable {
    private String id;

    private String name;

    private String classification;

    private String remark;

    private Integer sort;

    private Long uploadTime;

    private String baseUrl;

    private String url;

    private String content;

    private static final long serialVersionUID = 1L;
}