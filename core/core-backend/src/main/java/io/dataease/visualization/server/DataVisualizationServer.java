package io.dataease.visualization.server;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.chart.dto.ChartViewDTO;
import io.dataease.api.visualization.DataVisualizationApi;
import io.dataease.api.visualization.request.DataVisualizationBaseRequest;
import io.dataease.api.visualization.vo.DataVisualizationVO;
import io.dataease.chart.manage.ChartViewManege;
import io.dataease.commons.constants.DataVisualizationConstants;
import io.dataease.commons.exception.DataEaseException;
import io.dataease.exception.DEException;
import io.dataease.model.BusiNodeRequest;
import io.dataease.model.BusiNodeVO;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.IDUtils;
import io.dataease.visualization.dao.auto.entity.DataVisualizationInfo;
import io.dataease.visualization.dao.auto.mapper.DataVisualizationInfoMapper;
import io.dataease.visualization.manage.CoreVisualizationManage;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
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
    private CoreVisualizationManage coreVisualizationManage;

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
            if (!CollectionUtils.isEmpty(chartViewDTOS)) {
                Map<Long, ChartViewDTO> viewInfo = chartViewDTOS.stream().collect(Collectors.toMap(ChartViewDTO::getId, chartView -> chartView));
                result.setCanvasViewInfo(viewInfo);
            }
            return result;
        } else {
            DEException.throwException("Can not find any data visualization info...");
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
        if (ObjectUtils.isNotEmpty(visualizationInfoMapper.selectById(id))) {
            coreVisualizationManage.innerEdit(BeanUtils.copyBean(new DataVisualizationInfo(), request));
        } else {
            DataVisualizationInfo visualizationInfo = new DataVisualizationInfo();
            BeanUtils.copyBean(visualizationInfo, request);
            visualizationInfo.setNodeType(DataVisualizationConstants.NODE_TYPE.LEAF);
            coreVisualizationManage.innerSave(visualizationInfo);
        }

        // List<Long> viewIds = new ArrayList<>();
        String componentData = request.getComponentData();
        //保存视图信
        Map<Long, ChartViewDTO> chartViewsInfo = request.getCanvasViewInfo();
        if (!CollectionUtils.isEmpty(chartViewsInfo)) {
            chartViewsInfo.forEach((key, chartViewDTO) -> {
                if (componentData.indexOf(chartViewDTO.getId() + "") > -1) {
                    try {
                        chartViewDTO.setSceneId(request.getId());
                        chartViewManege.save(chartViewDTO);
                        // viewIds.add(chartViewDTO.getId());
                    } catch (Exception e) {
                        DEException.throwException(e);
                    }
                }
            });
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

    @Transactional
    @Override
    public void deleteLogic(Long dvId) {
        coreVisualizationManage.delete(dvId);
    }


    @Override
    public List<BusiNodeVO> tree(BusiNodeRequest request) {
        return coreVisualizationManage.tree(request);
    }

    @Transactional
    @Override
    public void savaOrUpdateBase(DataVisualizationBaseRequest request) {
        DataVisualizationInfo visualizationInfo = new DataVisualizationInfo();
        BeanUtils.copyBean(visualizationInfo, request);
        if (request.getId() == null) {
            visualizationInfo.setId(IDUtils.snowID());
            coreVisualizationManage.innerSave(visualizationInfo);

        } else {
            coreVisualizationManage.innerEdit(visualizationInfo);
        }
    }

    @Transactional
    @Override
    public void move(DataVisualizationBaseRequest request) {
        coreVisualizationManage.move(request);
    }

    @Override
    public void nameCheck(DataVisualizationBaseRequest request) {
        QueryWrapper<DataVisualizationInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("delete_flag", 0);
        wrapper.eq("pid", request.getPid());
        wrapper.eq("name", request.getName());
        wrapper.eq("node_type", request.getNodeType());
        wrapper.eq("type", request.getType());
        if ("update".equalsIgnoreCase(request.getOpt())) {
            wrapper.ne("id", request.getId());
        }
        if (visualizationInfoMapper.exists(wrapper)) {
            DEException.throwException("当前名称已经存在");
        }
    }

    @Override
    public List<DataVisualizationVO> findRecent(DataVisualizationBaseRequest request) {
        QueryWrapper<DataVisualizationInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("delete_flag", 0);
        wrapper.eq("node_type", "leaf");
        wrapper.like(StringUtils.isNotEmpty(request.getName()), "name", request.getName());
        wrapper.eq(StringUtils.isNotEmpty(request.getType()), "type", request.getType());
        List<DataVisualizationInfo> result = visualizationInfoMapper.selectList(wrapper);
        List<DataVisualizationVO> returnResult = new ArrayList<>();
        if (!CollectionUtils.isEmpty(result)) {
            result.forEach(dataVisualizationInfo -> {
                DataVisualizationVO dataVisualizationVO = new DataVisualizationVO();
                returnResult.add(BeanUtils.copyBean(dataVisualizationVO, dataVisualizationInfo));
            });
        }
        return returnResult;
    }
}
