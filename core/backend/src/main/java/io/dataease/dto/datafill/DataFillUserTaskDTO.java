package io.dataease.dto.datafill;

import io.dataease.plugins.common.base.domain.DataFillUserTask;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataFillUserTaskDTO extends DataFillUserTask {

    private static final long serialVersionUID = 3610753131867651856L;

    private String taskName;
    private String formName;
    private Long creator;
    private String creatorName;

}
