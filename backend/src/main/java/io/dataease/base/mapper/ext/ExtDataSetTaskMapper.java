package io.dataease.base.mapper.ext;

import io.dataease.base.domain.DatasetTableTaskLog;
import io.dataease.base.mapper.ext.query.GridExample;
import io.dataease.dto.dataset.DataSetTaskDTO;
import io.dataease.dto.dataset.DataSetTaskLogDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Author gin
 * @Date 2021/3/9 3:26 下午
 */
@Mapper
public interface ExtDataSetTaskMapper {
    List<DataSetTaskLogDTO> list(GridExample example);

    List<DataSetTaskDTO> taskList(GridExample example);
}
