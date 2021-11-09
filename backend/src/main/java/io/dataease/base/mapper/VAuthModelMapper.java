package io.dataease.base.mapper;

import io.dataease.base.domain.VAuthModel;
import io.dataease.base.domain.VAuthModelExample;
import io.dataease.base.domain.VAuthModelWithBLOBs;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface VAuthModelMapper {
    long countByExample(VAuthModelExample example);

    int deleteByExample(VAuthModelExample example);

    int insert(VAuthModelWithBLOBs record);

    int insertSelective(VAuthModelWithBLOBs record);

    List<VAuthModelWithBLOBs> selectByExampleWithBLOBs(VAuthModelExample example);

    List<VAuthModel> selectByExample(VAuthModelExample example);

    int updateByExampleSelective(@Param("record") VAuthModelWithBLOBs record, @Param("example") VAuthModelExample example);

    int updateByExampleWithBLOBs(@Param("record") VAuthModelWithBLOBs record, @Param("example") VAuthModelExample example);

    int updateByExample(@Param("record") VAuthModel record, @Param("example") VAuthModelExample example);
}