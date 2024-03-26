package io.dataease.controller.request.datafill;

import io.dataease.dto.datafill.DataFillUserTaskDTO;
import lombok.Data;
import lombok.experimental.Accessors;


@Data
@Accessors(chain = true)
public class DataFillUserTaskSearchRequest extends DataFillUserTaskDTO {

    private static final long serialVersionUID = 5881604308639714955L;

}
