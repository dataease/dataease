package io.dataease.api.permissions.auth.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class BusiResourceEditor implements Serializable {

    @Serial
    private static final long serialVersionUID = 5193320462388120821L;
    private Long id;
    private Long pid;
    private String name;
    private String flag;
    private int extraFlag;
}
