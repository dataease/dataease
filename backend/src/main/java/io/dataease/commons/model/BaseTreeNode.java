package io.dataease.commons.model;

import io.dataease.plugins.common.model.ITreeBase;
import lombok.Data;

import java.util.List;

@Data
public class BaseTreeNode implements ITreeBase<BaseTreeNode> {

    private String id;

    private String pid;

    private String text;

    private String nodeType;

    private List<BaseTreeNode> children;

    public BaseTreeNode(String id, String pid, String text, String nodeType) {
        this.id = id;
        this.pid = pid;
        this.text = text;
        this.nodeType = nodeType;
    }

}
