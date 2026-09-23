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
public class DataFillFormTableDataBaseRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -3381050774806384242L;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private boolean withoutLogs = true;

    private List<DataFillFormTableDataSearchParam> searchParams;

}
