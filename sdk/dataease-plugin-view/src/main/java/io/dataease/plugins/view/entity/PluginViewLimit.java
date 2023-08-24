package io.dataease.plugins.view.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class PluginViewLimit implements Serializable {

    private String type;

    private String resultMode;

    private Integer resultCount;
}
