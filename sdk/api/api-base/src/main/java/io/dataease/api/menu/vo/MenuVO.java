package io.dataease.api.menu.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class MenuVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -2515621871016825978L;

    private String path;

    private String component;

    private boolean hidden;

    private boolean isPlugin;

    private String name;

    private boolean inLayout;

    private String redirect;

    private MenuMeta meta;

    private List<MenuVO> children;
}
