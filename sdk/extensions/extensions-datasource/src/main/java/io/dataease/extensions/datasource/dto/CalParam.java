package io.dataease.extensions.datasource.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author Junjun
 */
@Data
public class CalParam implements Serializable {
    private String id;
    private String name;
    private String value;
}
