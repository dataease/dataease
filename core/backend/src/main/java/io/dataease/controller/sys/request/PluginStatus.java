package io.dataease.controller.sys.request;


import lombok.Data;

@Data
public class PluginStatus {

    private Long pluginId;

    private Boolean status;
}
