package io.dataease.visualization.dao.perext;

import io.dataease.dataset.dao.auto.entity.CoreDatasetGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;


@Mapper
public interface ResourcePermissionMapper {

    @Select("select `component_data` from data_visualization_info where id = #{id}")
    String queryResourceData(@Param("id") Long id);

    @Select("""
    <script>
        select cdg.* from core_dataset_group cdg 
        where EXISTS
        (SELECT 1 FROM core_chart_view cv WHERE cv.table_id = cdg.id and cv.id IN
        <foreach item='item' index='index' collection='viewIds' open='(' separator=',' close=')'>
            #{item}
        </foreach>
        )
    </script>
    """)
    List<CoreDatasetGroup> queryDataSetList(@Param("viewIds") List<Long> viewIds);
}
