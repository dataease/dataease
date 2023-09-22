package io.dataease.visualization.dao.ext.mapper;

import io.dataease.api.visualization.dto.VisualizationLinkJumpDTO;
import io.dataease.api.visualization.request.VisualizationLinkJumpBaseRequest;
import io.dataease.api.visualization.vo.VisualizationLinkJumpInfoVO;
import io.dataease.api.visualization.vo.VisualizationLinkJumpVO;
import io.dataease.api.visualization.vo.VisualizationViewTableVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
@Mapper
public interface ExtVisualizationLinkJumpMapper {
    List<VisualizationLinkJumpDTO> queryWithDvId(@Param("dvId") Long dvId);

    VisualizationLinkJumpDTO queryWithViewId(@Param("dvId") Long dvId,@Param("viewId") Long viewId);

    void deleteJumpTargetViewInfo(@Param("dvId") Long dvId,@Param("viewId") Long viewId);

    void deleteJumpInfo(@Param("dvId") Long dvId,@Param("viewId") Long viewId);

    void deleteJump(@Param("dvId") Long dvId,@Param("viewId") Long viewId);

    void deleteJumpTargetViewInfoWithVisualization(@Param("dvId") Long dvId);

    void deleteJumpInfoWithVisualization(@Param("dvId") Long dvId);

    void deleteJumpWithVisualization(@Param("dvId") Long dvId);

    List<VisualizationLinkJumpDTO> getTargetVisualizationJumpInfo(@Param("request") VisualizationLinkJumpBaseRequest request);

    void copyLinkJump(@Param("copyId")Long copyId);

    void copyLinkJumpInfo(@Param("copyId")Long copyId);

    void copyLinkJumpTarget(@Param("copyId")Long copyId);

    List<VisualizationLinkJumpVO> findLinkJumpWithDvId(@Param("dvId")Long dvId);
    List<VisualizationLinkJumpInfoVO> findLinkJumpInfoWithDvId(@Param("dvId")Long dvId);
    List<VisualizationViewTableVO> getViewTableDetails(@Param("dvId")Long dvId);
}
