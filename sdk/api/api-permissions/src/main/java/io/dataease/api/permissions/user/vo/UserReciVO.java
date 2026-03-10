package io.dataease.api.permissions.user.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserReciVO implements Serializable {

    private Long userId;

    private List<Long> roleIds;

    private boolean hasRootRole;

    private List<Long> commonRoleIds;

    public UserReciVO(Long userId) {
        this.userId = userId;
    }
}
