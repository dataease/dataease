package io.dataease.xpack.permissions.user.dao.ext.entity;

import io.dataease.api.permissions.user.vo.UserGridRoleItem;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UserRolePO implements Serializable {

    @Serial
    private static final long serialVersionUID = 7364771864462506805L;
    private Long uid;

    private Long rid;

    private String rname;

    public UserGridRoleItem convert() {
        UserGridRoleItem roleItem = new UserGridRoleItem();
        roleItem.setId(rid);
        roleItem.setName(rname);
        return roleItem;
    }
}
