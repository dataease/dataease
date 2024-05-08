package io.dataease.constant;

import java.util.Arrays;

public enum ReportTaskEnum {

    WAIT(0), SEND(1), STOP(2), FINISH(3);

    private Integer flag;

    public Integer getFlag() {
        return flag;
    }

    public void setFlag(Integer flag) {
        this.flag = flag;
    }

    ReportTaskEnum(Integer flag) {
        this.flag = flag;
    }

    ReportTaskEnum() {
    }

    public static ReportTaskEnum fromValue(Integer flag) {
        return Arrays.stream(values()).filter(v -> v.flag.equals(flag)).findFirst().get();
    }
}
