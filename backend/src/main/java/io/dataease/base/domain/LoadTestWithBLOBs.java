package io.dataease.base.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class LoadTestWithBLOBs extends LoadTest implements Serializable {
    private String loadConfiguration;

    private String advancedConfiguration;

    private static final long serialVersionUID = 1L;
}
