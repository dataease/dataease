package io.dataease.api.material.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MaterialUpdateRequest {
    private Long id;
    private String name;
    private Long categoryId;
    private boolean moveCategory;
}
