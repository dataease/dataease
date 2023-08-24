package io.dataease.plugins.common.dto.dataset;

import lombok.Data;

import java.util.List;

@Data
public class SqlVariableDetails {
    private String variableName;
    private String alias;
    private List<String> type;
    private String details;
    private String defaultValue;
    private DefaultValueScope defaultValueScope;
    private String id;

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
