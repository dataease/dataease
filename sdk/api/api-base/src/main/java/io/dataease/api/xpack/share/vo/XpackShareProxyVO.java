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
public class XpackShareProxyVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 6520351268758198483L;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long resourceId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long uid;

    private boolean exp;

    private boolean pwdValid;
}
