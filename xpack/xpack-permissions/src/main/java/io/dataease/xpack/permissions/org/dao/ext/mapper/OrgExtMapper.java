package io.dataease.xpack.permissions.org.dao.ext.mapper;

import io.dataease.xpack.permissions.org.dao.auto.entity.PerOrg;
import io.dataease.xpack.permissions.org.dao.auto.mapper.PerOrgMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrgExtMapper extends PerOrgMapper {

    @Select("""
            select po.*
            from per_org po 
            left join per_role pr on pr.org_id = po.id 
            left join per_user_role pur on pur.rid = pr.id 
            where pur.uid = #{uId}
            """)
    List<PerOrg> queryByUserId(@Param("uId") Long uId);
}
