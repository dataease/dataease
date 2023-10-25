package io.dataease.controller.sys.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class OnlineMapKeyRequest implements Serializable {
    public String key;
}
