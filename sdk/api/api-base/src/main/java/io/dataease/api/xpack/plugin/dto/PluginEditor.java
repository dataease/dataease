package io.dataease.api.xpack.plugin.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class PluginEditor implements Serializable {
    @Serial
    private static final long serialVersionUID = -1793403914368070138L;

    private String id;
}
