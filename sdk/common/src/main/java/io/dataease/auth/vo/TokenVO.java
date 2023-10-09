package io.dataease.auth.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 564596240616527258L;

    private String token;

    private Long exp;
}
