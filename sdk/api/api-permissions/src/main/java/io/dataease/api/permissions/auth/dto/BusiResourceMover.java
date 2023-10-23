package io.dataease.api.permissions.auth.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class BusiResourceMover implements Serializable {

    private Long id;

    private Long pid;
}
