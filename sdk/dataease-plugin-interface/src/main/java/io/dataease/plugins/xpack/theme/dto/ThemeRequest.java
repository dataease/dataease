package io.dataease.plugins.xpack.theme.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;


@Data
public class ThemeRequest implements Serializable{
    
    private Integer themeId;

    private Boolean clearFirst;

    private List<ThemeItem> themeItems;
}
