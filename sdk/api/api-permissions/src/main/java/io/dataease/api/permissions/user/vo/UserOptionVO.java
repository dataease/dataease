package io.dataease.api.permissions.user.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserOptionVO extends UserItem implements Serializable {

    private String account;
}
