package io.dataease.api.permissions.dataset.dto;


import lombok.Data;

import java.util.List;

@Data
public class BaseTreeNode implements ITreeBase<BaseTreeNode> {

    private Long id;

    private Long pid;

    private String text;

    private String nodeType;

    private List<BaseTreeNode> children;

    public BaseTreeNode(Long id, Long pid, String text, String nodeType) {
        this.id = id;
        this.pid = pid;
        this.text = text;
        this.nodeType = nodeType;
    }

}
