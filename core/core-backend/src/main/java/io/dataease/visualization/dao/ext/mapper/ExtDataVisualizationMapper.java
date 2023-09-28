package io.dataease.visualization.dao.ext.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.dataease.api.visualization.vo.DataVisualizationBaseVO;
import io.dataease.api.visualization.vo.DataVisualizationVO;
import io.dataease.api.visualization.vo.VisualizationResourceVO;
import io.dataease.chart.dao.auto.entity.CoreChartView;
import io.dataease.visualization.dao.ext.po.StorePO;
import io.dataease.visualization.dao.ext.po.VisualizationResourcePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ExtDataVisualizationMapper {


    @Select("<script> SELECT id, `name`, `name` as label, pid, org_id, node_type, mobile_layout, create_time, create_by, update_time, update_by \n" +
            "FROM\n" +
            "\tdata_visualization_info where delete_flag=0 <when test='nodeType !=null'> and node_type = #{nodeType} </when>  <when test='type !=null'> and type = #{type} </when> order by node_type desc </script>")
    List<DataVisualizationBaseVO> findBashInfo(@Param("nodeType") String nodeType, @Param("type") String type);

    @Select("select type from data_visualization_info where id = #{dvId}")
    String findDvType(@Param("dvId") Long dvId);

    void dvCopy(@Param("sourceDvId") Long sourceDvId,@Param("newDvId") Long newDvId,@Param("copyId") Long copyId);
    void viewCopyWithDv(@Param("sourceDvId") Long sourceDvId,@Param("newDvId") Long newDvId,@Param("copyId") Long copyId);
    List<CoreChartView> findViewInfoByCopyId(@Param("copyId") Long copyId);

    DataVisualizationVO findDvInfo(@Param("dvId") Long dvId,@Param("dvType") String dvType);

    IPage<VisualizationResourcePO> findRecent(IPage<VisualizationResourcePO> page, @Param("uid") Long uid, @Param("ew") QueryWrapper<Object> ew);

    void copyLinkJump(@Param("copyId") Long copyId);

    void copyLinkJumpInfo(@Param("copyId") Long copyId);

    void copyLinkJumpTargetInfo(@Param("copyId") Long copyId);

    void copyLinkage(@Param("copyId") Long copyId);

    void copyLinkageField(@Param("copyId") Long copyId);

}
