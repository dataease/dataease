package io.dataease.visualization.server;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.dataease.api.chart.dto.ChartViewDTO;
import io.dataease.api.template.dto.TemplateManageFileDTO;
import io.dataease.api.template.dto.VisualizationTemplateExtendDataDTO;
import io.dataease.api.visualization.DataVisualizationApi;
import io.dataease.api.visualization.request.DataVisualizationBaseRequest;
import io.dataease.api.visualization.request.VisualizationWorkbranchQueryRequest;
import io.dataease.api.visualization.vo.DataVisualizationVO;
import io.dataease.api.visualization.vo.VisualizationResourceVO;
import io.dataease.api.visualization.vo.VisualizationWatermarkVO;
import io.dataease.chart.dao.auto.entity.CoreChartView;
import io.dataease.chart.manage.ChartDataManage;
import io.dataease.chart.manage.ChartViewManege;
import io.dataease.commons.constants.DataVisualizationConstants;
import io.dataease.commons.constants.OptConstants;
import io.dataease.constant.CommonConstants;
import io.dataease.constant.LogOT;
import io.dataease.exception.DEException;
import io.dataease.license.config.XpackInteract;
import io.dataease.log.DeLog;
import io.dataease.model.BusiNodeRequest;
import io.dataease.model.BusiNodeVO;
import io.dataease.operation.manage.CoreOptRecentManage;
import io.dataease.template.dao.auto.entity.VisualizationTemplate;
import io.dataease.template.dao.auto.entity.VisualizationTemplateExtendData;
import io.dataease.template.dao.auto.mapper.VisualizationTemplateExtendDataMapper;
import io.dataease.template.dao.auto.mapper.VisualizationTemplateMapper;
import io.dataease.template.manage.TemplateCenterManage;
import io.dataease.utils.AuthUtils;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.IDUtils;
import io.dataease.utils.JsonUtil;
import io.dataease.visualization.dao.auto.entity.DataVisualizationInfo;
import io.dataease.visualization.dao.auto.entity.VisualizationWatermark;
import io.dataease.visualization.dao.auto.mapper.DataVisualizationInfoMapper;
import io.dataease.visualization.dao.auto.mapper.VisualizationWatermarkMapper;
import io.dataease.visualization.dao.ext.mapper.ExtDataVisualizationMapper;
import io.dataease.visualization.manage.CoreVisualizationManage;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
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

    @Resource
    private VisualizationTemplateMapper templateMapper;

    @Resource
    private TemplateCenterManage templateCenterManage;

    @Resource
    private StaticResourceServer staticResourceServer;

    @Resource
    private VisualizationTemplateExtendDataMapper templateExtendDataMapper;

    @Resource
    private CoreOptRecentManage coreOptRecentManage;

    @Resource
    private VisualizationWatermarkMapper watermarkMapper;

    @DeLog(id = "#p0", ot = LogOT.READ, stExp = "#p1")
    @Override
    public DataVisualizationVO findCopyResource(Long dvId, String busiFlag) {
        DataVisualizationVO result = findById(dvId, busiFlag);
        if(result !=null && result.getPid() == -1){
            return result;
        }else{
            return null;
        }
    }

    @Override
    @XpackInteract(value = "dataVisualizationServer", original = true)
    public DataVisualizationVO findById(Long dvId, String busiFlag) {
        DataVisualizationVO result = extDataVisualizationMapper.findDvInfo(dvId, busiFlag);
        if (result != null) {
            //获取视图信息
            List<ChartViewDTO> chartViewDTOS = chartViewManege.listBySceneId(dvId);
            if (!CollectionUtils.isEmpty(chartViewDTOS)) {
                Map<Long, ChartViewDTO> viewInfo = chartViewDTOS.stream().collect(Collectors.toMap(ChartViewDTO::getId, chartView -> chartView));
                result.setCanvasViewInfo(viewInfo);
            }
            VisualizationWatermark watermark = watermarkMapper.selectById("system_default");
            VisualizationWatermarkVO watermarkVO = new VisualizationWatermarkVO();
            BeanUtils.copyBean(watermarkVO, watermark);
            result.setWatermarkInfo(watermarkVO);
            return result;
        } else {
            DEException.throwException("资源不存在或已经被删除...");
        }
        return null;
    }

    @DeLog(id = "#p0.id", pid = "#p0.pid", ot = LogOT.CREATE, stExp = "#p0.type")
    @Override
    @Transactional
    public String saveCanvas(DataVisualizationBaseRequest request) {
        DataVisualizationInfo visualizationInfo = new DataVisualizationInfo();
        BeanUtils.copyBean(visualizationInfo, request);
        visualizationInfo.setNodeType(request.getNodeType() == null ? DataVisualizationConstants.NODE_TYPE.LEAF : request.getNodeType());
        if (request.getSelfWatermarkStatus() != null && request.getSelfWatermarkStatus()) {
            visualizationInfo.setSelfWatermarkStatus(1);
        } else {
            visualizationInfo.setSelfWatermarkStatus(0);
        }
        Long newDvId = coreVisualizationManage.innerSave(visualizationInfo);
        request.setId(newDvId);
        //保存视图信
        chartDataManage.saveChartViewFromVisualization(request.getComponentData(), newDvId, request.getCanvasViewInfo());
        return newDvId.toString();
    }

    @DeLog(id = "#p0.id", ot = LogOT.MODIFY, stExp = "#p0.type")
    @Override
    @Transactional
    public void updateCanvas(DataVisualizationBaseRequest request) {
        Long dvId = request.getId();
        if (dvId == null) {
            DEException.throwException("ID can not be null");
        }
        DataVisualizationInfo visualizationInfo = new DataVisualizationInfo();
        BeanUtils.copyBean(visualizationInfo, request);
        if (request.getSelfWatermarkStatus() != null && request.getSelfWatermarkStatus()) {
            visualizationInfo.setSelfWatermarkStatus(1);
        } else {
            visualizationInfo.setSelfWatermarkStatus(0);
        }
        if (DataVisualizationConstants.RESOURCE_OPT_TYPE.COPY.equals(request.getOptType())) {
            // 复制更新 新建权限插入
            visualizationInfoMapper.deleteById(dvId);
            visualizationInfo.setNodeType(DataVisualizationConstants.NODE_TYPE.LEAF);
            coreVisualizationManage.innerSave(visualizationInfo);
        } else {
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
        }
        //保存视图信
        chartDataManage.saveChartViewFromVisualization(request.getComponentData(), dvId, request.getCanvasViewInfo());
    }

    /**
     * @Description: 更新基础信息；
     * 为什么单独接口：1.基础信息更新频繁数据且数据载量较小；2.防止出现更新过多信息的情况，造成视图的误删等操作
     */
    @DeLog(id = "#p0.id", ot = LogOT.MODIFY, stExp = "#p0.type")
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
    @DeLog(id = "#p0", ot = LogOT.DELETE, stExp = "#p1")
    @Transactional
    @Override
    public void deleteLogic(Long dvId, String busiFlag) {
        coreVisualizationManage.delete(dvId);
    }


    @Override
    public List<BusiNodeVO> tree(BusiNodeRequest request) {
        return coreVisualizationManage.tree(request);
    }

    @DeLog(id = "#p0.id", pid = "#p0.pid", ot = LogOT.MODIFY, stExp = "#p0.type")
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
        DataVisualizationInfo visualizationInfoTarget = new DataVisualizationInfo();
        BeanUtils.copyBean(visualizationInfoTarget, newDv);
        visualizationInfoTarget.setPid(-1L);
        coreVisualizationManage.preInnerSave(visualizationInfoTarget);
        return String.valueOf(newDvId);
    }

    @Override
    public String findDvType(Long dvId) {
        return extDataVisualizationMapper.findDvType(dvId);
    }

    @Override
    public DataVisualizationVO decompression(DataVisualizationBaseRequest request) throws Exception {
        Long newDvId = IDUtils.snowID();
        String newFrom = request.getNewFrom();
        String templateStyle = null;
        String templateData = null;
        String dynamicData = null;
        String staticResource = null;
        String name = null;
        String dvType = null;
        //内部模板新建
        if (DataVisualizationConstants.NEW_PANEL_FROM.NEW_INNER_TEMPLATE.equals(newFrom)) {
            VisualizationTemplate visualizationTemplate = templateMapper.selectById(request.getTemplateId());
            templateStyle = visualizationTemplate.getTemplateStyle();
            templateData = visualizationTemplate.getTemplateData();
            dynamicData = visualizationTemplate.getDynamicData();
            name = visualizationTemplate.getName();
            dvType = visualizationTemplate.getDvType();
            // 模板市场记录
            coreOptRecentManage.saveOpt(request.getTemplateId(), OptConstants.OPT_RESOURCE_TYPE.TEMPLATE, OptConstants.OPT_TYPE.NEW);
            VisualizationTemplate visualizationTemplateUpdate = new VisualizationTemplate();
            visualizationTemplateUpdate.setId(visualizationTemplate.getId());
            visualizationTemplateUpdate.setUseCount(visualizationTemplate.getUseCount() == null ? 0 : visualizationTemplate.getUseCount() + 1);
            templateMapper.updateById(visualizationTemplateUpdate);
        } else if (DataVisualizationConstants.NEW_PANEL_FROM.NEW_OUTER_TEMPLATE.equals(newFrom)) {
            templateStyle = request.getCanvasStyleData();
            templateData = request.getComponentData();
            dynamicData = request.getDynamicData();
            staticResource = request.getStaticResource();
            name = request.getName();
            dvType = request.getType();
        } else if (DataVisualizationConstants.NEW_PANEL_FROM.NEW_MARKET_TEMPLATE.equals(newFrom)) {
            TemplateManageFileDTO templateFileInfo = templateCenterManage.getTemplateFromMarket(request.getTemplateUrl());
            if (templateFileInfo == null) {
                DEException.throwException("Can't find the template's info from market,please check");
            }
            templateStyle = templateFileInfo.getCanvasStyleData();
            templateData = templateFileInfo.getComponentData();
            dynamicData = templateFileInfo.getDynamicData();
            staticResource = templateFileInfo.getStaticResource();
            name = templateFileInfo.getName();
            dvType = templateFileInfo.getDvType();
            // 模板市场记录
            coreOptRecentManage.saveOpt(request.getResourceName(), OptConstants.OPT_RESOURCE_TYPE.TEMPLATE, OptConstants.OPT_TYPE.NEW);
        }
        // 解析动态数据
        Map<String, String> dynamicDataMap = JsonUtil.parseObject(dynamicData, Map.class);
        List<ChartViewDTO> chartViews = new ArrayList<>();
        Map<Long, ChartViewDTO> canvasViewInfo = new HashMap<>();
        Map<Long, VisualizationTemplateExtendDataDTO> extendDataInfo = new HashMap<>();
        for (Map.Entry<String, String> entry : dynamicDataMap.entrySet()) {
            String originViewId = entry.getKey();
            String originViewData = JsonUtil.toJSONString(entry.getValue()).toString();
            ChartViewDTO chartView = JsonUtil.parseObject(originViewData, ChartViewDTO.class);
            if (chartView == null) {
                continue;
            }
            Long newViewId = IDUtils.snowID();
            chartView.setId(newViewId);
            chartView.setSceneId(newDvId);
            chartView.setTableId(null);
            chartView.setDataFrom(CommonConstants.VIEW_DATA_FROM.TEMPLATE);
            // 数据处理 1.替换viewId 2.加入模板view data数据
            VisualizationTemplateExtendDataDTO extendDataDTO = new VisualizationTemplateExtendDataDTO(newDvId, newViewId, originViewData);
            extendDataInfo.put(newViewId, extendDataDTO);
            templateData = templateData.replaceAll(originViewId, newViewId.toString());
            canvasViewInfo.put(chartView.getId(), chartView);
            //插入模板数据 此处预先插入减少数据交互量
            VisualizationTemplateExtendData extendData = new VisualizationTemplateExtendData();
            templateExtendDataMapper.insert(BeanUtils.copyBean(extendData, extendDataDTO));
        }
        request.setComponentData(templateData);
        request.setCanvasStyleData(templateStyle);
        //Store static resource into the server
        staticResourceServer.saveFilesToServe(staticResource);
        return new DataVisualizationVO(newDvId, name, dvType, templateStyle, templateData, canvasViewInfo, null);
    }

    @Override
    public DataVisualizationVO decompressionLocalFile(MultipartFile file) {
        return null;
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
        wrapper.ne("pid", -1);
        wrapper.eq("name", request.getName().trim());
        wrapper.eq("node_type", request.getNodeType());
        wrapper.eq("type", request.getType());
        wrapper.eq("org_id", AuthUtils.getUser().getDefaultOid());
        if (visualizationInfoMapper.exists(wrapper)) {
            DEException.throwException("当前名称已经存在");
        }
    }

}
