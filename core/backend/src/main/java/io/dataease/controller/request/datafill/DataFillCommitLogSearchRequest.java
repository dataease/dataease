package io.dataease.controller.request.datafill;

import io.dataease.dto.datafill.DataFillCommitLogDTO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;


@Data
@Accessors(chain = true)
public class DataFillCommitLogSearchRequest extends DataFillCommitLogDTO {

    private static final long serialVersionUID = -1067572649791328116L;

    private List<String> formIds;

}
