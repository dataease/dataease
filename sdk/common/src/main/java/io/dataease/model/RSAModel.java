package io.dataease.model;

import lombok.Data;

@Data
public class RSAModel {

    private String privateKey;

    private String publicKey;

    private String aesKey;
}
