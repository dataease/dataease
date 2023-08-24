package io.dataease.plugins.xpack.theme.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ThemeCreateRequest implements Serializable {

    private String name;

    private Integer copyId;
}
