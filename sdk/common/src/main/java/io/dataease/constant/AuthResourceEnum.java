package io.dataease.constant;

public enum AuthResourceEnum {

    PANEL(2, 1), SCREEN(3, 2), DATASET(5, 3), DATASOURCE(6, 4), SYSTEM(7, 0), USER(8, 5), ROLE(8, 6),  ORG(9, 7);

    private long menuId;

    private int flag;

    public long getMenuId() {
        return menuId;
    }

    public void setMenuId(long menuId) {
        this.menuId = menuId;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    AuthResourceEnum(long menuId, int flag) {
        this.menuId = menuId;
        this.flag = flag;
    }
}
