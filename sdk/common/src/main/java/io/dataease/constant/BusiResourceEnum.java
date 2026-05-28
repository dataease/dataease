package io.dataease.constant;


import lombok.Getter;

@Getter
public enum BusiResourceEnum {
    MENU(0), PANEL(1), SCREEN(2), DATASET(3), DATASOURCE(4), DATA_FILLING(8), SPREADSHEET(10);

    private final int flag;

    BusiResourceEnum(int flag) {
        this.flag = flag;
    }
}
