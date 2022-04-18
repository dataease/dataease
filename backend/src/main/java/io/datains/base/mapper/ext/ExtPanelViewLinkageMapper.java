package io.datains.base.mapper.ext;

import io.datains.base.domain.DatasetTableField;
import io.datains.dto.LinkageInfoDTO;
import io.datains.dto.PanelViewLinkageDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtPanelViewLinkageMapper {

    List<PanelViewLinkageDTO> getViewLinkageGather(@Param("panelId") String panelId,@Param("sourceViewId") String sourceViewId,@Param("targetViewIds") List<String> targetViewIds);

    List<LinkageInfoDTO> getPanelAllLinkageInfo(@Param("panelId") String panelId);

    List<DatasetTableField> queryTableField(@Param("table_id") String tableId);

    List<DatasetTableField> queryTableFieldWithViewId(@Param("viewId") String viewId);

    void deleteViewLinkage(@Param("panelId") String panelId,@Param("sourceViewId") String sourceViewId);

    void deleteViewLinkageField(@Param("panelId") String panelId,@Param("sourceViewId") String sourceViewId);

    void copyViewLinkage(@Param("copyId") String copyId);

    void copyViewLinkageField(@Param("copyId") String copyId);
}
