package io.dataease.base.mapper;

import io.dataease.base.domain.Issues;
import io.dataease.base.domain.IssuesExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface IssuesMapper {
    long countByExample(IssuesExample example);

    int deleteByExample(IssuesExample example);

    int deleteByPrimaryKey(String id);

    int insert(Issues record);

    int insertSelective(Issues record);

    List<Issues> selectByExampleWithBLOBs(IssuesExample example);

    List<Issues> selectByExample(IssuesExample example);

    Issues selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") Issues record, @Param("example") IssuesExample example);

    int updateByExampleWithBLOBs(@Param("record") Issues record, @Param("example") IssuesExample example);

    int updateByExample(@Param("record") Issues record, @Param("example") IssuesExample example);

    int updateByPrimaryKeySelective(Issues record);

    int updateByPrimaryKeyWithBLOBs(Issues record);

    int updateByPrimaryKey(Issues record);
}
