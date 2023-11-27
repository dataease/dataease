package io.dataease.api.permissions.setting.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class PerSettingItemVO implements Serializable {

    private String pkey;

    private String pval;

    private String type;

    private Integer sort;
}
