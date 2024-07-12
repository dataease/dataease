package io.dataease.plugins.common.dto.datafill;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExtTableField implements Serializable {
    private static final long serialVersionUID = 9021129395822053871L;

    private String type;

    private String typeName;

    private String icon;

    private String id;

    private ExtTableFieldSetting settings;

    private boolean removed;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ExtTableFieldSetting {

        private String name;

        private boolean required;

        private ExtTableFieldMapping mapping;

        //弃用
        private boolean enableTime;

        private String rangeSeparator;

        private boolean unique;

        private String inputType;

        private String dateType;

        private String placeholder;
        private String startPlaceholder;
        private String endPlaceholder;

        private Integer optionSourceType;

        private String optionDatasource;
        private String optionTable;
        private String optionColumn;
        private String optionOrder;

        private boolean multiple;

        private boolean updateRuleCheck;

        private List<Option> options;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Option {

        private String name;

        private Object value;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ExtTableFieldMapping {

        private String columnName;

        //dateRange下对应两个字段
        private String columnName1;
        private String columnName2;

        private String oldColumnName;
        private String oldColumnName1;
        private String oldColumnName2;

        private BaseType type;

        //长度
        private Integer size;
        //精度
        private Integer accuracy;

    }

    public enum BaseType {
        nvarchar, //字符串
        text, //长文本
        number, //整型数字
        decimal, //小数数字
        datetime //日期
    }


    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TableField {

        private String columnName;

        private String oldColumnName;

        private BaseType type;

        private boolean required;

        private boolean primaryKey;

        //长度
        private Integer size;
        //精度
        private Integer accuracy;

        private String comment;

    }

}
