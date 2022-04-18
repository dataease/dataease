package io.datains.base.mapper.ext;

import io.datains.base.domain.PanelShare;
import io.datains.controller.request.panel.PanelShareRemoveRequest;
import io.datains.controller.request.panel.PanelShareSearchRequest;
import io.datains.dto.panel.PanelShareOutDTO;
import io.datains.dto.panel.PanelSharePo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ExtPanelShareMapper {

    int batchInsert(@Param("shares") List<PanelShare> shares, @Param("userName") String userName);

    int batchDelete(@Param("shareIds") List<Long> shareIds);

    List<PanelSharePo> query(Map<String, Object> param);

    List<PanelSharePo> queryOut(String userName);

    List<PanelShare> queryWithResource(PanelShareSearchRequest request);

    List<PanelShareOutDTO> queryTargets(@Param("panelId") String panelId, @Param("userName") String userName);

    void removeShares(@Param("request") PanelShareRemoveRequest request);

    List<Long> queryUserIdWithRoleIds(Map<String, List<Long>> param);

    List<Long> queryUserIdWithDeptIds(Map<String, List<Long>> param);
}
