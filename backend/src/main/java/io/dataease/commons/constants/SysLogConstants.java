package io.dataease.commons.constants;

import java.util.Arrays;
import java.util.Optional;

public class SysLogConstants {

    public static String operateTypeName(Integer value) {
        Optional<OPERATE_TYPE> any = Arrays.stream(OPERATE_TYPE.class.getEnumConstants()).filter(e -> e.value == value).findAny();
        if (any.isPresent()) return any.get().name;
        return null;
    }

    public enum OPERATE_TYPE {
        CREATE(1, "OPERATE_TYPE_CREATE"),
        MODIFY(2, "OPERATE_TYPE_MODIFY"),
        DELETE(3, "OPERATE_TYPE_DELETE"),
        SHARE(4, "OPERATE_TYPE_SHARE"),
        UNSHARE(5, "OPERATE_TYPE_UNSHARE"),
        AUTHORIZE(6, "OPERATE_TYPE_AUTHORIZE"),
        UNAUTHORIZE(7, "OPERATE_TYPE_UNAUTHORIZE"),
        CREATELINK(8, "OPERATE_TYPE_CREATELINK"),
        DELETELINK(9, "OPERATE_TYPE_DELETELINK"),
        MODIFYLINK(10, "OPERATE_TYPE_MODIFYLINK"),
        UPLOADFILE(11, "OPERATE_TYPE_UPLOADFILE"),

        LOGIN(12, "OPERATE_TYPE_LOGIN"),

        PC_VIEW(13, "OPERATE_TYPE_PC_VIEW"),

        MB_VIEW(14, "OPERATE_TYPE_MB_VIEW"),

        EXPORT(15, "OPERATE_TYPE_EXPORT"),

        BIND(16, "OPERATE_TYPE_BIND"),

        UNBIND(17, "OPERATE_TYPE_UNBIND");
        private Integer value;
        private String name;
        OPERATE_TYPE(Integer value, String name) {
            this.value = value;
            this.name = name;
        }

        public Integer getValue() {
            return value;
        }

        public String getName() {
            return name;
        }
    }

    public static String sourceTypeName(Integer value) {
        Optional<SOURCE_TYPE> any = Arrays.stream(SOURCE_TYPE.class.getEnumConstants()).filter(e -> e.value == value).findAny();
        if (any.isPresent()) return any.get().name;
        return null;
    }

    public enum SOURCE_TYPE {
        DATASOURCE(1, "SOURCE_TYPE_DATASOURCE"),
        DATASET(2, "SOURCE_TYPE_DATASET"),
        PANEL(3, "SOURCE_TYPE_PANEL"),
        VIEW(4, "SOURCE_TYPE_VIEW"),
        /*LINK(5, "SOURCE_TYPE_LINK"),*/
        USER(6, "SOURCE_TYPE_USER"),
        DEPT(7, "SOURCE_TYPE_DEPT"),
        ROLE(8, "SOURCE_TYPE_ROLE"),
        DRIVER(9, "SOURCE_TYPE_DRIVER"),
        DRIVER_FILE(10, "SOURCE_TYPE_DRIVER_FILE"),
        MENU(11, "SOURCE_TYPE_MENU");
        private Integer value;
        private String name;

        SOURCE_TYPE(Integer value, String name) {
            this.value = value;
            this.name = name;
        }

        public Integer getValue() {
            return value;
        }

        public String getName() {
            return name;
        }
    }
}
