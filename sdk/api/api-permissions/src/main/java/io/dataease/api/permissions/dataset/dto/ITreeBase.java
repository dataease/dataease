package io.dataease.api.permissions.dataset.dto;

import java.util.List;

public interface ITreeBase<T> {
    String getNodeType();

    Long getId();

    void setId(Long var1);

    Long getPid();

    void setPid(Long var1);

    List<T> getChildren();

    void setChildren(List<T> var1);
}
