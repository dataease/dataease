package io.dataease.controller.sys.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class RoleUserItem implements Serializable {

    private Long id;

    private String name;
}
