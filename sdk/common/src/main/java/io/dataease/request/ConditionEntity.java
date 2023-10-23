package io.dataease.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class ConditionEntity implements Serializable {

    private String field;

    private String operator;

    private Object value;
}
