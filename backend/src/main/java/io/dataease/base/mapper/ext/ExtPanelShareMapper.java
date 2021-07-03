package io.dataease.base.mapper.ext;

import io.dataease.base.domain.PanelShare;
import io.dataease.base.mapper.ext.query.GridExample;
import io.dataease.dto.panel.PanelSharePo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ExtPanelShareMapper {

    int batchInsert(@Param("shares") List<PanelShare> shares);

    List<PanelSharePo> query(Map<String, Object> param);

    List<PanelShare> queryWithResource(GridExample example);

    List<Long> queryUserIdWithRoleIds(Map<String, List<Long>> param);

    List<Long> queryUserIdWithDeptIds(Map<String, List<Long>> param);
}
