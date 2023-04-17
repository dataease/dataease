package io.dataease.api.permissions.user.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;

@Data
public class UserGridVO {

    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;

    private String account;

    private String name;

    private List<UserGridRoleItem> roleItems;

    private String email;

    private Boolean enable;

    private Long createTime;
}
