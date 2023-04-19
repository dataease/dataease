package io.dataease.xpack.permissions.auth.dao.ext.entity;


import lombok.Data;

import java.io.Serializable;

@Data
public class BusiResourcePO implements Serializable {

    private Long id;

    private String name;

    private Long pid;
}
