package io.dataease.api.dataset.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class DeSortDTO implements Serializable {

    private Long id;

    private String name;

    private String sort;

    private List<String> custom;
}
