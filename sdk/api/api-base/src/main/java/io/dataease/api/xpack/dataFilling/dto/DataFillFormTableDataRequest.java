package io.dataease.api.xpack.dataFilling.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;


@Data
@Accessors(chain = true)
public class DataFillFormTableDataRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -314618516232771747L;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private long currentPage;

    private long pageSize;

    private boolean withoutLogs = false;

    private List<String> primaryKeyValueList;

    private List<DataFillFormTableDataSearchParam> searchParams;

}
