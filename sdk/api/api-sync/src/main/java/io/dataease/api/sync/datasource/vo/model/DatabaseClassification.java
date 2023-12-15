package io.dataease.api.sync.datasource.vo.model;

import lombok.Getter;

/**
 * @author fit2cloud
 */

@Getter
public enum DatabaseClassification {
    OLTP("OLTP"),
    OLAP("OLAP"),
    DL("DL"),
    OTHER("OTHER");

    private final String  type;
    DatabaseClassification(String type){
        this.type = type;
    }
}
