package io.dataease.base.mapper;

import io.dataease.base.domain.TestCaseReviewTestCase;
import io.dataease.base.domain.TestCaseReviewTestCaseExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TestCaseReviewTestCaseMapper {
    long countByExample(TestCaseReviewTestCaseExample example);

    int deleteByExample(TestCaseReviewTestCaseExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestCaseReviewTestCase record);

    int insertSelective(TestCaseReviewTestCase record);

    List<TestCaseReviewTestCase> selectByExample(TestCaseReviewTestCaseExample example);

    TestCaseReviewTestCase selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestCaseReviewTestCase record, @Param("example") TestCaseReviewTestCaseExample example);

    int updateByExample(@Param("record") TestCaseReviewTestCase record, @Param("example") TestCaseReviewTestCaseExample example);

    int updateByPrimaryKeySelective(TestCaseReviewTestCase record);

    int updateByPrimaryKey(TestCaseReviewTestCase record);
}
