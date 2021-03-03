package io.dataease.auth.api.dto;


import lombok.Data;

import java.io.Serializable;

@Data
public class MenuMeta implements Serializable {

    private String title;

    private String icon;
}
