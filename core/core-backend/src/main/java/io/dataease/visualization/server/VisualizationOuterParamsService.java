package io.dataease.visualization.server;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import io.dataease.api.dataset.vo.CoreDatasetGroupVO;
import io.dataease.api.dataset.vo.CoreDatasetTableFieldVO;
import io.dataease.api.visualization.VisualizationOuterParamsApi;
import io.dataease.api.visualization.dto.VisualizationOuterParamsDTO;
import io.dataease.api.visualization.dto.VisualizationOuterParamsInfoDTO;
import io.dataease.api.visualization.response.VisualizationOuterParamsBaseResponse;
import io.dataease.auth.DeLinkPermit;
import io.dataease.constant.CommonConstants;
import io.dataease.dataset.dao.auto.entity.CoreDatasetTable;
import io.dataease.dataset.dao.auto.mapper.CoreDatasetTableMapper;
import io.dataease.constant.DeTypeConstants;
import io.dataease.dataset.utils.FieldUtils;
import io.dataease.extensions.view.dto.SqlVariableDetails;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.JsonUtil;
import io.dataease.visualization.dao.auto.entity.*;
import io.dataease.visualization.dao.auto.mapper.*;
import io.dataease.visualization.dao.ext.mapper.ExtVisualizationOuterParamsMapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author : WangJiaHao
 * @date : 2024/3/11 09:44
 */
@RestController
@RequestMapping("outerParams")
public class VisualizationOuterParamsService implements VisualizationOuterParamsApi {

    @Resource
    private ExtVisualizationOuterParamsMapper extOuterParamsMapper;
    @Resource
    private VisualizationOuterParamsMapper outerParamsMapper;
    @Resource
    private SnapshotVisualizationOuterParamsMapper snapshotOuterParamsMapper;

    @Resource
    private VisualizationOuterParamsInfoMapper outerParamsInfoMapper;

    @Resource
    private SnapshotVisualizationOuterParamsInfoMapper snapshotOuterParamsInfoMapper;

    @Resource
    private VisualizationOuterParamsTargetViewInfoMapper outerParamsTargetViewInfoMapper;

    @Resource
    private SnapshotVisualizationOuterParamsTargetViewInfoMapper snapshotOuterParamsTargetViewInfoMapper;

    @Resource
    private CoreDatasetTableMapper coreDatasetTableMapper;
    @Autowired
    private DataVisualizationServer dataVisualizationServer;
    @Autowired
    private SnapshotDataVisualizationInfoMapper snapshotDataVisualizationInfoMapper;


    @Override
    public VisualizationOuterParamsDTO queryWithVisualizationId(String visualizationId) {
        VisualizationOuterParamsDTO visualizationOuterParamsDTO = extOuterParamsMapper.queryWithVisualizationIdSnapshot(visualizationId);
        return visualizationOuterParamsDTO;
    }

    @Override
    public void updateOuterParamsSet(VisualizationOuterParamsDTO outerParamsDTO) {
        String visualizationId = outerParamsDTO.getVisualizationId();
        Assert.notNull(visualizationId, "visualizationId cannot be null");
        Map<String,String> paramsInfoNameIdMap = new HashMap<>();
        List<SnapshotVisualizationOuterParamsInfo> paramsInfoNameIdList = extOuterParamsMapper.getVisualizationOuterParamsInfoBase(visualizationId);
        if(!CollectionUtils.isEmpty(paramsInfoNameIdList)){
            paramsInfoNameIdMap = paramsInfoNameIdList.stream()
                    .collect(Collectors.toMap(SnapshotVisualizationOuterParamsInfo::getParamName, SnapshotVisualizationOuterParamsInfo::getParamsInfoId));
        }
        //清理原有数据
        extOuterParamsMapper.deleteOuterParamsTargetWithVisualizationIdSnapshot(visualizationId);
        extOuterParamsMapper.deleteOuterParamsInfoWithVisualizationIdSnapshot(visualizationId);
        extOuterParamsMapper.deleteOuterParamsWithVisualizationIdSnapshot(visualizationId);
        if(CollectionUtils.isEmpty(outerParamsDTO.getOuterParamsInfoArray())){
            return;
        }
        // 插入新的数据
        String paramsId = UUID.randomUUID().toString();
        outerParamsDTO.setParamsId(paramsId);
        SnapshotVisualizationOuterParams newOuterParams = new SnapshotVisualizationOuterParams();
        BeanUtils.copyBean(newOuterParams, outerParamsDTO);
        snapshotOuterParamsMapper.insert(newOuterParams);
        Map<String, String> finalParamsInfoNameIdMap = paramsInfoNameIdMap;
        Optional.ofNullable(outerParamsDTO.getOuterParamsInfoArray()).orElse(new ArrayList<>()).forEach(outerParamsInfo -> {
            String paramsInfoId = finalParamsInfoNameIdMap.get(outerParamsInfo.getParamName());
            if(StringUtils.isEmpty(paramsInfoId)){
                paramsInfoId = UUID.randomUUID().toString();
            }
            outerParamsInfo.setParamsInfoId(paramsInfoId);
            outerParamsInfo.setParamsId(paramsId);
            SnapshotVisualizationOuterParamsInfo newOuterParamsInfo = new SnapshotVisualizationOuterParamsInfo();
            BeanUtils.copyBean(newOuterParamsInfo, outerParamsInfo);
            snapshotOuterParamsInfoMapper.insert(newOuterParamsInfo);
            String finalParamsInfoId = paramsInfoId;
            Optional.ofNullable(outerParamsInfo.getTargetViewInfoList()).orElse(new ArrayList<>()).forEach(targetViewInfo -> {
                String targetViewInfoId = UUID.randomUUID().toString();
                targetViewInfo.setTargetId(targetViewInfoId);
                targetViewInfo.setParamsInfoId(finalParamsInfoId);
                SnapshotVisualizationOuterParamsTargetViewInfo newOuterParamsTargetViewInfo = new SnapshotVisualizationOuterParamsTargetViewInfo();
                BeanUtils.copyBean(newOuterParamsTargetViewInfo, targetViewInfo);
                snapshotOuterParamsTargetViewInfoMapper.insert(newOuterParamsTargetViewInfo);
            });
        });

    }

    @DeLinkPermit
    @Override
    public VisualizationOuterParamsBaseResponse getOuterParamsInfo(String visualizationId) {
        List<VisualizationOuterParamsInfoDTO> result = extOuterParamsMapper.getVisualizationOuterParamsInfo(visualizationId);
        return new VisualizationOuterParamsBaseResponse(Optional.ofNullable(result).orElse(new ArrayList<>()).stream().collect(Collectors.toMap(VisualizationOuterParamsInfoDTO::getSourceInfo, VisualizationOuterParamsInfoDTO::getTargetInfoList)),
                Optional.ofNullable(result).orElse(new ArrayList<>()).stream().collect(Collectors.toMap(VisualizationOuterParamsInfoDTO::getSourceInfo, paramsInfo -> paramsInfo))
        );
    }

    @Override
    public List<CoreDatasetGroupVO> queryDsWithVisualizationId(String visualizationId) {
        List<CoreDatasetGroupVO> result = extOuterParamsMapper.queryDsWithVisualizationId(visualizationId);
        if (!CollectionUtils.isEmpty(result)) {
            List<Long> activeViewIds = dataVisualizationServer.getEnabledViewIds(Long.valueOf(visualizationId), CommonConstants.RESOURCE_TABLE.SNAPSHOT);
            result.forEach(coreDatasetGroupVO -> {
                // 过滤已删除的图表
                if(!CollectionUtils.isEmpty(coreDatasetGroupVO.getDatasetViews())){
                    coreDatasetGroupVO.setDatasetViews(coreDatasetGroupVO.getDatasetViews().stream().filter(item ->activeViewIds.contains(item.getChartId())).toList());
                }
                List<CoreDatasetTableFieldVO> fields = coreDatasetGroupVO.getDatasetFields();
                QueryWrapper<CoreDatasetTable> wrapper = new QueryWrapper<>();
                wrapper.eq("dataset_group_id", coreDatasetGroupVO.getId());
                List<CoreDatasetTable> tableResult = coreDatasetTableMapper.selectList(wrapper);
                if (!CollectionUtils.isEmpty(tableResult)) {
                    tableResult.forEach(coreDatasetTable -> {
                        String sqlVarDetail = coreDatasetTable.getSqlVariableDetails();
                        if (StringUtils.isNotEmpty(sqlVarDetail)) {
                            TypeReference<List<SqlVariableDetails>> listTypeReference = new TypeReference<List<SqlVariableDetails>>() {
                            };
                            List<SqlVariableDetails> defaultsSqlVariableDetails = JsonUtil.parseList(sqlVarDetail, listTypeReference);
                            defaultsSqlVariableDetails.forEach(sqlVariableDetails -> {
                                String varFieldId = coreDatasetTable.getId() + "|DE|" + sqlVariableDetails.getVariableName();
                                fields.add(new CoreDatasetTableFieldVO(varFieldId, sqlVariableDetails.getVariableName(), FieldUtils.transType2DeType(sqlVariableDetails.getType().get(0).contains("DATETIME") ? "DATETIME" : sqlVariableDetails.getType().get(0))));
                            });
                        }
                    });
                }
            });
        }
        return result;
    }
}
