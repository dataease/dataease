package io.dataease.visualization.manage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.dataease.api.visualization.request.DataVisualizationBaseRequest;
import io.dataease.api.visualization.request.VisualizationWorkbranchQueryRequest;
import io.dataease.api.visualization.vo.VisualizationResourceVO;
import io.dataease.chart.dao.ext.mapper.ExtChartViewMapper;
import io.dataease.chart.manage.ChartViewManege;
import io.dataease.commons.constants.DataVisualizationConstants;
import io.dataease.commons.constants.OptConstants;
import io.dataease.constant.BusiResourceEnum;
import io.dataease.constant.CommonConstants;
import io.dataease.exception.DEException;
import io.dataease.license.config.XpackInteract;
import io.dataease.model.BusiNodeRequest;
import io.dataease.model.BusiNodeVO;
import io.dataease.operation.manage.CoreOptRecentManage;
import io.dataease.share.manage.XpackShareManage;
import io.dataease.utils.*;
import io.dataease.visualization.dao.auto.entity.DataVisualizationInfo;
import io.dataease.visualization.dao.auto.entity.SnapshotDataVisualizationInfo;
import io.dataease.visualization.dao.auto.mapper.DataVisualizationInfoMapper;
import io.dataease.visualization.dao.auto.mapper.SnapshotDataVisualizationInfoMapper;
import io.dataease.visualization.dao.ext.mapper.*;
import io.dataease.visualization.dao.ext.po.VisualizationNodePO;
import io.dataease.visualization.dao.ext.po.VisualizationResourcePO;
import io.dataease.visualization.dto.VisualizationNodeBO;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
@Transactional
public class CoreVisualizationManage {


    @Resource
    private CoreVisualiationExtMapper extMapper;

    @Resource
    private DataVisualizationInfoMapper mapper;

    @Resource
    private SnapshotDataVisualizationInfoMapper snapshotMapper;

    @Resource
    private ExtVisualizationLinkageMapper linkageMapper;

    @Resource
    private ExtVisualizationLinkJumpMapper linkJumpMapper;

    @Resource
    private ExtVisualizationOuterParamsMapper outerParamsMapper;

    @Resource
    private ExtDataVisualizationMapper extDataVisualizationMapper;

    @Resource
    private CoreOptRecentManage coreOptRecentManage;

    @Resource
    private ExtChartViewMapper extCoreChartMapper;

    @Resource
    private ChartViewManege chartViewManege;

    @Resource
    private XpackShareManage xpackShareManage;

    @XpackInteract(value = "visualizationResourceTree", replace = true, invalid = true)
    public List<BusiNodeVO> tree(BusiNodeRequest request) {
        List<VisualizationNodeBO> nodes = new ArrayList<>();
        if (ObjectUtils.isEmpty(request.getLeaf()) || !request.getLeaf()) {
            nodes.add(rootNode());
        }
        QueryWrapper<Object> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("delete_flag", false);
        queryWrapper.ne("pid", -1);
        queryWrapper.eq(ObjectUtils.isNotEmpty(request.getLeaf()), "node_type", ObjectUtils.isNotEmpty(request.getLeaf()) && request.getLeaf() ? "leaf" : "folder");
        queryWrapper.eq("type", request.getBusiFlag());
        String info = CommunityUtils.getInfo();
        if (StringUtils.isNotBlank(info)) {
            queryWrapper.notExists(String.format(info, "data_visualization_info.id"));
        }
        // 如果是编辑界面 只展示已发布的资源
        if (CommonConstants.RESOURCE_TABLE.SNAPSHOT.equals(request.getResourceTable())) {
            queryWrapper.in("status", Arrays.asList(1, 2));
        }
        queryWrapper.orderByDesc("create_time");
        List<VisualizationNodePO> pos = extMapper.queryNodes(queryWrapper);
        if (CollectionUtils.isNotEmpty(pos)) {
            nodes.addAll(pos.stream().map(this::convert).toList());
        }
        return TreeUtils.mergeTree(nodes, BusiNodeVO.class, false);
    }

    @XpackInteract(value = "visualizationResourceTree", before = false)
    public void delete(Long id) {
        DataVisualizationInfo info = mapper.selectById(id);
        if (ObjectUtils.isEmpty(info)) {
            DEException.throwException("resource not exist");
        }
        Set<Long> delIds = new LinkedHashSet<>();
        Stack<Long> stack = new Stack<>();
        stack.add(id);
        while (!stack.isEmpty()) {
            Long tempPid = stack.pop();
            if (isTopNode(tempPid)) continue;
            delIds.add(tempPid);
            List<Long> childrenIdList = extMapper.queryChildrenId(tempPid);
            if (CollectionUtils.isNotEmpty(childrenIdList)) {
                childrenIdList.forEach(kid -> {
                    if (!delIds.contains(kid)) {
                        stack.add(kid);
                    }
                });
            }
        }

        // 删除分享相关资源
        xpackShareManage.deleteByResource(id);

        // 删除可视化资源
        extDataVisualizationMapper.deleteDataVBatch(delIds, CommonConstants.RESOURCE_TABLE.CORE);
        extDataVisualizationMapper.deleteDataVBatch(delIds, CommonConstants.RESOURCE_TABLE.SNAPSHOT);
        // 删除图表信息
        extDataVisualizationMapper.deleteViewsBatch(delIds, CommonConstants.RESOURCE_TABLE.CORE);
        extDataVisualizationMapper.deleteViewsBatch(delIds, CommonConstants.RESOURCE_TABLE.SNAPSHOT);

        coreOptRecentManage.saveOpt(id, OptConstants.OPT_RESOURCE_TYPE.VISUALIZATION, OptConstants.OPT_TYPE.DELETE);
    }

    @XpackInteract(value = "visualizationResourceTree", before = false)
    public void move(DataVisualizationBaseRequest request) {
        if (!request.getMoveFromUpdate()) {
            DataVisualizationInfo visualizationInfo = new DataVisualizationInfo();
            BeanUtils.copyBean(visualizationInfo, request);
            if (ObjectUtils.isEmpty(visualizationInfo.getId())) {
                DEException.throwException("resource not exist");
            }
            visualizationInfo.setUpdateTime(System.currentTimeMillis());
            SnapshotDataVisualizationInfo snapshotVisualizationInfo = new SnapshotDataVisualizationInfo();
            BeanUtils.copyBean(snapshotVisualizationInfo, visualizationInfo);
            coreOptRecentManage.saveOpt(visualizationInfo.getId(), OptConstants.OPT_RESOURCE_TYPE.VISUALIZATION, OptConstants.OPT_TYPE.UPDATE);
            mapper.updateById(visualizationInfo);
            snapshotMapper.updateById(snapshotVisualizationInfo);
        }
    }

    @XpackInteract(value = "visualizationResourceTree", before = false)
    public Long innerSave(DataVisualizationInfo visualizationInfo) {
        visualizationInfo.setVersion(3);
        return preInnerSave(visualizationInfo);
    }

    public Long preInnerSave(DataVisualizationInfo visualizationInfo) {
        if (visualizationInfo.getId() == null) {
            Long id = IDUtils.snowID();
            visualizationInfo.setId(id);
        }
        visualizationInfo.setDeleteFlag(DataVisualizationConstants.DELETE_FLAG.AVAILABLE);
        visualizationInfo.setStatus(visualizationInfo.getStatus());
        visualizationInfo.setCreateBy(AuthUtils.getUser().getUserId().toString());
        visualizationInfo.setUpdateBy(AuthUtils.getUser().getUserId().toString());
        visualizationInfo.setCreateTime(System.currentTimeMillis());
        visualizationInfo.setUpdateTime(System.currentTimeMillis());
        visualizationInfo.setOrgId(AuthUtils.getUser().getDefaultOid());
        mapper.insert(visualizationInfo);
        // 镜像文件插入
        SnapshotDataVisualizationInfo snapshotVisualizationInfo = new SnapshotDataVisualizationInfo();
        BeanUtils.copyBean(snapshotVisualizationInfo, visualizationInfo);
        snapshotMapper.insert(snapshotVisualizationInfo);
        coreOptRecentManage.saveOpt(visualizationInfo.getId(), OptConstants.OPT_RESOURCE_TYPE.VISUALIZATION, OptConstants.OPT_TYPE.NEW);
        return visualizationInfo.getId();
    }

    @XpackInteract(value = "visualizationResourceTree", before = false)
    public void innerEdit(DataVisualizationInfo visualizationInfo) {
        // 镜像和主表保持名称一致
        visualizationInfo.setUpdateTime(System.currentTimeMillis());
        visualizationInfo.setUpdateBy(AuthUtils.getUser().getUserId().toString());
        visualizationInfo.setVersion(3);
        // 更新镜像
        SnapshotDataVisualizationInfo snapshotVisualizationInfo = new SnapshotDataVisualizationInfo();
        BeanUtils.copyBean(snapshotVisualizationInfo, visualizationInfo);
        snapshotMapper.updateById(snapshotVisualizationInfo);
        // 更新主表名称
        DataVisualizationInfo coreVisualizationInfo = new DataVisualizationInfo();
        coreVisualizationInfo.setId(visualizationInfo.getId());
        coreVisualizationInfo.setStatus(visualizationInfo.getStatus());
        coreVisualizationInfo.setPid(visualizationInfo.getPid());
        coreVisualizationInfo.setContentId(visualizationInfo.getContentId());
        coreVisualizationInfo.setName(visualizationInfo.getName());
        coreVisualizationInfo.setUpdateTime(System.currentTimeMillis());
        coreVisualizationInfo.setUpdateBy(AuthUtils.getUser().getUserId().toString());
        coreVisualizationInfo.setVersion(3);
        mapper.updateById(coreVisualizationInfo);
        coreOptRecentManage.saveOpt(visualizationInfo.getId(), OptConstants.OPT_RESOURCE_TYPE.VISUALIZATION, OptConstants.OPT_TYPE.UPDATE);
    }

    private boolean isTopNode(Long pid) {
        return ObjectUtils.isEmpty(pid) || pid.equals(0L);
    }

    private VisualizationNodeBO rootNode() {
        return new VisualizationNodeBO(0L, "root", false, 7, -1L, 0, 1);
    }

    private VisualizationNodeBO convert(VisualizationNodePO po) {
        return new VisualizationNodeBO(po.getId(), po.getName(), StringUtils.equals(po.getNodeType(), "leaf"), 9, po.getPid(), po.getExtraFlag(), po.getExtraFlag1());
    }

    public CoreVisualizationManage proxy() {
        return CommonBeanFactory.getBean(this.getClass());
    }

    @XpackInteract(value = "perFilterManage", recursion = true, invalid = true)
    public IPage<VisualizationResourceVO> query(int pageNum, int pageSize, VisualizationWorkbranchQueryRequest request) {
        IPage<VisualizationResourcePO> visualizationResourcePOPageIPage = proxy().queryVisualizationPage(pageNum, pageSize, request);
        if (ObjectUtils.isEmpty(visualizationResourcePOPageIPage)) {
            return null;
        }
        List<VisualizationResourceVO> vos = proxy().formatResult(visualizationResourcePOPageIPage.getRecords());
        IPage<VisualizationResourceVO> iPage = new Page<>();
        iPage.setCurrent(visualizationResourcePOPageIPage.getCurrent());
        iPage.setPages(visualizationResourcePOPageIPage.getPages());
        iPage.setSize(visualizationResourcePOPageIPage.getSize());
        iPage.setTotal(visualizationResourcePOPageIPage.getTotal());
        iPage.setRecords(vos);
        return iPage;
    }

    List<VisualizationResourceVO> formatResult(List<VisualizationResourcePO> pos) {
        if (CollectionUtils.isEmpty(pos)) {
            return new ArrayList<>();
        }
        return pos.stream().map(po ->
                new VisualizationResourceVO(
                        po.getId(), po.getResourceId(), po.getName(),
                        po.getType(), String.valueOf(po.getCreator()), String.valueOf(po.getLastEditor()), po.getLastEditTime(),
                        po.getFavorite(), 9, po.getExtFlag())).toList();
    }

    public IPage<VisualizationResourcePO> queryVisualizationPage(int goPage, int pageSize, VisualizationWorkbranchQueryRequest request) {
        Long uid = AuthUtils.getUser().getUserId();
        Map<String, Object> params = new HashMap<>();
        if (StringUtils.isNotBlank(request.getType())) {
            BusiResourceEnum busiResourceEnum = BusiResourceEnum.valueOf(request.getType().toUpperCase());
            if (ObjectUtils.isEmpty(busiResourceEnum)) {
                DEException.throwException("type is invalid");
            }
            params.put("type", request.getType());
        }
        String info = CommunityUtils.getInfo();
        if (StringUtils.isNotBlank(info)) {
            params.put("info", info);
        }
        params.put("isAsc", request.isAsc());
        Page<VisualizationResourcePO> page = new Page<>(goPage, pageSize);
        return extDataVisualizationMapper.findRecent(page, uid, request.getKeyword(), params);
    }

    @Transactional
    public void removeSnapshot(Long dvId) {
        if (dvId != null) {
            // 清理历史数据
            Set<Long> dvIds = new HashSet<>();
            dvIds.add(dvId);
            extDataVisualizationMapper.deleteDataVBatch(dvIds, CommonConstants.RESOURCE_TABLE.SNAPSHOT);
            extCoreChartMapper.deleteViewsBySceneId(dvId, CommonConstants.RESOURCE_TABLE.SNAPSHOT);
            linkageMapper.deleteViewLinkageFieldSnapshot(dvId, null);
            linkageMapper.deleteViewLinkageSnapshot(dvId, null);
            linkJumpMapper.deleteJumpTargetViewInfoWithVisualizationSnapshot(dvId);
            linkJumpMapper.deleteJumpInfoWithVisualizationSnapshot(dvId);
            linkJumpMapper.deleteJumpWithVisualizationSnapshot(dvId);
            outerParamsMapper.deleteOuterParamsTargetWithVisualizationIdSnapshot(dvId.toString());
            outerParamsMapper.deleteOuterParamsInfoWithVisualizationIdSnapshot(dvId.toString());
            outerParamsMapper.deleteOuterParamsWithVisualizationIdSnapshot(dvId.toString());
            //xpack 阈值告警
            chartViewManege.removeThreshold(dvId, CommonConstants.RESOURCE_TABLE.SNAPSHOT);

        }
    }

    @Transactional
    public void removeDvCore(Long dvId) {
        if (dvId != null) {
            // 清理历史数据
            Set<Long> dvIds = new HashSet<>();
            dvIds.add(dvId);
            extDataVisualizationMapper.deleteDataVBatch(dvIds, CommonConstants.RESOURCE_TABLE.CORE);
            extCoreChartMapper.deleteViewsBySceneId(dvId, CommonConstants.RESOURCE_TABLE.CORE);
            linkageMapper.deleteViewLinkageField(dvId, null);
            linkageMapper.deleteViewLinkage(dvId, null);
            linkJumpMapper.deleteJumpTargetViewInfoWithVisualization(dvId);
            linkJumpMapper.deleteJumpInfoWithVisualization(dvId);
            linkJumpMapper.deleteJumpWithVisualization(dvId);
            outerParamsMapper.deleteOuterParamsTargetWithVisualizationId(dvId.toString());
            outerParamsMapper.deleteOuterParamsInfoWithVisualizationId(dvId.toString());
            outerParamsMapper.deleteOuterParamsWithVisualizationId(dvId.toString());
            //xpack 阈值告警
            chartViewManege.removeThreshold(dvId, CommonConstants.RESOURCE_TABLE.CORE);
        }
    }

    @Transactional
    public void dvSnapshotRecover(Long dvId) {
        // 清理历史数据
        CoreVisualizationManage proxy = CommonBeanFactory.proxy(this.getClass());
        assert proxy != null;
        proxy.removeSnapshot(dvId);
        // 导入新数据
        extDataVisualizationMapper.snapshotDataV(dvId);
        extDataVisualizationMapper.snapshotViews(dvId);
        extDataVisualizationMapper.snapshotLinkJumpTargetViewInfo(dvId);
        extDataVisualizationMapper.snapshotLinkJumpInfo(dvId);
        extDataVisualizationMapper.snapshotLinkJump(dvId);
        extDataVisualizationMapper.snapshotLinkageField(dvId);
        extDataVisualizationMapper.snapshotLinkage(dvId);
        extDataVisualizationMapper.snapshotOuterParamsTargetViewInfo(dvId);
        extDataVisualizationMapper.snapshotOuterParamsInfo(dvId);
        extDataVisualizationMapper.snapshotOuterParams(dvId);
        //xpack 阈值告警
        chartViewManege.restoreThreshold(dvId, CommonConstants.RESOURCE_TABLE.SNAPSHOT);
    }

    @Transactional
    public void dvRestore(Long dvId) {
        extDataVisualizationMapper.restoreDataV(dvId);
        extDataVisualizationMapper.restoreViews(dvId);
        extDataVisualizationMapper.restoreLinkJumpTargetViewInfo(dvId);
        extDataVisualizationMapper.restoreLinkJumpInfo(dvId);
        extDataVisualizationMapper.restoreLinkJump(dvId);
        extDataVisualizationMapper.restoreLinkageField(dvId);
        extDataVisualizationMapper.restoreLinkage(dvId);
        extDataVisualizationMapper.restoreOuterParamsTargetViewInfo(dvId);
        extDataVisualizationMapper.restoreOuterParamsInfo(dvId);
        extDataVisualizationMapper.restoreOuterParams(dvId);
        //xpack 阈值告警
        chartViewManege.restoreThreshold(dvId, CommonConstants.RESOURCE_TABLE.CORE);
    }

}
