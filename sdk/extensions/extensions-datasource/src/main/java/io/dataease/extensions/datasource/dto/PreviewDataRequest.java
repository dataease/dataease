package io.dataease.extensions.datasource.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class PreviewDataRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -123932000705752358L;

    @JsonSerialize(using = ToStringSerializer.class)
    private String id;

    private String table;
}
