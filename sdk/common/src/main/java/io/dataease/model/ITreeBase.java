package io.dataease.model;

import java.util.List;

public interface ITreeBase<T> {

    String getNodeType();

    String getId();

    void setId(String id);

    String getPid();

    void setPid(String pid);

    List<T> getChildren();

    void setChildren(List<T> children);
}
