package io.dataease.service.panel;

import io.dataease.ext.ExtPanelLinkJumpMapper;
import io.dataease.ext.ExtPanelViewLinkageMapper;
import io.dataease.dto.panel.linkJump.PanelLinkJumpBaseRequest;
import io.dataease.dto.panel.linkJump.PanelLinkJumpBaseResponse;
import io.dataease.dto.panel.linkJump.PanelLinkJumpDTO;
import io.dataease.dto.panel.linkJump.PanelLinkJumpInfoDTO;
import io.dataease.plugins.common.base.domain.DatasetTableField;
import io.dataease.plugins.common.base.mapper.PanelLinkJumpInfoMapper;
import io.dataease.plugins.common.base.mapper.PanelLinkJumpMapper;
import io.dataease.plugins.common.base.mapper.PanelLinkJumpTargetViewInfoMapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Author: wangjiahao
 * Date: 2021/10/25
 * Description:
 */
@Service
public class PanelLinkJumpService {

    @Resource
    private ExtPanelViewLinkageMapper extPanelViewLinkageMapper;

    @Resource
    private ExtPanelLinkJumpMapper extPanelLinkJumpMapper;

    @Resource
    private PanelLinkJumpMapper panelLinkJumpMapper;

    @Resource
    private PanelLinkJumpInfoMapper panelLinkJumpInfoMapper;

    @Resource
    private PanelLinkJumpTargetViewInfoMapper panelLinkJumpTargetViewInfoMapper;


    public List<DatasetTableField> getViewFields(String viewId) {
        return extPanelViewLinkageMapper.queryTableFieldWithViewId(viewId);
    }

    public List<PanelLinkJumpDTO> queryWithPanelId(String panelId) {
        return extPanelLinkJumpMapper.queryWithPanelId(panelId);
    }

    //获取仪表板的跳转信息
    public PanelLinkJumpBaseResponse queryPanelJumpInfo(String panelId) {
        Map<String, PanelLinkJumpInfoDTO> resultBase = new HashMap<>();
        List<PanelLinkJumpDTO> resultLinkJumpList = extPanelLinkJumpMapper.queryWithPanelId(panelId);
        Optional.ofNullable(resultLinkJumpList).orElse(new ArrayList<>()).forEach(resultLinkJump -> {
            if (resultLinkJump.getChecked()) {
                String sourceViewId = resultLinkJump.getSourceViewId();
                Optional.ofNullable(resultLinkJump.getLinkJumpInfoArray()).orElse(new ArrayList<>()).forEach(linkJumpInfo -> {
                    if (linkJumpInfo.getChecked()) {
                        String sourceJumpInfo = sourceViewId + "#" + linkJumpInfo.getSourceFieldId();
                        // 内部仪表板跳转 需要设置好仪表板ID
                        if ("inner".equals(linkJumpInfo.getLinkType())) {
                            if (StringUtils.isNotEmpty(linkJumpInfo.getTargetPanelId())) {
                                resultBase.put(sourceJumpInfo, linkJumpInfo);
                            }
                        } else {
                            // 外部跳转
                            resultBase.put(sourceJumpInfo, linkJumpInfo);
                        }
                    }
                });
            }
        });
        return new PanelLinkJumpBaseResponse(resultBase, null);
    }

    public PanelLinkJumpDTO queryWithView(String panelId, String viewId) {
        return extPanelLinkJumpMapper.queryWithViewId(panelId, viewId);
    }

    @Transactional
    public void updateJumpSet(PanelLinkJumpDTO jumpDTO) {
        String panelId = jumpDTO.getSourcePanelId();
        String viewId = jumpDTO.getSourceViewId();
        Assert.notNull(panelId, "panelId cannot be null");
        Assert.notNull(viewId, "viewId cannot be null");
        //清理原有数据
        extPanelLinkJumpMapper.deleteJumpTargetViewInfo(panelId, viewId);
        extPanelLinkJumpMapper.deleteJumpInfo(panelId, viewId);
        extPanelLinkJumpMapper.deleteJump(panelId, viewId);

        // 插入新的数据
        String linkJumpId = UUID.randomUUID().toString();
        jumpDTO.setId(linkJumpId);
        panelLinkJumpMapper.insertSelective(jumpDTO);
        Optional.ofNullable(jumpDTO.getLinkJumpInfoArray()).orElse(new ArrayList<>()).forEach(linkJumpInfo -> {
            String linkJumpInfoId = UUID.randomUUID().toString();
            linkJumpInfo.setId(linkJumpInfoId);
            linkJumpInfo.setLinkJumpId(linkJumpId);
            panelLinkJumpInfoMapper.insertSelective(linkJumpInfo);
            Optional.ofNullable(linkJumpInfo.getTargetViewInfoList()).orElse(new ArrayList<>()).forEach(targetViewInfo -> {
                String targetViewInfoId = UUID.randomUUID().toString();
                targetViewInfo.setTargetId(targetViewInfoId);
                targetViewInfo.setLinkJumpInfoId(linkJumpInfoId);
                panelLinkJumpTargetViewInfoMapper.insertSelective(targetViewInfo);
            });
        });
    }

    public PanelLinkJumpBaseResponse queryTargetPanelJumpInfo(PanelLinkJumpBaseRequest request) {
        List<PanelLinkJumpDTO> result = extPanelLinkJumpMapper.getTargetPanelJumpInfo(request);
        return new PanelLinkJumpBaseResponse(null, Optional.ofNullable(result).orElse(new ArrayList<>()).stream().collect(Collectors.toMap(PanelLinkJumpDTO::getSourceInfo, PanelLinkJumpDTO::getTargetInfoList)));
    }

}
