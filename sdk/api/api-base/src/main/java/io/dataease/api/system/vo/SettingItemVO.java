package io.dataease.api.system.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class SettingItemVO implements Serializable {

    private String pkey;

    private String pval;

    private String type;

    private Integer sort;
}
