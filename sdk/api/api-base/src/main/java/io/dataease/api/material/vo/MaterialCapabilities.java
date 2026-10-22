package io.dataease.api.material.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MaterialCapabilities {
    private boolean manage;
    private boolean available;

    public MaterialCapabilities(boolean manage) {
        this(manage, false);
    }
}
