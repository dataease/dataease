package io.dataease.plugins.common.constants;

import java.util.Arrays;
import java.util.Optional;

public enum DatasetType {

    DB("db"),
    SQL("sql"),
    CUSTOM("custom"),
    EXCEL("excel"),
    API("api"),
    UNION("union");

    private String  type;

    DatasetType(String type){
        this.type = type;
    }
    public String getType(){
        return type;
    }

    public static DatasetType getEnumObjByKey(String key){
        Optional<DatasetType> any = Arrays.stream(DatasetType.class.getEnumConstants())
                .filter(e -> e.getType().equals(key)).findAny();
        if (any.isPresent()){
            return any.get();
        }
        return null;
    }
}
