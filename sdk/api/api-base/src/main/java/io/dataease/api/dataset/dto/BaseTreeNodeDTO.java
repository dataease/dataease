package io.dataease.api.dataset.dto;


import lombok.Data;

import java.util.List;

@Data
public class BaseTreeNodeDTO {

    private String id;

    private String pid;

    private String text;

    private String nodeType;

    private List<BaseTreeNodeDTO> children;

    public BaseTreeNodeDTO(String id, String pid, String text, String nodeType) {
        this.id = id;
        this.pid = pid;
        this.text = text;
        this.nodeType = nodeType;
    }

}
