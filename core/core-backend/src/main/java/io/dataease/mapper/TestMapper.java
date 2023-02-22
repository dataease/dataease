package io.dataease.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface TestMapper {

    @Select("select * from sys_user")
    List<Map> queryTest();

}
