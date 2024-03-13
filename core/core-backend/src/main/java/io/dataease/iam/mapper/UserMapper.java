package io.dataease.iam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.dataease.iam.pojo.entity.OrganizationEntity;
import io.dataease.iam.pojo.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Description: UserMapper
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
    Long getPerId(@Param("account") String account);
}
