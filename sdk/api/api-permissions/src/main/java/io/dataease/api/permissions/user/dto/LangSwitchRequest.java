package io.dataease.api.permissions.user.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class LangSwitchRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -6779697711311519431L;


    private String lang;
}
