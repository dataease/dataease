package io.dataease.base.mapper;

import io.dataease.base.domain.Quota;
import io.dataease.base.domain.QuotaExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface QuotaMapper {
    long countByExample(QuotaExample example);

    int deleteByExample(QuotaExample example);

    int deleteByPrimaryKey(String id);

    int insert(Quota record);

    int insertSelective(Quota record);

    List<Quota> selectByExample(QuotaExample example);

    Quota selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") Quota record, @Param("example") QuotaExample example);

    int updateByExample(@Param("record") Quota record, @Param("example") QuotaExample example);

    int updateByPrimaryKeySelective(Quota record);

    int updateByPrimaryKey(Quota record);
}
