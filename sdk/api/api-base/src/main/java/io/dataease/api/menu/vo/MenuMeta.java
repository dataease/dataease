package io.dataease.api.menu.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class MenuMeta implements Serializable {
    @Serial
    private static final long serialVersionUID = 1173571493163976261L;

    private String title;

    private String icon;

}
