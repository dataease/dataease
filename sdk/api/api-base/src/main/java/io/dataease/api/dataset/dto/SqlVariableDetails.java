package io.dataease.api.dataset.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;

@Data
public class SqlVariableDetails {
    private String variableName;
    private String alias;
    private List<String> type;
    private int deType;
    private String details;
    private String defaultValue;
    private DefaultValueScope defaultValueScope;
    @JsonSerialize(using = ToStringSerializer.class)
    private String id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long datasetTableId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long datasetGroupId;
    private boolean required;
    private String operator;
    private List<String> value;

    public enum DefaultValueScope {
        EDIT("EDIT"),
        ALLSCOPE("ALLSCOPE");
        private String  type;
        DefaultValueScope(String type){
            this.type = type;
        }
        public String getType(){
            return type;
        }
    }
}
