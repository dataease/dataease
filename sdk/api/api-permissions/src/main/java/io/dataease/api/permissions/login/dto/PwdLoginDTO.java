package io.dataease.api.permissions.login.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class PwdLoginDTO {

    @NotBlank(message = "login.validator.name")
    private String name;

    @NotBlank(message = "login.validator.pwd")
    private String pwd;
}
