package io.dataease.base.mapper.ext;

import io.dataease.base.domain.PanelShare;
import io.dataease.base.mapper.ext.query.GridExample;
import io.dataease.dto.panel.PanelShareDto;
import io.dataease.dto.panel.PanelSharePo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtPanelShareMapper {

    int batchInsert(@Param("shares") List<PanelShare> shares);

    List<PanelSharePo> query(GridExample example);

    List<PanelShare> queryWithResource(GridExample example);
}
