package io.dataease.mobile.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class DirItemDTO implements Serializable {

    private String id;
    private String text;
    private String type;
    private Integer subs;
}
