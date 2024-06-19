package io.dataease.api.xpack.plugin.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PluginVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -3889122930435272191L;

    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;

    private String name;

    private String flag;

    private String icon;

    private String version;

    private Long installTime;

    private String developer;
}
