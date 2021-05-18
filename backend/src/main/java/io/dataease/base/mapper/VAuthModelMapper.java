package io.dataease.base.mapper;

import io.dataease.base.domain.VAuthModel;
import io.dataease.base.domain.VAuthModelExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface VAuthModelMapper {
    long countByExample(VAuthModelExample example);

    int deleteByExample(VAuthModelExample example);

    int insert(VAuthModel record);

    int insertSelective(VAuthModel record);

    List<VAuthModel> selectByExample(VAuthModelExample example);

    int updateByExampleSelective(@Param("record") VAuthModel record, @Param("example") VAuthModelExample example);

    int updateByExample(@Param("record") VAuthModel record, @Param("example") VAuthModelExample example);
}