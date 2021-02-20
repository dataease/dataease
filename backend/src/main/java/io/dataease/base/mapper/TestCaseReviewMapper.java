package io.dataease.base.mapper;

import io.dataease.base.domain.TestCaseReview;
import io.dataease.base.domain.TestCaseReviewExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TestCaseReviewMapper {
    long countByExample(TestCaseReviewExample example);

    int deleteByExample(TestCaseReviewExample example);

    int deleteByPrimaryKey(String id);

    int insert(TestCaseReview record);

    int insertSelective(TestCaseReview record);

    List<TestCaseReview> selectByExampleWithBLOBs(TestCaseReviewExample example);

    List<TestCaseReview> selectByExample(TestCaseReviewExample example);

    TestCaseReview selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") TestCaseReview record, @Param("example") TestCaseReviewExample example);

    int updateByExampleWithBLOBs(@Param("record") TestCaseReview record, @Param("example") TestCaseReviewExample example);

    int updateByExample(@Param("record") TestCaseReview record, @Param("example") TestCaseReviewExample example);

    int updateByPrimaryKeySelective(TestCaseReview record);

    int updateByPrimaryKeyWithBLOBs(TestCaseReview record);

    int updateByPrimaryKey(TestCaseReview record);
}
