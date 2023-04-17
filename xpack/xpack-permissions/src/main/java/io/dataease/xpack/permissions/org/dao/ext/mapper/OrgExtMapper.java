package io.dataease.xpack.permissions.org.dao.ext.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.xpack.permissions.org.dao.auto.entity.PerOrg;
import io.dataease.xpack.permissions.org.dao.auto.mapper.PerOrgMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrgExtMapper extends PerOrgMapper {

    @Select("""
            select distinct po.*
            from per_org po 
            left join per_user_role pur on pur.oid = po.id 
            ${ew.customSqlSegment} 
            """)
    List<PerOrg> queryByUserId(@Param("ew") QueryWrapper queryWrapper);

    @Select("select count(*) as bcount from per_busi_resource where org_id = #{oid}")
    int busiCount(@Param("oid") Long oid);
}
