package io.dataease.api.visualization.vo;

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
public class VisualizationResourceVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 627770173259978185L;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long resourceId;

    private String name;

    private String type;

    private String creator;

    private String lastEditor;

    private Long lastEditTime;

    private int weight;
}
