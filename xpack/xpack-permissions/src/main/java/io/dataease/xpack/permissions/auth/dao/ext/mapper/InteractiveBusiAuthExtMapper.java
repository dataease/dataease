package io.dataease.xpack.permissions.auth.dao.ext.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.xpack.permissions.auth.dao.ext.entity.BusiPerPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface InteractiveBusiAuthExtMapper {

    @Select("""
            select distinct pbr.id, pbr.name, pbr.leaf, pbr.pid, pabu.weight, pabu.uid as target_id
            from 
            per_busi_resource pbr 
            left join per_auth_busi_user pabu on pbr.id = pabu.resource_id
            ${ew.customSqlSegment} 
            """)
    List<BusiPerPO> queryWithUid(@Param("ew") QueryWrapper queryWrapper);


    @Select("""
            select distinct pbr.id, pbr.name, bpr.leaf, pbr.pid, pabr.weight, pabr,rid as target_id
            from 
            per_busi_resource pbr 
            left join per_auth_busi_role pabr on pbr.id = pabr.resource_id
            ${ew.customSqlSegment} 
            """)
    List<BusiPerPO> queryWithRid(@Param("ew") QueryWrapper queryWrapper);
}
