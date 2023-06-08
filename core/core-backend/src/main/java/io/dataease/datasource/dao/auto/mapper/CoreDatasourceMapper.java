package io.dataease.datasource.dao.auto.mapper;

import io.dataease.datasource.dao.auto.entity.CoreDatasource;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author fit2cloud
 * @since 2023-04-18
 */
public interface CoreDatasourceMapper extends BaseMapper<CoreDatasource> {

    @Insert("INSERT INTO `core_datasource` (`id`, `name`, `description`, `type`, `configuration`, `create_time`, `update_time`, `create_by`, `status`, `qrtz_instance`, `task_status`) VALUES (#{id}, #{name}, #{description}, #{type}, #{configuration}, #{createTime}, #{updateTime}, #{createBy}, #{status}, #{qrtzInstance}, #{taskStatus})")
    int insert(CoreDatasource coreDatasource);
}
