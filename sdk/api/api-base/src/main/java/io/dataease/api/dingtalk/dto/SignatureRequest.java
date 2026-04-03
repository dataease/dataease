package io.dataease.api.dingtalk.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SignatureRequest implements Serializable {

    private String currentUrl;
}
