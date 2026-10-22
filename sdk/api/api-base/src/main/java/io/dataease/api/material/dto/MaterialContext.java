package io.dataease.api.material.dto;

import lombok.Getter;
import lombok.Setter;

/** The resource is resolved and authorized on the server; never trust client-supplied content. */
@Getter
@Setter
public class MaterialContext {
    private Long resourceId;
    private Long parentId;
    private String resourceType;
    private String resourceTable = "core";
    private String templateId;
    private String sourceTemplateId;
}
