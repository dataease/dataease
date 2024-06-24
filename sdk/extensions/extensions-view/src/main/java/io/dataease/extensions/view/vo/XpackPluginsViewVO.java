package io.dataease.extensions.view.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Data
public class XpackPluginsViewVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 5059944608544058565L;

    private Long id;

    private String name;

    private String icon;

    private String category;

    private String title;

    private String chartValue;

    private String chartTitle;

    private String render;

    private Map<String, String> staticMap;

}
