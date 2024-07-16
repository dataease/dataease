package io.dataease.traffic.dao.mapper;

import io.dataease.traffic.dao.entity.CoreApiTraffic;
import org.apache.ibatis.annotations.*;

@Mapper
public interface CoreApiTrafficMapper {

    @Select("select `alive` from `core_api_traffic` where `api` = #{api}")
    int getAlive(@Param("api") String api);

    @Update("update `core_api_traffic` set alive = alive + 1 where `api` = #{api}")
    void upgrade(@Param("api") String api);

    @Insert("insert into core_api_traffic values(#{id}, #{api}, #{threshold}, 0)")
    void insert(CoreApiTraffic traffic);

    @Select("select count(*) from core_api_traffic where api = #{api}")
    Integer apiCount(@Param("api") String api);

    @Update("""
        update `core_api_traffic` set alive = 
        CASE WHEN alive > 0 THEN alive - 1
        ELSE alive END 
        where `api` = #{api}
    """)
    void releaseAlive(@Param("api") String api);

    @Delete("delete from core_api_traffic")
    void cleanTraffic();
}
