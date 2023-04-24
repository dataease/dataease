package io.dataease.api.chart.dto;

import lombok.Data;

import java.util.List;

@Data
public class ColumnPermissionItem {
    private String id;
    private String name;
    private Boolean selected = false;
    private String opt;

    private DesensitizationRule desensitizationRule;


    @Data
    public class DesensitizationRule {
        private BuiltInRule builtInRule;
        private CustomBuiltInRule customBuiltInRule;

        private Integer m;
        private Integer n;
        private String specialCharacter;
        private List<String> specialCharacterList;
    }

    public enum BuiltInRule {
        CompleteDesensitization,
        KeepFirstAndLastThreeCharacters,
        KeepMiddleThreeCharacters,
        custom
    }

    static public String CompleteDesensitization = "******";
    static public String KeepFirstAndLastThreeCharacters = "XXX***XXX";
    static public String KeepMiddleThreeCharacters = "***XXX***";
    public enum CustomBuiltInRule {
        RetainBeforeMAndAfterN,
        RetainMToN
    }
}
