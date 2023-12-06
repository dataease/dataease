package io.dataease.controller.sys.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class TransAccountRequest implements Serializable {

    private String account;
}
