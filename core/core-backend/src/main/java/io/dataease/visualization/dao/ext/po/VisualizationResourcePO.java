package io.dataease.visualization.dao.ext.po;

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
public class VisualizationResourcePO implements Serializable {
    @Serial
    private static final long serialVersionUID = 627770173259978185L;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long resourceId;

    private String name;

    private String type;

    private Long creator;

    private Long lastEditor;

    private Long lastEditTime;

    private Boolean favorite;

    private int weight;
}
