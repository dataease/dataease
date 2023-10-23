package io.dataease.api.visualization.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VisualizationStoreRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 166667337997303748L;

    private Long id;

    private String type;
}
