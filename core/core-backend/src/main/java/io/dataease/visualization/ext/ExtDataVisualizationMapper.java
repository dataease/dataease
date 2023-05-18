package io.dataease.visualization.ext;

import io.dataease.api.visualization.vo.DataVisualizationBaseVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ExtDataVisualizationMapper {


    @Select("<script> SELECT id, `name`, `name` as label, pid, org_id, node_type, mobile_layout, create_time, create_by, update_time, update_by \n" +
            "FROM\n" +
            "\tdata_visualization_info where delete_flag=0 <when test='nodeType !=null'> and node_type = #{nodeType} </when>  <when test='type !=null'> and type = #{type} </when> order by node_type desc </script>")
    List<DataVisualizationBaseVO> findBashInfo(@Param("nodeType") String nodeType, @Param("type") String type);

}
