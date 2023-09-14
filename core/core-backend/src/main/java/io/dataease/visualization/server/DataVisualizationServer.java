package io.dataease.visualization.server;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.chart.dto.ChartViewDTO;
import io.dataease.api.visualization.DataVisualizationApi;
import io.dataease.api.visualization.request.DataVisualizationBaseRequest;
import io.dataease.api.visualization.request.VisualizationWorkbranchQueryRequest;
import io.dataease.api.visualization.vo.DataVisualizationVO;
import io.dataease.api.visualization.vo.VisualizationResourceVO;
import io.dataease.chart.dao.auto.entity.CoreChartView;
import io.dataease.chart.dao.auto.mapper.CoreChartViewMapper;
import io.dataease.chart.manage.ChartViewManege;
import io.dataease.commons.constants.DataVisualizationConstants;
import io.dataease.exception.DEException;
import io.dataease.model.BusiNodeRequest;
import io.dataease.model.BusiNodeVO;
import io.dataease.utils.AuthUtils;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.IDUtils;
import io.dataease.visualization.dao.auto.entity.DataVisualizationInfo;
import io.dataease.visualization.dao.auto.mapper.DataVisualizationInfoMapper;
import io.dataease.visualization.dao.ext.mapper.ExtDataVisualizationMapper;
import io.dataease.visualization.manage.CoreVisualizationManage;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
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
    private ExtDataVisualizationMapper extDataVisualizationMapper;

    @Resource
    private CoreVisualizationManage coreVisualizationManage;

    @Resource
    private CoreChartViewMapper coreChartViewMapper;

    @Override
    public DataVisualizationVO findById(Long dvId) {
        DataVisualizationVO result = extDataVisualizationMapper.findDvInfo(dvId);
        if (result != null) {
            //获取视图信息
            List<ChartViewDTO> chartViewDTOS = chartViewManege.listBySceneId(dvId);
            if (!CollectionUtils.isEmpty(chartViewDTOS)) {
                Map<Long, ChartViewDTO> viewInfo = chartViewDTOS.stream().collect(Collectors.toMap(ChartViewDTO::getId, chartView -> chartView));
                result.setCanvasViewInfo(viewInfo);
            }
            return result;
        } else {
            DEException.throwException("资源不存在或已经被删除...");
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
            DEException.throwException("Id can not be null");
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
        if (DataVisualizationConstants.RESOURCE_OPT_TYPE.MOVE.equals(request.getOpt())
                || DataVisualizationConstants.RESOURCE_OPT_TYPE.RENAME.equals(request.getOpt())
                || DataVisualizationConstants.RESOURCE_OPT_TYPE.COPY.equals(request.getOpt())) {
            if (request.getPid() == null) {
                DataVisualizationInfo result = visualizationInfoMapper.selectById(request.getId());
                request.setPid(result.getPid());
            }
            if (DataVisualizationConstants.RESOURCE_OPT_TYPE.MOVE.equals(request.getOpt())
                    || DataVisualizationConstants.RESOURCE_OPT_TYPE.RENAME.equals(request.getOpt())) {
                wrapper.ne("id", request.getId());
            }
        }
        wrapper.eq("delete_flag", 0);
        wrapper.eq("pid", request.getPid());
        wrapper.eq("name", request.getName().trim());
        wrapper.eq("node_type", request.getNodeType());
        wrapper.eq("type", request.getType());
        wrapper.eq("org_id", AuthUtils.getUser().getDefaultOid());
        if (visualizationInfoMapper.exists(wrapper)) {
            DEException.throwException("当前名称已经存在");
        }
    }

    @Override
    public List<VisualizationResourceVO> findRecent(@RequestBody VisualizationWorkbranchQueryRequest request) {
        return coreVisualizationManage.findRecent(1L,20L,request);
    }

    @Override
    public String copy(DataVisualizationBaseRequest request) {
        Long sourceDvId = request.getId(); //源仪表板ID
        Long newDvId = IDUtils.snowID(); //目标仪表板ID
        Long copyId = IDUtils.snowID()/100; // 本次复制执行ID
        // 复制仪表板
        DataVisualizationInfo newDv = visualizationInfoMapper.selectById(sourceDvId);
        newDv.setName(request.getName());
        newDv.setId(newDvId);
        newDv.setPid(request.getPid());
        newDv.setCreateTime(System.currentTimeMillis());
        // 复制视图 chart_view
        extDataVisualizationMapper.viewCopyWithDv(sourceDvId, newDvId, copyId);
        List<CoreChartView> viewList = extDataVisualizationMapper.findViewInfoByCopyId(copyId);
        if (!CollectionUtils.isEmpty(viewList)) {
            String componentData = newDv.getComponentData();
            // componentData viewId 数据  并保存
            for (CoreChartView viewInfo : viewList) {
                componentData = componentData.replaceAll(String.valueOf(viewInfo.getCopyFrom()), String.valueOf(viewInfo.getId()));
            }
            newDv.setComponentData(componentData);
        }
        coreVisualizationManage.innerSave(newDv);
        return String.valueOf(newDvId);
    }
}
