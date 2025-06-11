package io.dataease.datasource.dao.ext.po;

import lombok.Data;

import java.io.Serializable;

@Data
public class DataSourceNodePO implements Serializable {


    private Long id;

    private Long pid;

    private String name;

    private String type;

    private String status;

    private Long createTime;
}
