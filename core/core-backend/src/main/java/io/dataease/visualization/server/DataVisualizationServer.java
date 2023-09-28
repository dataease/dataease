package io.dataease.visualization.server;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.dataease.api.chart.dto.ChartViewDTO;
import io.dataease.api.visualization.DataVisualizationApi;
import io.dataease.api.visualization.request.DataVisualizationBaseRequest;
import io.dataease.api.visualization.request.VisualizationWorkbranchQueryRequest;
import io.dataease.api.visualization.vo.DataVisualizationVO;
import io.dataease.api.visualization.vo.VisualizationResourceVO;
import io.dataease.chart.dao.auto.entity.CoreChartView;
import io.dataease.chart.manage.ChartDataManage;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    private ChartDataManage chartDataManage;

    @Override
    public DataVisualizationVO findById(Long dvId,String busiFlag) {
        DataVisualizationVO result = extDataVisualizationMapper.findDvInfo(dvId,busiFlag);
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
    public String saveCanvas(DataVisualizationBaseRequest request) {
        DataVisualizationInfo visualizationInfo = new DataVisualizationInfo();
        BeanUtils.copyBean(visualizationInfo, request);
        visualizationInfo.setNodeType(request.getNodeType() == null ? DataVisualizationConstants.NODE_TYPE.LEAF : request.getNodeType());
        Long newDvId = coreVisualizationManage.innerSave(visualizationInfo);
        //保存视图信
        chartDataManage.saveChartViewFromVisualization(request.getComponentData(), newDvId, request.getCanvasViewInfo());
        return newDvId.toString();
    }

    @Override
    @Transactional
    public void updateCanvas(DataVisualizationBaseRequest request) {
        Long dvId = request.getId();
        if (dvId == null) {
            DEException.throwException("ID can not be null");
        }
        DataVisualizationInfo visualizationInfo = new DataVisualizationInfo();
        BeanUtils.copyBean(visualizationInfo, request);

        // 检查当前节点的pid是否一致如果不一致 需要调用move 接口(预存 可能会出现pid =-1的情况)
        if (request.getPid() != -1) {
            QueryWrapper<DataVisualizationInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("pid", request.getPid());
            queryWrapper.eq("id", dvId);
            if (!visualizationInfoMapper.exists(queryWrapper)) {
                request.setMoveFromUpdate(true);
                coreVisualizationManage.move(request);
            }
        }
        coreVisualizationManage.innerEdit(visualizationInfo);
        //保存视图信
        chartDataManage.saveChartViewFromVisualization(request.getComponentData(), dvId, request.getCanvasViewInfo());
    }

    /**
     * @Description: 更新基础信息；
     * 为什么单独接口：1.基础信息更新频繁数据且数据载量较小；2.防止出现更新过多信息的情况，造成视图的误删等操作
     */
    @Override
    @Transactional
    public void updateBase(DataVisualizationBaseRequest request) {
        if (request.getId() != null) {
            coreVisualizationManage.innerEdit(BeanUtils.copyBean(new DataVisualizationInfo(), request));
        } else {
            DEException.throwException("Id can not be null");
        }
    }

    /**
     * @Description: 逻辑删除可视化信息；将delete_flag 置为0
     */
    @Transactional
    @Override
    public void deleteLogic(Long dvId,String busiFlag) {
        coreVisualizationManage.delete(dvId);
    }


    @Override
    public List<BusiNodeVO> tree(BusiNodeRequest request) {
        return coreVisualizationManage.tree(request);
    }

    @Transactional
    @Override
    public void move(DataVisualizationBaseRequest request) {
        coreVisualizationManage.move(request);
    }

    @Override
    public List<VisualizationResourceVO> findRecent(@RequestBody VisualizationWorkbranchQueryRequest request) {
        request.setQueryFrom("recent");
        IPage<VisualizationResourceVO> result = coreVisualizationManage.query(1, 20, request);
        return result.getRecords();
    }

    /**
     * @Description: 复制仪表板
     * 复制步骤 1.复制基础可视化数据；2.复制视图数据；3.附加数据（包括联动信息，跳转信息，外部参数信息等仪表板附加信息）
     */
    @Transactional
    @Override
    public String copy(DataVisualizationBaseRequest request) {
        Long sourceDvId = request.getId(); //源仪表板ID
        Long newDvId = IDUtils.snowID(); //目标仪表板ID
        Long copyId = IDUtils.snowID() / 100; // 本次复制执行ID
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
        // 复制视图联动信息
        extDataVisualizationMapper.copyLinkage(copyId);
        extDataVisualizationMapper.copyLinkageField(copyId);
        // 复制视图跳转信息
        extDataVisualizationMapper.copyLinkJump(copyId);
        extDataVisualizationMapper.copyLinkJumpInfo(copyId);
        extDataVisualizationMapper.copyLinkJumpTargetInfo(copyId);

        coreVisualizationManage.innerSave(newDv);
        return String.valueOf(newDvId);
    }

    @Override
    public String findDvType(Long dvId) {
        return extDataVisualizationMapper.findDvType(dvId);
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

}
