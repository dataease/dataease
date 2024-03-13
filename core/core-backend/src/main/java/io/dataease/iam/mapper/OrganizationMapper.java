package io.dataease.iam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.dataease.iam.pojo.entity.OrganizationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Description: OrganizationMapper
 *
 */
@Mapper
public interface OrganizationMapper extends BaseMapper<OrganizationEntity> {
    OrganizationEntity selectInfoById(@Param("id") Long id);

    OrganizationEntity selectInfoByUuid(@Param("organizationUuid") String organizationUuid);

    Long selectRoleInfoByUuid(@Param("organizationUuid") String organizationUuid);

}
