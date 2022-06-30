package io.dataease.ext;

import io.dataease.plugins.common.base.domain.SysUserAssist;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ExtSysUserAssistMapper {

    @Insert("insert into sys_user_assist values(#{po.userId}, #{po.needFirstNoti})")
    void save(@Param("po") SysUserAssist po);

    @Select("select * from sys_user_assist where user_id = #{userId}")
    SysUserAssist query(@Param("userId") Long userId);
}
