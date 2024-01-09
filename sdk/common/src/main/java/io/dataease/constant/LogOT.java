package io.dataease.constant;

import java.util.Arrays;

public enum LogOT {
    CREATE(1, "OPERATE_TYPE_CREATE"),
    MODIFY(2, "OPERATE_TYPE_MODIFY"),
    DELETE(3, "OPERATE_TYPE_DELETE"),
    READ(4, "OPERATE_TYPE_READ"),
    EXPORT(5, "OPERATE_TYPE_EXPORT"),
    AUTHORIZE(6, "OPERATE_TYPE_AUTHORIZE"),
    UNAUTHORIZE(7, "OPERATE_TYPE_UNAUTHORIZE"),
    CREATELINK(8, "OPERATE_TYPE_CREATELINK"),
    DELETELINK(9, "OPERATE_TYPE_DELETELINK"),
    MODIFYLINK(10, "OPERATE_TYPE_MODIFYLINK"),
    UPLOADFILE(11, "OPERATE_TYPE_UPLOADFILE"),
    BIND(12, "OPERATE_TYPE_BIND"),
    UNBIND(13, "OPERATE_TYPE_UNBIND"),
    LOGIN(14, "OPERATE_TYPE_LOGIN");
    private Integer value;
    private String name;

    LogOT(Integer value, String name) {
        this.value = value;
        this.name = name;
    }

    public static LogOT fromValue(Integer value) {
        return Arrays.stream(values()).filter(v -> v.value.equals(value)).findFirst().get();
    }

    public Integer getValue() {
        return value;
    }

    public String getName() {
        return name;
    }
}
