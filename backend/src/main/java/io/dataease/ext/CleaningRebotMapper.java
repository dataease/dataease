package io.dataease.ext;

public interface CleaningRebotMapper {

    int delFloatingDept();

    void updateUserDept();

    void delFloatingRoleMapping();

    void delFloatingPanelShare();

    void delFloatingTargetShare();

    void delFloatingPanelStore();

    void delFloatingTargetStore();

    void delFloatingPanelLink();

    void delFloatingPanelLinkMapping();

    void delFloatingCreatorLink();

    void delFloatingCreatorLinkMapping();
}
