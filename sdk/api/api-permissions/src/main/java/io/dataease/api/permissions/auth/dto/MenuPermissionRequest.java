package io.dataease.api.permissions.auth.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class MenuPermissionRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -7609671259840867561L;

    private Long id;

}
