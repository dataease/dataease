package io.dataease.api.permissions.auth.dto;

import io.dataease.constant.AuthEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BusiPerCheckDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = -6047004531129863548L;

    private Long id;

    private AuthEnum authEnum;
}
