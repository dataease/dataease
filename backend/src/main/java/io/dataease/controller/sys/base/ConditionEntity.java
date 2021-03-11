package io.dataease.controller.sys.base;

import lombok.Data;

import java.io.Serializable;

@Data
public class ConditionEntity implements Serializable {

    private String field;

    private String operator;

    private Object value;

}
