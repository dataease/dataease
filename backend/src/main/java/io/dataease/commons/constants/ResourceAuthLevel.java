package io.dataease.commons.constants;

public enum ResourceAuthLevel {

    COMMON_LEVEL_USE(1),

    PANEL_LEVEL_VIEW(1),
    PANEL_LEVEL_EXPORT(3),
    PANEL_LEVEL_MANAGE(5),
    PANEL_LEVEL_GRANT(15),

    DATASET_LEVEL_USE(1),
    DATASET_LEVEL_MANAGE(3),
    DATASET_LEVEL_GRANT(15),

    LINK_LEVEL_USE(1),
    LINK_LEVEL_MANAGE(3),
    LINK_LEVEL_GRANT(15),

    DATASOURCE_LEVEL_USE(1),
    DATASOURCE_LEVEL_MANAGE(3),
    DATASOURCE_LEVEL_GRANT(15);

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
