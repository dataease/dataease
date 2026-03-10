package io.dataease.api.permissions.dataset.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ColPermissionInfo implements Serializable {

    private boolean selected;

    private Long id;

    private String opt;

    private Object desensitizationRule;

}
