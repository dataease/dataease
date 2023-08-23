package io.dataease.plugins.common.model;

import java.util.List;

/**
 * Author: wangjiahao
 * Date: 2021-05-24
 * Description:
 */
public interface ITreeBase<T> {

    String getNodeType();

    String getId();

    void setId(String id);

    String getPid();

    void setPid(String pid);

    List<T> getChildren();

    void setChildren(List<T> children);
}
