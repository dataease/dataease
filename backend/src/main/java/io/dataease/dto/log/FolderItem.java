package io.dataease.dto.log;

import lombok.Data;

import java.io.Serializable;

@Data
public class FolderItem implements Serializable {

    private String id;

    private String name;

    private Integer type;

}
