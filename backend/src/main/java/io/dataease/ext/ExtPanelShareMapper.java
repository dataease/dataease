package io.dataease.ext;

import io.dataease.controller.request.panel.PanelShareRemoveRequest;
import io.dataease.controller.request.panel.PanelShareSearchRequest;
import io.dataease.dto.panel.PanelShareOutDTO;
import io.dataease.dto.panel.PanelSharePo;
import io.dataease.plugins.common.base.domain.PanelShare;
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
