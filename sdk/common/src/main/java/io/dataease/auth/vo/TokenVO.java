package io.dataease.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Schema(description = "Token VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 564596240616527258L;

    @Schema(description = "token")
    private String token;

    @Schema(description = "有效期")
    private Long exp;
}
