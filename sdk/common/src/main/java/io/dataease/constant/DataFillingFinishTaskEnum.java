package io.dataease.constant;

import java.util.Arrays;

public enum DataFillingFinishTaskEnum {

    OPEN(0), FINISHED(1);

    private Integer flag;

    public Integer getFlag() {
        return flag;
    }

    public void setFlag(Integer flag) {
        this.flag = flag;
    }

    DataFillingFinishTaskEnum(Integer flag) {
        this.flag = flag;
    }

    DataFillingFinishTaskEnum() {
    }

    public static DataFillingFinishTaskEnum fromValue(Integer flag) {
        return Arrays.stream(values()).filter(v -> v.flag.equals(flag)).findFirst().get();
    }
}
