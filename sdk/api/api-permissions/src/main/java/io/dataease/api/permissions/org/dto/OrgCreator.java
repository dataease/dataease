package io.dataease.api.permissions.org.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class OrgCreator implements Serializable {

    @Serial
    private static final long serialVersionUID = -4246980891732805368L;

    private String name;
    private Long pid;
}
