package io.dataease.plugins.xpack.theme.dto;

import lombok.Data;

import java.util.List;

@Data
public class ThemeBaseDTO extends ThemeDto{

    private List<ThemeItem> baseItems;
}
