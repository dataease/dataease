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
import io.dataease.dataset.dao.auto.entity.CoreDatasetTable;
import io.dataease.dataset.dao.auto.mapper.CoreDatasetTableMapper;
import io.dataease.constant.DeTypeConstants;
import io.dataease.extensions.view.dto.SqlVariableDetails;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.JsonUtil;
import io.dataease.visualization.dao.auto.entity.VisualizationOuterParams;
import io.dataease.visualization.dao.auto.entity.VisualizationOuterParamsInfo;
import io.dataease.visualization.dao.auto.entity.VisualizationOuterParamsTargetViewInfo;
import io.dataease.visualization.dao.auto.mapper.VisualizationOuterParamsInfoMapper;
import io.dataease.visualization.dao.auto.mapper.VisualizationOuterParamsMapper;
import io.dataease.visualization.dao.auto.mapper.VisualizationOuterParamsTargetViewInfoMapper;
import io.dataease.visualization.dao.ext.mapper.ExtVisualizationOuterParamsMapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
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
    private VisualizationOuterParamsInfoMapper outerParamsInfoMapper;

    @Resource
    private VisualizationOuterParamsTargetViewInfoMapper outerParamsTargetViewInfoMapper;

    @Resource
    private CoreDatasetTableMapper coreDatasetTableMapper;


    @Override
    public VisualizationOuterParamsDTO queryWithVisualizationId(String visualizationId) {
        VisualizationOuterParamsDTO visualizationOuterParamsDTO = extOuterParamsMapper.queryWithVisualizationId(visualizationId);
        return visualizationOuterParamsDTO;
    }

    @Override
    public void updateOuterParamsSet(VisualizationOuterParamsDTO outerParamsDTO) {
        String visualizationId = outerParamsDTO.getVisualizationId();
        Assert.notNull(visualizationId, "visualizationId cannot be null");
        Map<String,String> paramsInfoNameIdMap = new HashMap<>();
        List<VisualizationOuterParamsInfo> paramsInfoNameIdList = extOuterParamsMapper.getVisualizationOuterParamsInfoBase(visualizationId);
        if(!CollectionUtils.isEmpty(paramsInfoNameIdList)){
            paramsInfoNameIdMap = paramsInfoNameIdList.stream()
                    .collect(Collectors.toMap(VisualizationOuterParamsInfo::getParamName, VisualizationOuterParamsInfo::getParamsInfoId));
        }
        //清理原有数据
        extOuterParamsMapper.deleteOuterParamsTargetWithVisualizationId(visualizationId);
        extOuterParamsMapper.deleteOuterParamsInfoWithVisualizationId(visualizationId);
        extOuterParamsMapper.deleteOuterParamsWithVisualizationId(visualizationId);
        if(CollectionUtils.isEmpty(outerParamsDTO.getOuterParamsInfoArray())){
            return;
        }
        // 插入新的数据
        String paramsId = UUID.randomUUID().toString();
        outerParamsDTO.setParamsId(paramsId);
        VisualizationOuterParams newOuterParams = new VisualizationOuterParams();
        BeanUtils.copyBean(newOuterParams, outerParamsDTO);
        outerParamsMapper.insert(newOuterParams);
        Map<String, String> finalParamsInfoNameIdMap = paramsInfoNameIdMap;
        Optional.ofNullable(outerParamsDTO.getOuterParamsInfoArray()).orElse(new ArrayList<>()).forEach(outerParamsInfo -> {
            String paramsInfoId = finalParamsInfoNameIdMap.get(outerParamsInfo.getParamName());
            if(StringUtils.isEmpty(paramsInfoId)){
                paramsInfoId = UUID.randomUUID().toString();
            }
            outerParamsInfo.setParamsInfoId(paramsInfoId);
            outerParamsInfo.setParamsId(paramsId);
            VisualizationOuterParamsInfo newOuterParamsInfo = new VisualizationOuterParamsInfo();
            BeanUtils.copyBean(newOuterParamsInfo, outerParamsInfo);
            outerParamsInfoMapper.insert(newOuterParamsInfo);
            String finalParamsInfoId = paramsInfoId;
            Optional.ofNullable(outerParamsInfo.getTargetViewInfoList()).orElse(new ArrayList<>()).forEach(targetViewInfo -> {
                String targetViewInfoId = UUID.randomUUID().toString();
                targetViewInfo.setTargetId(targetViewInfoId);
                targetViewInfo.setParamsInfoId(finalParamsInfoId);
                VisualizationOuterParamsTargetViewInfo newOuterParamsTargetViewInfo = new VisualizationOuterParamsTargetViewInfo();
                BeanUtils.copyBean(newOuterParamsTargetViewInfo, targetViewInfo);
                outerParamsTargetViewInfoMapper.insert(newOuterParamsTargetViewInfo);
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
            result.forEach(coreDatasetGroupVO -> {
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
                                fields.add(new CoreDatasetTableFieldVO(varFieldId, sqlVariableDetails.getVariableName(), DeTypeConstants.DE_STRING));
                            });
                        }
                    });
                }
            });
        }
        return result;
    }
}
