package io.dataease.constant;

import java.util.Arrays;

public enum MessageEnum {

    INNER(0), EMAIL(1), WECOM(2), DINGTALK(3), LARK(4), LARKSUITE(5), LARKGROUP(6);
    private Integer flag;

    public Integer getFlag() {
        return flag;
    }

    public void setFlag(Integer flag) {
        this.flag = flag;
    }

    MessageEnum(Integer flag) {
        this.flag = flag;
    }

    MessageEnum() {
    }

    public static MessageEnum fromValue(Integer flag) {
        return Arrays.stream(values()).filter(v -> v.flag.equals(flag)).findFirst().get();
    }
}
