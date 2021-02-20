package io.dataease.base.mapper;

import io.dataease.base.domain.Workspace;
import io.dataease.base.domain.WorkspaceExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface WorkspaceMapper {
    long countByExample(WorkspaceExample example);

    int deleteByExample(WorkspaceExample example);

    int deleteByPrimaryKey(String id);

    int insert(Workspace record);

    int insertSelective(Workspace record);

    List<Workspace> selectByExample(WorkspaceExample example);

    Workspace selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") Workspace record, @Param("example") WorkspaceExample example);

    int updateByExample(@Param("record") Workspace record, @Param("example") WorkspaceExample example);

    int updateByPrimaryKeySelective(Workspace record);

    int updateByPrimaryKey(Workspace record);
}
