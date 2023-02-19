package io.dataease.api.permissions.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class AuthDTO implements Serializable {

    private Integer model;

    private List<Long> resourceId;

    public boolean isSimple() {
        return model == 0;
    }

    public boolean isStandalone() {
        return model == 1;
    }

    public boolean isDistributed() {
        return model == 2;
    }
}
