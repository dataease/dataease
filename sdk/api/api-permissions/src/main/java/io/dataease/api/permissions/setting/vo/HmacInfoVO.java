package io.dataease.api.permissions.setting.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HmacInfoVO {
    private int version = 2;
    private boolean enabled;
}
