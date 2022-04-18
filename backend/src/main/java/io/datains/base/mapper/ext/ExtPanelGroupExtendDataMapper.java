package io.datains.base.mapper.ext;

import io.datains.dto.PanelGroupExtendDataDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtPanelGroupExtendDataMapper {
    void savePanelExtendData(@Param("records") List<PanelGroupExtendDataDTO> records);

    void copyExtendData(@Param("sourceViewId")String sourceViewId,@Param("newViewId")String newViewId,@Param("newPanelId")String newPanelId);

    void copyWithCopyId(@Param("copyId")String copyId);
}
