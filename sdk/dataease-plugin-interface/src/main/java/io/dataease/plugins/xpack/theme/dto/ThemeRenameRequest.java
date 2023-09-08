package io.dataease.plugins.xpack.theme.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ThemeRenameRequest implements Serializable {

    private Integer id;

    private String name;
}
