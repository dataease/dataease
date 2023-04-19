package io.dataease.constant;


public enum BusiResourceEnum {
    PANEL(1), SCREEN(2), DATASET(3), DATASOURCE(4);

    private int flag;

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    BusiResourceEnum(int flag) {
        this.flag = flag;
    }
}
