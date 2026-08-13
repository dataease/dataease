package io.dataease.api.free.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class FreeRelationRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -316612770549936486L;

    private Long id;
}
