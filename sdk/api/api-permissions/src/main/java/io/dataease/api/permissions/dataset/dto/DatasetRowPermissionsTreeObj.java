package io.dataease.api.permissions.dataset.dto;


import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class DatasetRowPermissionsTreeObj implements Serializable {

    private String logic;
    private List<DatasetRowPermissionsTreeItem> items;

    private static final long serialVersionUID = 1L;
}
