package io.dataease.model;

import lombok.Data;

import java.util.List;

@Data
public class TreeModel<T>{

    private TreeBaseModel<T> data;

    private List<TreeModel> children;

    public TreeModel(TreeBaseModel<T> data) {
        this.data = data;
    }

    public Long getId() {
        return data.getId();
    }

    public Long getPid() {
        return data.getPid();
    }

    public String getName() {
        return data.getName();
    }
}
