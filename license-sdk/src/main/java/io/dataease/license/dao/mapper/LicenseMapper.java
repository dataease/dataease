package io.dataease.license.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.dataease.license.dao.po.LicensePO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LicenseMapper extends BaseMapper<LicensePO> {
}
