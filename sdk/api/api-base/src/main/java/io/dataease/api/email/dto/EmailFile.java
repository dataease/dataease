package io.dataease.api.email.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class EmailFile implements Serializable {
    @Serial
    private static final long serialVersionUID = -3898069299109267651L;

    private String fileName;

    private byte[] fileByte;
}
