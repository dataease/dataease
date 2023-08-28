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
public class XpackShareVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 7364165756855382682L;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private Long exp;
    private String uuid;
    private String pwd;
}
