package io.dataease.model;

import java.util.List;

public interface ITreeBase<T> {

    String getNodeType();

    Long getId();

    void setId(Long id);

    Long getPid();

    void setPid(Long pid);

    List<T> getChildren();

    void setChildren(List<T> children);
}
