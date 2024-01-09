package io.dataease.constant;

import java.util.Arrays;

public enum LogST {
    PANEL(1, "SOURCE_TYPE_PANEL"),
    SCREEN(2, "SOURCE_TYPE_SCREEN"),
    DATASET(3, "SOURCE_TYPE_DATASET"),
    DATASOURCE(4, "SOURCE_TYPE_DATASOURCE"),
    USER(5, "SOURCE_TYPE_USER"),
    ROLE(6, "SOURCE_TYPE_ROLE"),
    ORG(7, "SOURCE_TYPE_DEPT"),
    VIEW(8, "SOURCE_TYPE_VIEW"),
    LINK(9, "SOURCE_TYPE_LINK"),
    DRIVER(10, "SOURCE_TYPE_DRIVER"),
    DRIVER_FILE(11, "SOURCE_TYPE_DRIVER_FILE"),
    MENU(12, "SOURCE_TYPE_MENU"),
    APIKEY(13, "SOURCE_TYPE_APIKEY");
    private Integer value;

    private String name;

    public Integer getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    LogST(Integer value, String name) {
        this.value = value;
        this.name = name;
    }

    public static LogST fromValue(Integer value) {
        return Arrays.stream(values()).filter(v -> v.value.equals(value)).findFirst().get();
    }

    LogST() {
    }
}
