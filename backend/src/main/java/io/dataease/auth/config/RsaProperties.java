package io.dataease.auth.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class RsaProperties {

    public static String privateKey;

    public static String publicKey;

    @Value("${rsa.private_key}")
    public void setPrivateKey(String privateKey) {
        RsaProperties.privateKey = privateKey;
    }

    @Value("${rsa.public_key}")
    public void setPublicKey(String publicKey) {
        RsaProperties.publicKey = publicKey;
    }
}
