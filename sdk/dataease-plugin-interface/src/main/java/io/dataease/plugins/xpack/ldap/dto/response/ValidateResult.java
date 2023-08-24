package io.dataease.plugins.xpack.ldap.dto.response;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class ValidateResult<T> implements Serializable {

    private boolean success;

    private T data;

    private String msg;

}
