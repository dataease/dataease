package io.dataease.base.mapper;

import io.dataease.base.domain.MyPlugin;
import io.dataease.base.domain.MyPluginExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface MyPluginMapper {
    long countByExample(MyPluginExample example);

    int deleteByExample(MyPluginExample example);

    int deleteByPrimaryKey(Long pluginId);

    int insert(MyPlugin record);

    int insertSelective(MyPlugin record);

    List<MyPlugin> selectByExample(MyPluginExample example);

    MyPlugin selectByPrimaryKey(Long pluginId);

    int updateByExampleSelective(@Param("record") MyPlugin record, @Param("example") MyPluginExample example);

    int updateByExample(@Param("record") MyPlugin record, @Param("example") MyPluginExample example);

    int updateByPrimaryKeySelective(MyPlugin record);

    int updateByPrimaryKey(MyPlugin record);
}