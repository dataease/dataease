package io.dataease.mobile.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class PanelEntity implements Serializable {

    private String id;

    private String text;

    private String pid;

    private String type;
}
