package io.dataease.ext;

import io.dataease.ext.query.GridExample;
import io.dataease.dto.panel.PanelStoreDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtPanelStoreMapper {

    List<PanelStoreDto> query(@Param("uid") Long uid);
}
