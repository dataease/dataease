package io.dataease.api.xpack.dataFilling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ExtraDetails implements Serializable {

    @Serial
    private static final long serialVersionUID = -7249780247687230185L;

    private String name;

    private Object value;
}
