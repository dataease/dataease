package io.dataease.plugins.common.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class FileContent implements Serializable {
    private String fileId;

    private byte[] file;

    private static final long serialVersionUID = 1L;
}
