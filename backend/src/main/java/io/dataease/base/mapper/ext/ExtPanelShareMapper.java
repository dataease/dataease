package io.dataease.base.mapper.ext;

import io.dataease.base.domain.PanelShare;
import io.dataease.base.mapper.ext.query.GridExample;
import io.dataease.controller.request.panel.PanelShareRemoveRequest;
import io.dataease.dto.panel.PanelShareOutDTO;
import io.dataease.dto.panel.PanelSharePo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ExtPanelShareMapper {

    int batchInsert(@Param("shares") List<PanelShare> shares, @Param("userName") String userName);

    int batchDelete(@Param("shareIds") List<Long> shareIds);

    List<PanelSharePo> query(Map<String, Object> param);

    List<PanelSharePo> queryOut(String userName);

    List<PanelShare> queryWithResource(GridExample example);

    List<PanelShareOutDTO> queryTargets(String panelId);

    void removeShares(@Param("request") PanelShareRemoveRequest request);

    List<Long> queryUserIdWithRoleIds(Map<String, List<Long>> param);

    List<Long> queryUserIdWithDeptIds(Map<String, List<Long>> param);
}
