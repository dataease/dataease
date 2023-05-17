package io.dataease.ext;

import io.dataease.controller.sys.response.MsgGridDto;
import io.dataease.plugins.common.base.domain.SysMsgExample;
import io.dataease.plugins.common.base.domain.SysMsgSetting;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ExtSysMsgMapper {

    @Update({
            "<script>",
            "update sys_msg set status = 1, read_time = #{time} where msg_id in ",
            "<foreach collection='msgIds' item='msgId' open='(' separator=',' close=')' >",
            " #{msgId}",
            "</foreach>",
            " and user_id = #{uid}",
            "</script>"
    })
    int batchStatus(@Param("msgIds") List<Long> msgIds, @Param("time") Long time, @Param("uid") Long uid);

    @Delete({
            "<script>",
            "delete from sys_msg where msg_id in ",
            "<foreach collection='msgIds' item='msgId' open='(' separator=',' close=')' >",
            " #{msgId}",
            "</foreach>",
            " and user_id = #{uid} ",
            "</script>"
    })
    int batchDelete(@Param("msgIds") List<Long> msgIds, @Param("uid") Long uid);

    int batchInsert(@Param("settings") List<SysMsgSetting> settings);

    List<MsgGridDto> queryGrid(SysMsgExample example);

}
