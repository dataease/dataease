package io.dataease.share.dao.ext.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.dataease.share.dao.ext.po.XpackSharePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface XpackShareExtMapper {

    @Select("""
            select
            s.id as share_id,
            v.id as resource_id,
            v.mobile_layout as ext_flag,
            v.type,
            s.creator,
            s.time,
            s.exp,
            v.name
            from xpack_share s
            left join data_visualization_info v on s.resource_id = v.id
            ${ew.customSqlSegment}
            """)
    IPage<XpackSharePO> query(IPage<XpackSharePO> page, @Param("ew") QueryWrapper<Object> ew);

    @Select("select type from data_visualization_info where id = #{id}")
    String visualizationType(@Param("id") Long id);

    @Update("update core_share_ticket set uuid = #{ticketUuid} where uuid = #{originUuid}")
    void updateTicketUuid(@Param("originUuid") String originUuid, @Param("ticketUuid") String ticketUuid);
}
