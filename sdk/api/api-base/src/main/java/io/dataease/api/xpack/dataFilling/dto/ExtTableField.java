package io.dataease.api.xpack.dataFilling.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExtTableField implements Serializable {
    @Serial
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
    public static class ExtTableFieldSetting implements Serializable  {

        @Serial
        private static final long serialVersionUID = 8776508642526681125L;

        private String name;

        private boolean required;

        private ExtTableFieldMapping mapping;

        private String rangeSeparator;

        private boolean unique;

        private String inputType;

        private String dateType;

        private String placeholder;
        private String startPlaceholder;
        private String endPlaceholder;

        private Integer optionSourceType;

        @JsonSerialize(using = ToStringSerializer.class)
        private Long optionDatasource;
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
    public static class Option implements Serializable  {

        @Serial
        private static final long serialVersionUID = -1681618296840344071L;

        private String name;

        private Object value;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ExtTableFieldMapping implements Serializable  {

        @Serial
        private static final long serialVersionUID = 4233066732126872840L;

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
    public static class TableField implements Serializable  {

        @Serial
        private static final long serialVersionUID = 85092190247927362L;

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
