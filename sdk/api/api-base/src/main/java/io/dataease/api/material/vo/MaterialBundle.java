package io.dataease.api.material.vo;

import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class MaterialBundle {
    private int version = 1;
    private java.util.Map<String, String> referenceMap = new java.util.LinkedHashMap<>();
    private List<MaterialBundleEntry> files = new ArrayList<>();
}
