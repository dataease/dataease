package io.dataease.visualization.server;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.chart.dto.ChartViewDTO;
import io.dataease.api.permissions.auth.api.InteractiveAuthApi;
import io.dataease.api.permissions.auth.dto.BusiResourceCreator;
import io.dataease.api.permissions.auth.dto.BusiResourceEditor;
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
import io.dataease.visualization.dao.ext.ExtDataVisualizationMapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired(required = false)
    private InteractiveAuthApi interactiveAuthApi;

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
        DataVisualizationInfo sourceData = null;
        if (ObjectUtils.isNotEmpty(sourceData = visualizationInfoMapper.selectById(id))) {
            request.setUpdateBy("");
            request.setUpdateTime(System.currentTimeMillis());
            DataVisualizationInfo visualizationInfo = new DataVisualizationInfo();
            BeanUtils.copyBean(visualizationInfo, request);
            visualizationInfoMapper.updateById(visualizationInfo);
            if (ObjectUtils.isNotEmpty(interactiveAuthApi) && !StringUtils.equals(sourceData.getName(), visualizationInfo.getName())) {
                BusiResourceEditor editor = new BusiResourceEditor();
                editor.setId(id);
                editor.setName(visualizationInfo.getName());
                editor.setFlag(StringUtils.equals("dataV", visualizationInfo.getType()) ? "screen" : "panel");
                interactiveAuthApi.editResource(editor);
            }
        } else {
            DataVisualizationInfo visualizationInfo = new DataVisualizationInfo();
            BeanUtils.copyBean(visualizationInfo, request);
            visualizationInfo.setDeleteFlag(DataVisualizationConstants.DELETE_FLAG.AVAILABLE);
            visualizationInfo.setNodeType(DataVisualizationConstants.NODE_TYPE.LEAF);
            visualizationInfo.setCreateBy("");
            visualizationInfo.setCreateTime(System.currentTimeMillis());
            visualizationInfoMapper.insert(visualizationInfo);
            if (ObjectUtils.isNotEmpty(interactiveAuthApi)) {
                BusiResourceCreator creator = new BusiResourceCreator();
                creator.setId(id);
                creator.setName(visualizationInfo.getName());
                creator.setLeaf(!StringUtils.equals("folder", visualizationInfo.getNodeType()));
                creator.setPid(visualizationInfo.getPid());
                creator.setFlag(StringUtils.equals("dataV", visualizationInfo.getType()) ? "screen" : "panel");
                interactiveAuthApi.saveResource(creator);
            }
        }

        List<Long> viewIds = new ArrayList<>();
        //保存视图信
        Map<Long, ChartViewDTO> chartViewsInfo = request.getCanvasViewInfo();
        if (!CollectionUtils.isEmpty(chartViewsInfo)) {
            chartViewsInfo.forEach((key, chartViewDTO) -> {
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
        if (!CollectionUtils.isEmpty(viewIds)) {
            chartViewManege.deleteBySceneId(request.getId(), viewIds);
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
        if (ObjectUtils.isNotEmpty(interactiveAuthApi)) {
            interactiveAuthApi.delResource(visualizationInfo.getId());
        }
    }

    @Override
    public List<DataVisualizationBaseVO> findTree(DataVisualizationBaseRequest request) {
        List<DataVisualizationBaseVO> result = dvMapper.findBashInfo(request.getNodeType(), request.getType());
        if (CollectionUtils.isEmpty(result)) {
            return new ArrayList<>();
        } else {
            return TreeUtils.mergeTree(result, 0L);
        }
    }

    @Override
    public void savaOrUpdateBase(DataVisualizationBaseRequest request) {
        DataVisualizationInfo visualizationInfo = new DataVisualizationInfo();
        BeanUtils.copyBean(visualizationInfo, request);
        if (request.getId() == null) {
            visualizationInfo.setDeleteFlag(DataVisualizationConstants.DELETE_FLAG.AVAILABLE);
            visualizationInfo.setId(IDUtils.snowID());
            visualizationInfo.setCreateBy("");
            visualizationInfo.setCreateTime(System.currentTimeMillis());
            visualizationInfoMapper.insert(visualizationInfo);
            if (ObjectUtils.isNotEmpty(interactiveAuthApi)) {
                BusiResourceCreator creator = new BusiResourceCreator();
                creator.setName(visualizationInfo.getName());
                creator.setId(visualizationInfo.getId());
                creator.setLeaf(!StringUtils.equals("folder", visualizationInfo.getNodeType()));
                creator.setPid(visualizationInfo.getPid());
                creator.setFlag(StringUtils.equals("dataV", visualizationInfo.getType()) ? "screen" : "panel");
                interactiveAuthApi.saveResource(creator);
            }
        } else {
            Long id = visualizationInfo.getId();
            DataVisualizationInfo sourceData = visualizationInfoMapper.selectById(id);
            visualizationInfo.setUpdateTime(System.currentTimeMillis());
            visualizationInfoMapper.updateById(visualizationInfo);
            if (ObjectUtils.isNotEmpty(interactiveAuthApi) && ObjectUtils.isNotEmpty(sourceData) && !StringUtils.equals(sourceData.getName(), visualizationInfo.getName())) {
                BusiResourceEditor editor = new BusiResourceEditor();
                editor.setId(id);
                editor.setName(visualizationInfo.getName());
                editor.setFlag(StringUtils.equals("dataV", visualizationInfo.getType()) ? "screen" : "panel");
                interactiveAuthApi.editResource(editor);
            }
        }
    }

    @Override
    public void nameCheck(DataVisualizationBaseRequest request) {
        QueryWrapper<DataVisualizationInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("delete_flag", 0);
        wrapper.eq("pid", request.getPid());
        wrapper.eq("name",request.getName());
        wrapper.eq("node_type",request.getNodeType());
        wrapper.eq("type",request.getType());
        if("update".equalsIgnoreCase(request.getOpt())){
            wrapper.ne("id",request.getId());
        }
        if(visualizationInfoMapper.exists(wrapper)){
            DataEaseException.throwException("当前名称已经存在");
        }
    }
}
