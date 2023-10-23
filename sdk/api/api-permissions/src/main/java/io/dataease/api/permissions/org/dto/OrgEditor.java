package io.dataease.api.permissions.org.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class OrgEditor implements Serializable {

    @Serial
    private static final long serialVersionUID = -5571486179570725994L;

    private Long id;

    private String name;
}
