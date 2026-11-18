package io.dataease.api.permissions.setting.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HmacExchangeRequest {
    @NotNull
    private Integer version;

    @NotNull
    @Pattern(regexp = "[0-9a-f]{32}")
    private String requestId;

    @NotNull
    @Size(min = 1, max = 512)
    private String publicKey;
}
