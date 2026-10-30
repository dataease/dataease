package io.dataease.api.permissions.setting.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HmacExchangeVO extends HmacInfoVO {
    private String requestId;
    private String encryptedKey;
    private String iv;
    private String ciphertext;
}
