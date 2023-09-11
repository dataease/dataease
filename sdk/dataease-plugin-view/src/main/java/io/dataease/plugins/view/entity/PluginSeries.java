package io.dataease.plugins.view.entity;

import lombok.Data;

import java.util.List;

@Data
public class PluginSeries {

    private String name;
    private String type;
    private List<Object> data;
}
