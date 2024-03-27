package io.dataease.controller.request.datafill;

import io.dataease.dto.datafill.DataFillTaskDTO;
import lombok.Data;
import lombok.experimental.Accessors;


@Data
@Accessors(chain = true)
public class DataFillTaskSearchRequest extends DataFillTaskDTO {

    private static final long serialVersionUID = 5881604308639714955L;

}
