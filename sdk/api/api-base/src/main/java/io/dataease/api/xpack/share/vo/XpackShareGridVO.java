package io.dataease.api.xpack.share.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class XpackShareGridVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -1633588323141385486L;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long shareId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long resourceId;

    private String name;

    private String creator;

    private Long time;

    private Long exp;

}
