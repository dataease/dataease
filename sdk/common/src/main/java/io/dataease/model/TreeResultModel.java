package io.dataease.model;

import java.util.List;

public interface TreeResultModel<T> {

    void setChildren(List<T> children);

    List<T> getChildren();

    Long getId();
}
