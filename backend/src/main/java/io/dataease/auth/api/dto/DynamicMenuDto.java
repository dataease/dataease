package io.dataease.auth.api.dto;

import lombok.Data;


import java.io.Serializable;
import java.util.List;

@Data
public class DynamicMenuDto implements Serializable {

    private String path;

    private String component;

    private String redirect;

    private String name;

    private MenuMeta meta;

    private Long pid;

    private Long id;

    private List<DynamicMenuDto> children;

}
