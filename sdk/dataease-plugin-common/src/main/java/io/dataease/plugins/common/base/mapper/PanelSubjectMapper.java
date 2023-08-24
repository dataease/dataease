package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.PanelSubject;
import io.dataease.plugins.common.base.domain.PanelSubjectExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PanelSubjectMapper {
    long countByExample(PanelSubjectExample example);

    int deleteByExample(PanelSubjectExample example);

    int deleteByPrimaryKey(String id);

    int insert(PanelSubject record);

    int insertSelective(PanelSubject record);

    List<PanelSubject> selectByExampleWithBLOBs(PanelSubjectExample example);

    List<PanelSubject> selectByExample(PanelSubjectExample example);

    PanelSubject selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") PanelSubject record, @Param("example") PanelSubjectExample example);

    int updateByExampleWithBLOBs(@Param("record") PanelSubject record, @Param("example") PanelSubjectExample example);

    int updateByExample(@Param("record") PanelSubject record, @Param("example") PanelSubjectExample example);

    int updateByPrimaryKeySelective(PanelSubject record);

    int updateByPrimaryKeyWithBLOBs(PanelSubject record);

    int updateByPrimaryKey(PanelSubject record);
}