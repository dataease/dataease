package io.dataease.ext;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ExtMenuMapper {

    @Update(" update sys_menu set sub_count = sub_count+1 where menu_id = #{menuId} ")
    int incrementalSubcount(@Param("menuId") Long menuId);

    @Update(" update sys_menu set sub_count = sub_count-1 where menu_id = #{menuId} and sub_count > 0")
    int decreasingSubcount(@Param("menuId") Long menuId);
}
