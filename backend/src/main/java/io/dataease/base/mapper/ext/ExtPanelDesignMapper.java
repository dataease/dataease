package io.dataease.base.mapper.ext;


import org.apache.ibatis.annotations.Param;

public interface ExtPanelDesignMapper {

    void deleteByPanelId(@Param("panelId") String panelId);

}
