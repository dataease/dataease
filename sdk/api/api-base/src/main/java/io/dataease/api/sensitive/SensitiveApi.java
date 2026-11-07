package io.dataease.api.sensitive;

import io.dataease.auth.ConfigurationSecurity;
import io.dataease.auth.DeApiPath;
import io.dataease.constant.AuthResourceEnum;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@DeApiPath(value = "/sensitive", rt = AuthResourceEnum.SYSTEM)
public interface SensitiveApi {
    @PostMapping("/reveal")
    @ConfigurationSecurity(encryptedRequest = true)
    EncryptedValue reveal(@RequestBody Request request);

    record Request(String resourceId, SecretType secretType, String password, String responseKey) {
    }

    record EncryptedValue(String iv, String ciphertext) {
    }
}
