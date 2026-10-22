package io.dataease.api.material.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MaterialBundleRequest extends MaterialContext {
    private String canvasStyleData;
    private String componentData;
}
