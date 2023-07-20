package io.dataease.visualization.server;

import io.dataease.api.visualization.VisualizationLinkJumpApi;
import io.dataease.api.visualization.dto.VisualizationLinkJumpDTO;
import io.dataease.api.visualization.dto.VisualizationLinkJumpInfoDTO;
import io.dataease.api.visualization.request.VisualizationLinkJumpBaseRequest;
import io.dataease.api.visualization.response.VisualizationLinkJumpBaseResponse;
import io.dataease.api.visualization.vo.VisualizationViewTableVO;
import io.dataease.dto.dataset.DatasetTableFieldDTO;
import io.dataease.utils.BeanUtils;
import io.dataease.visualization.dao.auto.entity.VisualizationLinkJump;
import io.dataease.visualization.dao.auto.entity.VisualizationLinkJumpInfo;
import io.dataease.visualization.dao.auto.entity.VisualizationLinkJumpTargetViewInfo;
import io.dataease.visualization.dao.auto.mapper.VisualizationLinkJumpInfoMapper;
import io.dataease.visualization.dao.auto.mapper.VisualizationLinkJumpMapper;
import io.dataease.visualization.dao.auto.mapper.VisualizationLinkJumpTargetViewInfoMapper;
import io.dataease.visualization.ext.ExtVisualizationLinkJumpMapper;
import io.dataease.visualization.ext.ExtVisualizationLinkageMapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author : WangJiaHao
 * @date : 2023/7/18 14:31
 */
@RestController
@RequestMapping("linkJump")
public class VisualizationLinkJumpService implements VisualizationLinkJumpApi {

    @Resource
    private ExtVisualizationLinkageMapper extVisualizationLinkageMapper;

    @Resource
    private ExtVisualizationLinkJumpMapper extVisualizationLinkJumpMapper;

    @Resource
    private VisualizationLinkJumpMapper visualizationLinkJumpMapper;

    @Resource
    private VisualizationLinkJumpInfoMapper visualizationLinkJumpInfoMapper;

    @Resource
    private VisualizationLinkJumpTargetViewInfoMapper visualizationLinkJumpTargetViewInfoMapper;


    @Override
    public List<DatasetTableFieldDTO> getTableFieldWithViewId(String viewId) {
        return extVisualizationLinkageMapper.queryTableFieldWithViewId(viewId);
    }

    public List<VisualizationLinkJumpDTO> queryWithDvId(String dvId) {
        return extVisualizationLinkJumpMapper.queryWithDvId(dvId);
    }

    //获取仪表板的跳转信息
    @Override
    public VisualizationLinkJumpBaseResponse queryVisualizationJumpInfo(String dvId) {
        Map<String, VisualizationLinkJumpInfoDTO> resultBase = new HashMap<>();
        List<VisualizationLinkJumpDTO> resultLinkJumpList = extVisualizationLinkJumpMapper.queryWithDvId(dvId);
        Optional.ofNullable(resultLinkJumpList).orElse(new ArrayList<>()).forEach(resultLinkJump -> {
            if (resultLinkJump.getChecked()) {
                String sourceViewId = resultLinkJump.getSourceViewId();
                Optional.ofNullable(resultLinkJump.getLinkJumpInfoArray()).orElse(new ArrayList<>()).forEach(linkJumpInfo -> {
                    if (linkJumpInfo.getChecked()) {
                        String sourceJumpInfo = sourceViewId + "#" + linkJumpInfo.getSourceFieldId();
                        // 内部仪表板跳转 需要设置好仪表板ID
                        if ("inner".equals(linkJumpInfo.getLinkType())) {
                            if (StringUtils.isNotEmpty(linkJumpInfo.getTargetDvId())) {
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
        return new VisualizationLinkJumpBaseResponse(resultBase, null);
    }
    @Override
    public VisualizationLinkJumpDTO queryWithViewId(String dvId, String viewId) {
        return extVisualizationLinkJumpMapper.queryWithViewId(dvId, viewId);
    }

    @Transactional
    @Override
    public void updateJumpSet(VisualizationLinkJumpDTO jumpDTO) {
        String dvId = jumpDTO.getSourceDvId();
        String viewId = jumpDTO.getSourceViewId();
        Assert.notNull(dvId, "dvId cannot be null");
        Assert.notNull(viewId, "viewId cannot be null");
        //清理原有数据
        extVisualizationLinkJumpMapper.deleteJumpTargetViewInfo(dvId, viewId);
        extVisualizationLinkJumpMapper.deleteJumpInfo(dvId, viewId);
        extVisualizationLinkJumpMapper.deleteJump(dvId, viewId);

        // 插入新的数据
        String linkJumpId = UUID.randomUUID().toString();
        jumpDTO.setId(linkJumpId);
        VisualizationLinkJump insertParam = new VisualizationLinkJump();
        BeanUtils.copyBean(insertParam,jumpDTO);
        visualizationLinkJumpMapper.insert(insertParam);
        Optional.ofNullable(jumpDTO.getLinkJumpInfoArray()).orElse(new ArrayList<>()).forEach(linkJumpInfo -> {
            String linkJumpInfoId = UUID.randomUUID().toString();
            linkJumpInfo.setId(linkJumpInfoId);
            linkJumpInfo.setLinkJumpId(linkJumpId);
            VisualizationLinkJumpInfo insertJumpInfoParam = new VisualizationLinkJumpInfo();
            BeanUtils.copyBean(insertJumpInfoParam,linkJumpInfo);
            visualizationLinkJumpInfoMapper.insert(insertJumpInfoParam);
            Optional.ofNullable(linkJumpInfo.getTargetViewInfoList()).orElse(new ArrayList<>()).forEach(targetViewInfo -> {
                String targetViewInfoId = UUID.randomUUID().toString();
                targetViewInfo.setTargetId(targetViewInfoId);
                targetViewInfo.setLinkJumpInfoId(linkJumpInfoId);
                VisualizationLinkJumpTargetViewInfo insertTargetViewInfoParam = new VisualizationLinkJumpTargetViewInfo();
                BeanUtils.copyBean(insertTargetViewInfoParam,targetViewInfo);
                visualizationLinkJumpTargetViewInfoMapper.insert(insertTargetViewInfoParam);
            });
        });
    }

    @Override
    public VisualizationLinkJumpBaseResponse queryTargetVisualizationJumpInfo(VisualizationLinkJumpBaseRequest request) {
        List<VisualizationLinkJumpDTO> result = extVisualizationLinkJumpMapper.getTargetVisualizationJumpInfo(request);
        return new VisualizationLinkJumpBaseResponse(null, Optional.ofNullable(result).orElse(new ArrayList<>()).stream().collect(Collectors.toMap(VisualizationLinkJumpDTO::getSourceInfo, VisualizationLinkJumpDTO::getTargetInfoList)));
    }

    @Override
    public List<VisualizationViewTableVO> viewTableDetailList(String dvId) {
        return extVisualizationLinkJumpMapper.getViewTableDetails(dvId);
    }
}
