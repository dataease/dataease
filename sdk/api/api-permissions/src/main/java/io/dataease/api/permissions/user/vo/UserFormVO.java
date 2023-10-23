package io.dataease.api.permissions.user.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class UserFormVO implements Serializable {

    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;

    private String account;

    private String name;

    private List<String> roleIds;

    private String email;

    private Boolean enable;

    private String phonePrefix;

    private String phone;
}
