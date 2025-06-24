package io.dataease.visualization.dao.ext.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.dataease.api.permissions.user.vo.UserFormVO;
import io.dataease.api.visualization.dto.VisualizationViewTableDTO;
import io.dataease.api.visualization.vo.DataVisualizationBaseVO;
import io.dataease.api.visualization.vo.DataVisualizationVO;
import io.dataease.api.visualization.vo.VisualizationReportFilterVO;
import io.dataease.dao.auto.entity.CoreChartView;
import io.dataease.visualization.dao.ext.po.VisualizationResourcePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
public interface ExtDataVisualizationMapper {


    void viewCopyWithDv(@Param("sourceDvId") Long sourceDvId, @Param("newDvId") Long newDvId, @Param("copyId") Long copyId, @Param("resourceTable") String resourceTable);

    List<CoreChartView> findViewInfoByCopyId(@Param("copyId") Long copyId);

    DataVisualizationVO findDvInfo(@Param("dvId") Long dvId, @Param("dvType") String dvType, @Param("resourceTable") String resourceTable);

    IPage<VisualizationResourcePO> findRecent(IPage<VisualizationResourcePO> page, @Param("uid") Long uid, @Param("keyword") String keyword, @Param("ew") Map ew);

    void copyLinkJump(@Param("copyId") Long copyId);

    void copyLinkJumpInfo(@Param("copyId") Long copyId);

    void copyLinkJumpTargetInfo(@Param("copyId") Long copyId);

    void copyLinkage(@Param("copyId") Long copyId);

    void copyLinkageField(@Param("copyId") Long copyId);


    List<VisualizationReportFilterVO> queryReportFilter(@Param("dvId") Long dvId, @Param("taskId") Long taskId);


}
