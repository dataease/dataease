package io.dataease.visualization.dao.ext.mapper;

import io.dataease.api.visualization.dto.VisualizationLinkJumpDTO;
import io.dataease.api.visualization.request.VisualizationLinkJumpBaseRequest;
import io.dataease.api.visualization.vo.VisualizationLinkJumpInfoVO;
import io.dataease.api.visualization.vo.VisualizationLinkJumpVO;
import io.dataease.api.visualization.vo.VisualizationOutParamsJumpVO;
import io.dataease.api.visualization.vo.VisualizationViewTableVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ExtVisualizationLinkJumpMapper {
    List<VisualizationLinkJumpDTO> queryWithDvId(@Param("dvId") Long dvId, @Param("uid") Long uid, @Param("isDesktop") Boolean isDesktop);

    List<VisualizationLinkJumpDTO> queryWithDvIdSnapshot(@Param("dvId") Long dvId, @Param("uid") Long uid, @Param("isDesktop") Boolean isDesktop);

    VisualizationLinkJumpDTO queryWithViewId(@Param("dvId") Long dvId, @Param("viewId") Long viewId, @Param("uid") Long uid, @Param("isDesktop") Boolean isDesktop);

    void deleteJumpTargetViewInfoSnapshot(@Param("dvId") Long dvId, @Param("viewId") Long viewId);

    void deleteJumpInfoSnapshot(@Param("dvId") Long dvId, @Param("viewId") Long viewId);

    void deleteJumpSnapshot(@Param("dvId") Long dvId, @Param("viewId") Long viewId);










}
