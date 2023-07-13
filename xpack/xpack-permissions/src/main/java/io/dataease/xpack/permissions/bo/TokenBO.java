package io.dataease.xpack.permissions.bo;

import io.dataease.auth.bo.TokenUserBO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class TokenBO extends TokenUserBO implements Serializable {

    private Long exp;

    public TokenBO(Long userId, Long defaultOid, Long exp) {
        super(userId, defaultOid);
        this.exp = exp;
    }
}
