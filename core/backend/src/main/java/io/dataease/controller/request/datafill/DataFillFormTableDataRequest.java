package io.dataease.controller.request.datafill;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;


@Data
@Accessors(chain = true)
public class DataFillFormTableDataRequest extends DataFillFormRequest {

    private static final long serialVersionUID = -314618516232771747L;

    private long currentPage;

    private long pageSize;

    private String primaryKeyValue;

    private List<String> primaryKeyValueList;

}
