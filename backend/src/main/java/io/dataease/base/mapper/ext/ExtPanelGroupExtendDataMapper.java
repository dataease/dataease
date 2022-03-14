package io.dataease.base.mapper.ext;

import io.dataease.dto.PanelGroupExtendDataDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtPanelGroupExtendDataMapper {
    void savePanelExtendData(@Param("records") List<PanelGroupExtendDataDTO> records);
}
