package io.dataease.dto.dataset;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class DeSortDTO implements Serializable {

    private String id;

    private String name;

    private String sort;

    private List<String> custom;
}
