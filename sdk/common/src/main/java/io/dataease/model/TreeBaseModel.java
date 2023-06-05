package io.dataease.model;

public interface TreeBaseModel<T>{

    Long getId();

    Long getPid();

    String getName();

    void setName(String name);
}
