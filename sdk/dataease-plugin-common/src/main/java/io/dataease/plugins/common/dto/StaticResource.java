package io.dataease.plugins.common.dto;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

@Data
public class StaticResource implements Serializable {

    private String name;
    private String suffix;

    public boolean match(String name, String suffix) {
        return StringUtils.isNotBlank(name) &&
                StringUtils.isNotBlank(suffix) &&
                StringUtils.equals(name, this.name) &&
                StringUtils.equals(suffix, this.suffix);
    }
}
