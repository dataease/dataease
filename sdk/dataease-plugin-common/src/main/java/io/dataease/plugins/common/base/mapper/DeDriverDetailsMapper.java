package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.DeDriverDetails;
import io.dataease.plugins.common.base.domain.DeDriverDetailsExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DeDriverDetailsMapper {
    long countByExample(DeDriverDetailsExample example);

    int deleteByExample(DeDriverDetailsExample example);

    int deleteByPrimaryKey(String id);

    int insert(DeDriverDetails record);

    int insertSelective(DeDriverDetails record);

    List<DeDriverDetails> selectByExample(DeDriverDetailsExample example);

    DeDriverDetails selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") DeDriverDetails record, @Param("example") DeDriverDetailsExample example);

    int updateByExample(@Param("record") DeDriverDetails record, @Param("example") DeDriverDetailsExample example);

    int updateByPrimaryKeySelective(DeDriverDetails record);

    int updateByPrimaryKey(DeDriverDetails record);
}