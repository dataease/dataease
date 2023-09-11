package io.dataease.plugins.common.constants;

public enum DatabaseClassification {
    OLTP("OLTP"),
    OLAP("OLAP"),
    DL("DL"),
    OTHER("OTHER");

    private String  type;
    DatabaseClassification(String type){
        this.type = type;
    }
    public String getType(){
        return type;
    }
}
