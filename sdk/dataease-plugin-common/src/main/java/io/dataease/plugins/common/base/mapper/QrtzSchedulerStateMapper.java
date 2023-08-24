package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.QrtzSchedulerState;
import io.dataease.plugins.common.base.domain.QrtzSchedulerStateExample;
import io.dataease.plugins.common.base.domain.QrtzSchedulerStateKey;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface QrtzSchedulerStateMapper {
    long countByExample(QrtzSchedulerStateExample example);

    int deleteByExample(QrtzSchedulerStateExample example);

    int deleteByPrimaryKey(QrtzSchedulerStateKey key);

    int insert(QrtzSchedulerState record);

    int insertSelective(QrtzSchedulerState record);

    List<QrtzSchedulerState> selectByExample(QrtzSchedulerStateExample example);

    QrtzSchedulerState selectByPrimaryKey(QrtzSchedulerStateKey key);

    int updateByExampleSelective(@Param("record") QrtzSchedulerState record, @Param("example") QrtzSchedulerStateExample example);

    int updateByExample(@Param("record") QrtzSchedulerState record, @Param("example") QrtzSchedulerStateExample example);

    int updateByPrimaryKeySelective(QrtzSchedulerState record);

    int updateByPrimaryKey(QrtzSchedulerState record);
}