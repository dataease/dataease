package io.dataease.base.mapper;

import io.dataease.base.domain.TestResource;
import io.dataease.base.domain.TestResourceExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TestResourceMapper {
    long countByExample(TestResourceExample example);

    int deleteByExample(TestResourceExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestResource record);

    int insertSelective(TestResource record);

    List<TestResource> selectByExampleWithBLOBs(TestResourceExample example);

    List<TestResource> selectByExample(TestResourceExample example);

    TestResource selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestResource record, @Param("example") TestResourceExample example);

    int updateByExampleWithBLOBs(@Param("record") TestResource record, @Param("example") TestResourceExample example);

    int updateByExample(@Param("record") TestResource record, @Param("example") TestResourceExample example);

    int updateByPrimaryKeySelective(TestResource record);

    int updateByPrimaryKeyWithBLOBs(TestResource record);

    int updateByPrimaryKey(TestResource record);
}
