package io.dataease.constant;

import lombok.Getter;

@Getter
public enum AuthResourceEnum {

    PANEL(2, 1), SCREEN(3, 2), DATASET(5, 3), DATASOURCE(6, 4), SYSTEM(7, 0), USER(8, 5), ROLE(8, 6),  ORG(9, 7),  SYNC_DATASOURCE(23, 9),  TASK(24, 9), SUMMARY(22, 9), DATA_FILLING(60, 8), SPREADSHEET(100L, 10), EMBEDDED(25L, 11), SYS_PARAM(16L, 12);

    private final long menuId;

    private final int flag;

    AuthResourceEnum(long menuId, int flag) {
        this.menuId = menuId;
        this.flag = flag;
    }
}
