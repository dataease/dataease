package io.dataease.ext;

import io.dataease.dto.LinkageInfoDTO;
import io.dataease.dto.PanelViewLinkageDTO;
import io.dataease.plugins.common.base.domain.DatasetTableField;
import io.dataease.plugins.common.base.domain.PanelViewLinkage;
import io.dataease.plugins.common.base.domain.PanelViewLinkageField;
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

    List<PanelViewLinkage> findLinkageWithPanelId(@Param("panelId") String panelId);

    List<PanelViewLinkageField> findLinkageFieldWithPanelId(@Param("panelId") String panelId);
}
