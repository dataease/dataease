package io.dataease.dto;

import io.dataease.plugins.common.base.domain.DeDriver;
import lombok.Data;

@Data
public class DriverDTO extends DeDriver {
    private String typeDesc;
}
