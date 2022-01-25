package io.dataease.commons.constants;

public enum ResourceAuthLevel {

    COMMON_LEVEL_USE(1),

    PANNEL_LEVEL_VIEW(1),
    PANNEL_LEVEL_EXPORT(3),
    PANNEL_LEVEL_MANAGE(5),
    PANNEL_LEVEL_GRANT(15),

    DATASET_LEVEL_USE(1),
    DATASET_LEVEL_MANAGE(3),
    DATASET_LEVEL_GRANT(15),

    LINK_LEVEL_USE(1),
    LINK_LEVEL_MANAGE(3),
    LINK_LEVEL_GRANT(15);

    private Integer level;

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    ResourceAuthLevel(Integer level) {
        this.level = level;
    }


}
