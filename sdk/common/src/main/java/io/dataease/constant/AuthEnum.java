package io.dataease.constant;

public enum AuthEnum {
    READ(1), USER(2), EXPORT(4), EXPORT_VIEW(5), EXPORT_DETAIL(6), MANAGE(7), AUTH(9);
    private Integer weight;

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    AuthEnum(Integer weight) {
        this.weight = weight;
    }

    AuthEnum() {
    }
}
