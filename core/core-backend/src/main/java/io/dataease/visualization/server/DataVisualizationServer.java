package io.dataease.visualization.server;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.chart.dto.ChartViewDTO;
import io.dataease.api.visualization.DataVisualizationApi;
import io.dataease.api.visualization.request.DataVisualizationBaseRequest;
import io.dataease.api.visualization.vo.DataVisualizationBaseVO;
import io.dataease.api.visualization.vo.DataVisualizationVO;
import io.dataease.chart.manage.ChartViewManege;
import io.dataease.commons.constants.DataVisualizationConstants;
import io.dataease.commons.exception.DataEaseException;
import io.dataease.exception.DEException;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.IDUtils;
import io.dataease.utils.TreeUtils;
import io.dataease.visualization.dao.auto.entity.DataVisualizationInfo;
import io.dataease.visualization.dao.auto.mapper.DataVisualizationInfoMapper;
import io.dataease.visualization.ext.ExtDataVisualizationMapper;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/dataVisualization")
public class DataVisualizationServer implements DataVisualizationApi {

    @Resource
    private DataVisualizationInfoMapper visualizationInfoMapper;

    @Resource
    private ChartViewManege chartViewManege;

    @Resource
    private ExtDataVisualizationMapper dvMapper;

    @Override
    public DataVisualizationVO findById(Long dvId) {
        QueryWrapper<DataVisualizationInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("delete_flag", 0);
        wrapper.eq("id", dvId);
        DataVisualizationInfo visualizationInfo = visualizationInfoMapper.selectOne(wrapper);
        if (visualizationInfo != null) {
            DataVisualizationVO result = new DataVisualizationVO();
            BeanUtils.copyBean(result, visualizationInfo);
            //获取视图信息
            List<ChartViewDTO> chartViewDTOS = chartViewManege.listBySceneId(dvId);
            if(!CollectionUtils.isEmpty(chartViewDTOS)){
               Map<Long,ChartViewDTO> viewInfo =  chartViewDTOS.stream().collect(Collectors.toMap(ChartViewDTO::getId, chartView -> chartView));
                result.setCanvasViewInfo(viewInfo);
            }
            return result;
        } else {
            DataEaseException.throwException("Can not find any data visualization info...");
        }
        return null;
    }

    @Override
    @Transactional
    public void save(DataVisualizationBaseRequest request) {
        Long id = request.getId();
        if (id == null) {
            DEException.throwException("no id");
        }
        QueryWrapper<DataVisualizationInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("id",id);
        Boolean dvCheck = visualizationInfoMapper.exists(wrapper);
        if(visualizationInfoMapper.exists(wrapper)){
            request.setUpdateBy("");
            request.setUpdateTime(System.currentTimeMillis());
            DataVisualizationInfo visualizationInfo = new DataVisualizationInfo();
            BeanUtils.copyBean(visualizationInfo, request);
            visualizationInfoMapper.updateById(visualizationInfo);
        }else{
            DataVisualizationInfo visualizationInfo = new DataVisualizationInfo();
            BeanUtils.copyBean(visualizationInfo, request);
            visualizationInfo.setDeleteFlag(DataVisualizationConstants.DELETE_FLAG.AVAILABLE);
            visualizationInfo.setNodeType(DataVisualizationConstants.NODE_TYPE.DV);
            visualizationInfo.setCreateBy("");
            visualizationInfo.setCreateTime(System.currentTimeMillis());
            visualizationInfoMapper.insert(visualizationInfo);
        }

        List<Long> viewIds = new ArrayList<>();
        //保存视图信
        Map<Long,ChartViewDTO> chartViewsInfo = request.getCanvasViewInfo();
        if(!CollectionUtils.isEmpty(chartViewsInfo)){
            chartViewsInfo.forEach((key,chartViewDTO) -> {
                try {
                    chartViewDTO.setSceneId(request.getId());
                    chartViewManege.save(chartViewDTO);
                    viewIds.add(chartViewDTO.getId());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
        // TODO 清理无用的视图
        if(!CollectionUtils.isEmpty(viewIds)){
            chartViewManege.deleteBySceneId(request.getId(),viewIds);
        }
    }

    @Override
    public void update(DataVisualizationBaseRequest request) {
        if (request.getId() != null) {
            request.setUpdateBy("");
            request.setUpdateTime(System.currentTimeMillis());
            DataVisualizationInfo visualizationInfo = new DataVisualizationInfo();
            BeanUtils.copyBean(visualizationInfo, request);
            visualizationInfoMapper.updateById(visualizationInfo);
        } else {
            DataEaseException.throwException("Id can not be null");
        }
    }

    @Override
    public void deleteLogic(Long dvId) {
        DataVisualizationInfo visualizationInfo = new DataVisualizationInfo();
        visualizationInfo.setDeleteBy("");
        visualizationInfo.setDeleteTime(System.currentTimeMillis());
        visualizationInfo.setDeleteFlag(DataVisualizationConstants.DELETE_FLAG.DELETED);
        visualizationInfo.setId(dvId);
        visualizationInfoMapper.updateById(visualizationInfo);
    }

    @Override
    public List<DataVisualizationBaseVO> findTree(DataVisualizationBaseRequest request) {
        List<DataVisualizationBaseVO> result = dvMapper.findBashInfo(request.getNodeType(),request.getType());
        if(CollectionUtils.isEmpty(result)){
            return new ArrayList<>();
        }else{
            return TreeUtils.mergeTree(result,0l);
        }
    }

    @Override
    public void savaOrUpdateBase(DataVisualizationBaseRequest request) {
        DataVisualizationInfo visualizationInfo = new DataVisualizationInfo();
        BeanUtils.copyBean(visualizationInfo, request);
        if(request.getId() == null){
            visualizationInfo.setDeleteFlag(DataVisualizationConstants.DELETE_FLAG.AVAILABLE);
            visualizationInfo.setId(IDUtils.snowID());
            visualizationInfo.setCreateBy("");
            visualizationInfo.setCreateTime(System.currentTimeMillis());
            visualizationInfoMapper.insert(visualizationInfo);
        }else{
            visualizationInfo.setUpdateTime(System.currentTimeMillis());
            visualizationInfoMapper.updateById(visualizationInfo);
        }
    }
}
