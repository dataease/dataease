package io.dataease.chart.dao.ext.mapper;

import io.dataease.api.chart.vo.ViewSelectorVO;
import io.dataease.chart.dao.ext.entity.ChartBasePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ExtChartViewMapper {

    @Select("""
            select id, scene_id as pid, title, type from core_chart_view where type != 'VQuery' and scene_id = #{resourceId}
            """)
    List<ViewSelectorVO> queryViewOption(@Param("resourceId") Long resourceId);

    @Select("""
                select 
                    ccv.id as chart_id,
                    ccv.title as chart_name,
                    ccv.type as chart_type,
                    ccv.table_id,
                    dvi.id as resource_id,
                    dvi.name as resource_name,
                    dvi.type as resource_type,
                    ccv.x_axis,
                    ccv.x_axis_ext,
                    ccv.y_axis,
                    ccv.y_axis_ext,
                    ccv.ext_stack,
                    ccv.ext_bubble,
                    ccv.ext_label,
                    ccv.ext_tooltip,
                    ccv.flow_map_start_name,
                    ccv.flow_map_end_name,
                    ccv.ext_color
                from core_chart_view ccv 
                    left join data_visualization_info dvi on dvi.id = ccv.scene_id
                where ccv.id = #{id}
            """)
    ChartBasePO queryChart(@Param("id") Long id);
}
