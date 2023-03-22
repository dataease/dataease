package io.dataease.xpack.permissions.login.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenUserBO implements Serializable {

    @Serial
    private static final long serialVersionUID = -9163835853313313361L;

    private Long userId;
}
