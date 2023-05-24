package io.dataease.constant;

public enum AuthResourceEnum {

    PANEL(9, 1), SCREEN(10, 2), DATASET(11, 3), DATASOURCE(12, 4), SYSTEM(6, 0), USER(13, 5), ROLE(13, 6),  ORG(14, 7);

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
