package io.dataease.xpack.permissions.auth.dao.ext.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class ResourcePO implements Serializable {

    private Integer type;

    private Long id;
}
