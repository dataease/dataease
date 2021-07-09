package io.dataease.base.mapper.ext;

import io.dataease.base.domain.SysMsgExample;
import io.dataease.base.mapper.ext.query.GridExample;
import io.dataease.controller.message.dto.MsgGridDto;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ExtSysMsgMapper {


    @Update({
        "<script>",
            "update sys_msg set status = 1 where msg_id in ",
            "<foreach collection='msgIds' item='msgId' open='(' separator=',' close=')' >",
                " #{msgId}",
            "</foreach>",
        "</script>"
    })
    int batchStatus(@Param("msgIds") List<Long> msgIds);


    @Delete({
            "<script>",
                "delete from sys_msg where msg_id in ",
                "<foreach collection='msgIds' item='msgId' open='(' separator=',' close=')' >",
                    " #{msgId}",
                "</foreach>",
            "</script>"
    })
    int batchDelete(@Param("msgIds") List<Long> msgIds);


    List<MsgGridDto> queryGrid(SysMsgExample example);


}
