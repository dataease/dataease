package io.dataease.visualization.manage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.dataease.api.visualization.request.DataVisualizationBaseRequest;
import io.dataease.api.visualization.request.VisualizationWorkbranchQueryRequest;
import io.dataease.api.visualization.vo.VisualizationResourceVO;
import io.dataease.commons.constants.DataVisualizationConstants;
import io.dataease.commons.constants.OptConstants;
import io.dataease.constant.BusiResourceEnum;
import io.dataease.exception.DEException;
import io.dataease.license.config.XpackInteract;
import io.dataease.model.BusiNodeRequest;
import io.dataease.model.BusiNodeVO;
import io.dataease.operation.manage.CoreOptRecentManage;
import io.dataease.utils.*;
import io.dataease.visualization.dao.auto.entity.DataVisualizationInfo;
import io.dataease.visualization.dao.auto.mapper.DataVisualizationInfoMapper;
import io.dataease.visualization.dao.ext.mapper.CoreVisualiationExtMapper;
import io.dataease.visualization.dao.ext.mapper.ExtDataVisualizationMapper;
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
    private ExtDataVisualizationMapper extDataVisualizationMapper;

    @Resource
    private CoreOptRecentManage coreOptRecentManage;

    @XpackInteract(value = "visualizationResourceTree", replace = true)
    public List<BusiNodeVO> tree(BusiNodeRequest request) {
        List<VisualizationNodeBO> nodes = new ArrayList<>();
        if (ObjectUtils.isEmpty(request.getLeaf()) || !request.getLeaf()) {
            nodes.add(rootNode());
        }
        QueryWrapper<Object> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("delete_flag", false);
        if(StringUtils.isNotEmpty(request.getPid())) {
            queryWrapper.eq("pid", request.getPid());
        } else {
            queryWrapper.ne("pid", -1);
        }
        queryWrapper.eq(ObjectUtils.isNotEmpty(request.getLeaf()), "node_type", ObjectUtils.isNotEmpty(request.getLeaf()) && request.getLeaf() ? "leaf" : "folder");
        queryWrapper.eq("type", request.getBusiFlag());
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
        extMapper.batchDel(delIds, System.currentTimeMillis(), AuthUtils.getUser().getUserId());
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
            coreOptRecentManage.saveOpt(visualizationInfo.getId(), OptConstants.OPT_RESOURCE_TYPE.VISUALIZATION, OptConstants.OPT_TYPE.UPDATE);
            mapper.updateById(visualizationInfo);
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
        visualizationInfo.setCreateBy(AuthUtils.getUser().getUserId().toString());
        visualizationInfo.setUpdateBy(AuthUtils.getUser().getUserId().toString());
        visualizationInfo.setCreateTime(System.currentTimeMillis());
        visualizationInfo.setUpdateTime(System.currentTimeMillis());
        visualizationInfo.setOrgId(AuthUtils.getUser().getDefaultOid());
        mapper.insert(visualizationInfo);
        coreOptRecentManage.saveOpt(visualizationInfo.getId(), OptConstants.OPT_RESOURCE_TYPE.VISUALIZATION, OptConstants.OPT_TYPE.NEW);
        return visualizationInfo.getId();
    }

    @XpackInteract(value = "visualizationResourceTree", before = false)
    public void innerEdit(DataVisualizationInfo visualizationInfo) {
        visualizationInfo.setUpdateTime(System.currentTimeMillis());
        visualizationInfo.setUpdateBy(AuthUtils.getUser().getUserId().toString());
        visualizationInfo.setVersion(3);
        mapper.updateById(visualizationInfo);
        coreOptRecentManage.saveOpt(visualizationInfo.getId(), OptConstants.OPT_RESOURCE_TYPE.VISUALIZATION, OptConstants.OPT_TYPE.UPDATE);
    }

    private boolean isTopNode(Long pid) {
        return ObjectUtils.isEmpty(pid) || pid.equals(0L);
    }

    private VisualizationNodeBO rootNode() {
        return new VisualizationNodeBO(0L, "root", false, 7, -1L, 0);
    }

    private VisualizationNodeBO convert(VisualizationNodePO po) {
        return new VisualizationNodeBO(po.getId(), po.getName(), StringUtils.equals(po.getNodeType(), "leaf"), 7, po.getPid(), po.getExtraFlag());
    }

    public CoreVisualizationManage proxy() {
        return CommonBeanFactory.getBean(this.getClass());
    }

    @XpackInteract(value = "perFilterManage", recursion = true)
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
                        po.getFavorite(), 9,po.getExtFlag())).toList();
    }

    public IPage<VisualizationResourcePO> queryVisualizationPage(int goPage, int pageSize, VisualizationWorkbranchQueryRequest request) {
        Long uid = AuthUtils.getUser().getUserId();
        QueryWrapper<Object> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(request.getType())) {
            BusiResourceEnum busiResourceEnum = BusiResourceEnum.valueOf(request.getType().toUpperCase());
            if (ObjectUtils.isEmpty(busiResourceEnum)) {
                DEException.throwException("type is invalid");
            }
            queryWrapper.eq("dvResource.type", request.getType());
        }
        if (StringUtils.isNotBlank(request.getKeyword())) {
            queryWrapper.like("dvResource.name", request.getKeyword());
        }
        queryWrapper.orderBy(true, request.isAsc(), "core_opt_recent.time");
        Page<VisualizationResourcePO> page = new Page<>(goPage, pageSize);
        return extDataVisualizationMapper.findRecent(page, uid, queryWrapper);
    }
}
