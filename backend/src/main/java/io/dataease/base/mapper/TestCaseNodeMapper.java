package io.dataease.base.mapper;

import io.dataease.base.domain.TestCaseNode;
import io.dataease.base.domain.TestCaseNodeExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TestCaseNodeMapper {
    long countByExample(TestCaseNodeExample example);

    int deleteByExample(TestCaseNodeExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestCaseNode record);

    int insertSelective(TestCaseNode record);

    List<TestCaseNode> selectByExample(TestCaseNodeExample example);

    TestCaseNode selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestCaseNode record, @Param("example") TestCaseNodeExample example);

    int updateByExample(@Param("record") TestCaseNode record, @Param("example") TestCaseNodeExample example);

    int updateByPrimaryKeySelective(TestCaseNode record);

    int updateByPrimaryKey(TestCaseNode record);
}
