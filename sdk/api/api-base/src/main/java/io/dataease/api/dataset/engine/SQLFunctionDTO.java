package io.dataease.api.dataset.engine;

import lombok.Data;

/**
 * @Author Junjun
 */
@Data
public class SQLFunctionDTO {
    private String name;
    private String func;
    private int type;
    private String desc;
    private boolean isCustom;
}
