package io.dataease.base.mapper;

import io.dataease.base.domain.TestResourcePool;
import io.dataease.base.domain.TestResourcePoolExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TestResourcePoolMapper {
    long countByExample(TestResourcePoolExample example);

    int deleteByExample(TestResourcePoolExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestResourcePool record);

    int insertSelective(TestResourcePool record);

    List<TestResourcePool> selectByExample(TestResourcePoolExample example);

    TestResourcePool selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestResourcePool record, @Param("example") TestResourcePoolExample example);

    int updateByExample(@Param("record") TestResourcePool record, @Param("example") TestResourcePoolExample example);

    int updateByPrimaryKeySelective(TestResourcePool record);

    int updateByPrimaryKey(TestResourcePool record);
}
