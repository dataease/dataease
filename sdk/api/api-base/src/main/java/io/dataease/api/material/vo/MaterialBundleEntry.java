package io.dataease.api.material.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MaterialBundleEntry {
    private String reference;
    private String name;
    private String format;
    private String content;
}
