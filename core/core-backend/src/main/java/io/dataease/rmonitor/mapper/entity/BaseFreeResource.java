package io.dataease.rmonitor.mapper.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class BaseFreeResource implements Serializable {

    private Long id;

    private String name;

    private Long pid;
}
