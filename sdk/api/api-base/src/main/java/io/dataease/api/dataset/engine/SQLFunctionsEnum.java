package io.dataease.api.dataset.engine;

/**
 * @Author Junjun
 */
public enum SQLFunctionsEnum {
    ABS("ABS", "ABS(x)", 2, "返回x的绝对值", false),
    SUM("SUM", "SUM(x)", 4, "对x分组求和", false);

    private String name;
    private String func;
    private int type;// 0文本，1时间，2数值，3逻辑，4聚合
    private String desc;
    private boolean isCustom;

    SQLFunctionsEnum(String name, String func, int type, String desc, boolean isCustom) {
        this.name = name;
        this.func = func;
        this.type = type;
        this.desc = desc;
        this.isCustom = isCustom;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFunc() {
        return func;
    }

    public void setFunc(String func) {
        this.func = func;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public boolean isCustom() {
        return isCustom;
    }

    public void setCustom(boolean custom) {
        isCustom = custom;
    }
}
