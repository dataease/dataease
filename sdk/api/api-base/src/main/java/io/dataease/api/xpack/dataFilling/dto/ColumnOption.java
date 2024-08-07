package io.dataease.api.xpack.dataFilling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ColumnOption implements Serializable {

    @Serial
    private static final long serialVersionUID = -422787778573500470L;

    private String name;

    private Object value;
}
