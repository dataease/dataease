package io.dataease.constant;

import java.util.Arrays;

public enum ReportLastStatusEnum {
    EMPTY(0), RUN(1), SUCCESS(2), FAIL(3);

    private Integer flag;

    public Integer getFlag() {
        return flag;
    }

    public void setFlag(Integer flag) {
        this.flag = flag;
    }

    ReportLastStatusEnum() {
    }

    ReportLastStatusEnum(Integer flag) {
        this.flag = flag;
    }

    public static ReportLastStatusEnum fromValue(Integer flag) {
        return Arrays.stream(values()).filter(v -> v.flag.equals(flag)).findFirst().get();
    }
}
