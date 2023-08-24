package io.dataease.commons.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class RedisMessage<T> implements Serializable {

    private String type;

    private T data;
}
