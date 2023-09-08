package io.dataease.plugins.common.entity;

import io.dataease.plugins.common.annotation.PluginResultMap;
import lombok.Data;

import java.io.Serializable;

@Data
@PluginResultMap
public class XpackConditionEntity implements Serializable {

    private String field;

    private String operator;

    private Object value;
}
