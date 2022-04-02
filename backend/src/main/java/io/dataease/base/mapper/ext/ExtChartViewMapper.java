package io.dataease.base.mapper.ext;

import io.dataease.controller.request.chart.ChartViewRequest;
import io.dataease.dto.chart.ChartViewDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ExtChartViewMapper {
    List<ChartViewDTO> search(ChartViewRequest request);

//    ChartViewDTO searchOne(ChartViewRequest request);

    void chartCopy(@Param("newChartId")String newChartId,@Param("oldChartId")String oldChartId,@Param("panelId")String panelId);

    @Select("select id from chart_view where table_id = #{tableId}")
    List<String> allViewIds(@Param("tableId") String tableId);

    String searchAdviceSceneId(@Param("userId") String userId,@Param("panelId") String panelId);

    int checkSameDataSet(@Param("viewIdSource") String viewIdSource,@Param("viewIdTarget") String viewIdTarget);

    ChartViewDTO searchOneWithPrivileges(@Param("userId") String userId,@Param("id") String id );

    ChartViewDTO searchOne(@Param("id") String id );

    void chartCopyWithPanel(@Param("copyId") String copyId);

    void deleteCircleView(@Param("pid") String pid);

    void deleteCircleGroup(@Param("pid") String pid);

    List<ChartViewDTO> searchViewsWithPanelId(@Param("panelId") String panelId);

    ChartViewDTO searchOneFromCache(@Param("id") String id );

    void copyToCache(@Param("id") String id );

    void deleteCacheWithPanel(@Param("viewIds") List<String> viewIds,@Param("panelId") String panelId );

    void deleteViewCache(@Param("viewId") String viewId );

    void copyCacheToView(@Param("viewIds") List<String> viewIds );

    int updateToCache(@Param("viewId") String viewId );

    void copyCache(@Param("sourceViewId") String sourceViewId,@Param("newViewId") String newViewId);

    void deleteNoUseView(@Param("viewIds") List<String> viewIds,@Param("panelId") String panelId );
}
