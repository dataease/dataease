package io.dataease.api.permissions.auth.dto;

import lombok.Data;

@Data
public class BusiTargetPerCreator extends MenuTargetPerCreator{

    private Integer type;
    private String flag;
}
