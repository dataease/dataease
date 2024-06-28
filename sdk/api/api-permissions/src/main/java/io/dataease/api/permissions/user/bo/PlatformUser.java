package io.dataease.api.permissions.user.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlatformUser implements Serializable {
    @Serial
    private static final long serialVersionUID = 2749044307502902368L;

    private String account;

    private String name;

    private String email;

    private String phone;

    private Integer type;
}
