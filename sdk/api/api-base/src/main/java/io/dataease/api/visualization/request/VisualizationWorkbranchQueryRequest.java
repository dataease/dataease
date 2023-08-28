package io.dataease.api.visualization.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VisualizationWorkbranchQueryRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -3522243514336261778L;

    private String type;

    private String keyword;

    private boolean asc = false;
}
