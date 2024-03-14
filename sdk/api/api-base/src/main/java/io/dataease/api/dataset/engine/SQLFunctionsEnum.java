package io.dataease.api.dataset.engine;

/**
 * @Author Junjun
 */
public enum SQLFunctionsEnum {
    SUBSTRING("SUBSTRING", "SUBSTRING(s,n,len)", 0, "获取从字符串s中的第n个位置开始长度为len的字符串", false),
    ABS("ABS", "ABS(x)", 2, "返回x的绝对值", false),
    CEIL("CEIL", "CEIL(x)", 2, "返回不小于x的最小整数", false),
    FLOOR("FLOOR", "FLOOR(x)", 2, "返回不大于x的最大整数", false),
    ROUND1("ROUND", "ROUND(x)", 2, "返回离x最近的整数", false),
    ROUND2("ROUND", "ROUND(x,y)", 2, "保留x小数点后y位的值，但截断时要进行四舍五入", false),
    COUNT("COUNT", "COUNT(x)", 4, "对x计数", false),
    SUM("SUM", "SUM(x)", 4, "对x求和", false),
    AVG("AVG", "AVG(x)", 4, "对x求平均值", false),
    MAX("MAX", "MAX(x)", 4, "对x求最大值", false),
    MIN("MIN", "MIN(x)", 4, "对x求最小值", false);

    private String name;// 显示名
    private String func;// 表达式
    private int type;// 0文本，1时间，2数值，3逻辑，4聚合
    private String desc;// 描述
    private boolean isCustom;// 是否de自定义

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
