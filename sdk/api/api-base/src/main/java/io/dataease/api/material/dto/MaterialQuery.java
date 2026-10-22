package io.dataease.api.material.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MaterialQuery extends MaterialContext {
    private String keyword;
    private String format;
    private Long categoryId;
    private boolean preset;
    /** Include both system presets and custom materials in the selector. */
    private boolean allSources;
    private boolean management;
}
