package io.dataease.visualization.server;

import io.dataease.api.visualization.VisualizationLinkJumpApi;
import io.dataease.api.visualization.dto.VisualizationComponentDTO;
import io.dataease.api.visualization.dto.VisualizationLinkJumpDTO;
import io.dataease.api.visualization.dto.VisualizationLinkJumpInfoDTO;
import io.dataease.api.visualization.request.VisualizationLinkJumpBaseRequest;
import io.dataease.api.visualization.response.VisualizationLinkJumpBaseResponse;
import io.dataease.api.visualization.vo.VisualizationViewTableVO;
import io.dataease.chart.dao.auto.entity.CoreChartView;
import io.dataease.chart.dao.auto.mapper.CoreChartViewMapper;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.utils.AuthUtils;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.IDUtils;
import io.dataease.visualization.dao.auto.entity.DataVisualizationInfo;
import io.dataease.visualization.dao.auto.entity.VisualizationLinkJump;
import io.dataease.visualization.dao.auto.entity.VisualizationLinkJumpInfo;
import io.dataease.visualization.dao.auto.entity.VisualizationLinkJumpTargetViewInfo;
import io.dataease.visualization.dao.auto.mapper.DataVisualizationInfoMapper;
import io.dataease.visualization.dao.auto.mapper.VisualizationLinkJumpInfoMapper;
import io.dataease.visualization.dao.auto.mapper.VisualizationLinkJumpMapper;
import io.dataease.visualization.dao.auto.mapper.VisualizationLinkJumpTargetViewInfoMapper;
import io.dataease.visualization.dao.ext.mapper.ExtVisualizationLinkJumpMapper;
import io.dataease.visualization.dao.ext.mapper.ExtVisualizationLinkageMapper;
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

    @Resource
    private CoreChartViewMapper coreChartViewMapper;

    @Resource
    private DataVisualizationInfoMapper dataVisualizationInfoMapper;

    @Override
    public List<DatasetTableFieldDTO> getTableFieldWithViewId(Long viewId) {
        return extVisualizationLinkageMapper.queryTableFieldWithViewId(viewId);
    }

    //获取仪表板的跳转信息
    @Override
    public VisualizationLinkJumpBaseResponse queryVisualizationJumpInfo(Long dvId) {
        Map<String, VisualizationLinkJumpInfoDTO> resultBase = new HashMap<>();
        List<VisualizationLinkJumpDTO> resultLinkJumpList = extVisualizationLinkJumpMapper.queryWithDvId(dvId, AuthUtils.getUser().getUserId());
        Optional.ofNullable(resultLinkJumpList).orElse(new ArrayList<>()).forEach(resultLinkJump -> {
            if (resultLinkJump.getChecked()) {
                Long sourceViewId = resultLinkJump.getSourceViewId();
                Optional.ofNullable(resultLinkJump.getLinkJumpInfoArray()).orElse(new ArrayList<>()).forEach(linkJumpInfo -> {
                    if (linkJumpInfo.getChecked()) {
                        String sourceJumpInfo = sourceViewId + "#" + linkJumpInfo.getSourceFieldId();
                        // 内部仪表板跳转 需要设置好仪表板ID
                        if ("inner".equals(linkJumpInfo.getLinkType())) {
                            if (linkJumpInfo.getTargetDvId() != null) {
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
    public VisualizationLinkJumpDTO queryWithViewId(Long dvId, Long viewId) {
        return extVisualizationLinkJumpMapper.queryWithViewId(dvId, viewId, AuthUtils.getUser().getUserId());
    }

    @Transactional
    @Override
    public void updateJumpSet(VisualizationLinkJumpDTO jumpDTO) {
        Long dvId = jumpDTO.getSourceDvId();
        Long viewId = jumpDTO.getSourceViewId();
        Assert.notNull(dvId, "dvId cannot be null");
        Assert.notNull(viewId, "viewId cannot be null");
        //清理原有数据
        extVisualizationLinkJumpMapper.deleteJumpTargetViewInfo(dvId, viewId);
        extVisualizationLinkJumpMapper.deleteJumpInfo(dvId, viewId);
        extVisualizationLinkJumpMapper.deleteJump(dvId, viewId);

        // 插入新的数据
        Long linkJumpId = IDUtils.snowID();
        jumpDTO.setId(linkJumpId);
        VisualizationLinkJump insertParam = new VisualizationLinkJump();
        BeanUtils.copyBean(insertParam, jumpDTO);
        visualizationLinkJumpMapper.insert(insertParam);
        Optional.ofNullable(jumpDTO.getLinkJumpInfoArray()).orElse(new ArrayList<>()).forEach(linkJumpInfo -> {
            Long linkJumpInfoId = IDUtils.snowID();
            linkJumpInfo.setId(linkJumpInfoId);
            linkJumpInfo.setLinkJumpId(linkJumpId);
            VisualizationLinkJumpInfo insertJumpInfoParam = new VisualizationLinkJumpInfo();
            BeanUtils.copyBean(insertJumpInfoParam, linkJumpInfo);
            visualizationLinkJumpInfoMapper.insert(insertJumpInfoParam);
            Optional.ofNullable(linkJumpInfo.getTargetViewInfoList()).orElse(new ArrayList<>()).forEach(targetViewInfo -> {
                Long targetViewInfoId = IDUtils.snowID();
                targetViewInfo.setTargetId(targetViewInfoId);
                targetViewInfo.setLinkJumpInfoId(linkJumpInfoId);
                VisualizationLinkJumpTargetViewInfo insertTargetViewInfoParam = new VisualizationLinkJumpTargetViewInfo();
                BeanUtils.copyBean(insertTargetViewInfoParam, targetViewInfo);
                visualizationLinkJumpTargetViewInfoMapper.insert(insertTargetViewInfoParam);
            });
        });
    }

    @Override
    public VisualizationLinkJumpBaseResponse queryTargetVisualizationJumpInfo(VisualizationLinkJumpBaseRequest request) {
        List<VisualizationLinkJumpDTO> result = extVisualizationLinkJumpMapper.getTargetVisualizationJumpInfo(request);
        return new VisualizationLinkJumpBaseResponse(null, Optional.ofNullable(result).orElse(new ArrayList<>()).stream().filter(item -> StringUtils.isNotEmpty(item.getSourceInfo())).collect(Collectors.toMap(VisualizationLinkJumpDTO::getSourceInfo, VisualizationLinkJumpDTO::getTargetInfoList)));
    }

    @Override
    public VisualizationComponentDTO viewTableDetailList(Long dvId) {
        DataVisualizationInfo dvInfo = dataVisualizationInfoMapper.selectById(dvId);
        List<VisualizationViewTableVO> result;
        String componentData;
        if (dvInfo != null) {
            result = extVisualizationLinkJumpMapper.getViewTableDetails(dvId).stream().filter(viewTableInfo -> dvInfo.getComponentData().indexOf(viewTableInfo.getId().toString()) > -1).collect(Collectors.toList());
            componentData = dvInfo.getComponentData();
        } else {
            result = new ArrayList<>();
            componentData = "[]";
        }
        return new VisualizationComponentDTO(componentData,result);

    }

    @Override
    public VisualizationLinkJumpBaseResponse updateJumpSetActive(VisualizationLinkJumpBaseRequest request) {
        CoreChartView coreChartView = new CoreChartView();
        coreChartView.setId(Long.valueOf(request.getSourceViewId()));
        coreChartView.setJumpActive(request.getActiveStatus());
        coreChartViewMapper.updateById(coreChartView);
        return queryVisualizationJumpInfo(request.getSourceDvId());
    }

    @Override
    public void removeJumpSet(VisualizationLinkJumpDTO jumpDTO) {
        //清理原有数据
        extVisualizationLinkJumpMapper.deleteJumpTargetViewInfo(jumpDTO.getSourceDvId(), jumpDTO.getSourceViewId());
        extVisualizationLinkJumpMapper.deleteJumpInfo(jumpDTO.getSourceDvId(), jumpDTO.getSourceViewId());
        extVisualizationLinkJumpMapper.deleteJump(jumpDTO.getSourceDvId(), jumpDTO.getSourceViewId());
    }

}
