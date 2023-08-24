package io.dataease.plugins.entity;

import io.dataease.plugins.common.base.domain.MyPlugin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PluginOperate implements Serializable {

    private String type;

    private MyPlugin plugin;

    private String senderIp;
}
