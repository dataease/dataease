package io.dataease.extensions.datafilling.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class XpackPluginsDfVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 5059944608544058565L;

    private Long id;

    private String icon;

    private String category;

    private String type;

    private Integer flag;
}
