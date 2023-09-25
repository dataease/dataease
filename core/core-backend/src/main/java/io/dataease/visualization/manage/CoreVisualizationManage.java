package io.dataease.visualization.manage;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.dataease.api.visualization.request.DataVisualizationBaseRequest;
import io.dataease.api.visualization.request.VisualizationWorkbranchQueryRequest;
import io.dataease.api.visualization.vo.VisualizationResourceVO;
import io.dataease.api.visualization.vo.VisualizationStoreVO;
import io.dataease.commons.constants.DataVisualizationConstants;
import io.dataease.constant.BusiResourceEnum;
import io.dataease.exception.DEException;
import io.dataease.license.config.XpackInteract;
import io.dataease.model.BusiNodeRequest;
import io.dataease.model.BusiNodeVO;
import io.dataease.utils.*;
import io.dataease.visualization.dao.auto.entity.DataVisualizationInfo;
import io.dataease.visualization.dao.auto.mapper.DataVisualizationInfoMapper;
import io.dataease.visualization.dao.ext.mapper.CoreVisualiationExtMapper;
import io.dataease.visualization.dao.ext.mapper.ExtDataVisualizationMapper;
import io.dataease.visualization.dao.ext.po.StorePO;
import io.dataease.visualization.dao.ext.po.VisualizationNodePO;
import io.dataease.visualization.dao.ext.po.VisualizationResourcePO;
import io.dataease.visualization.dto.VisualizationNodeBO;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

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

    @XpackInteract(value = "visualizationResourceTree", replace = true)
    public List<BusiNodeVO> tree(BusiNodeRequest request) {
        List<VisualizationNodeBO> nodes = new ArrayList<>();
        if (ObjectUtils.isEmpty(request.getLeaf()) || !request.getLeaf()) {
            nodes.add(rootNode());
        }
        QueryWrapper<Object> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("delete_flag", false);
        queryWrapper.eq(ObjectUtils.isNotEmpty(request.getLeaf()), "node_type", ObjectUtils.isNotEmpty(request.getLeaf()) && request.getLeaf() ? "leaf" : "folder");
        queryWrapper.eq("type", request.getBusiFlag());
        queryWrapper.orderByDesc("create_time");
        List<VisualizationNodePO> pos = extMapper.queryNodes(queryWrapper);
        if (CollectionUtil.isNotEmpty(pos)) {
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
            if (CollectionUtil.isNotEmpty(childrenIdList)) {
                stack.addAll(childrenIdList);
            }
        }
        extMapper.batchDel(delIds, System.currentTimeMillis(), AuthUtils.getUser().getUserId());
    }

    @XpackInteract(value = "visualizationResourceTree", before = false)
    public void move(DataVisualizationBaseRequest request) {
        if(!request.getMoveFromUpdate()){
            DataVisualizationInfo visualizationInfo = new DataVisualizationInfo();
            BeanUtils.copyBean(visualizationInfo, request);
            if (ObjectUtils.isEmpty(visualizationInfo.getId())) {
                DEException.throwException("resource not exist");
            }
            visualizationInfo.setUpdateTime(System.currentTimeMillis());
            mapper.updateById(visualizationInfo);
        }
    }

    @XpackInteract(value = "visualizationResourceTree", before = false)
    public Long innerSave(DataVisualizationInfo visualizationInfo) {
        if(visualizationInfo.getId() == null){
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
        return visualizationInfo.getId();
    }

    @XpackInteract(value = "visualizationResourceTree", before = false)
    public void innerEdit(DataVisualizationInfo visualizationInfo) {
        visualizationInfo.setUpdateTime(System.currentTimeMillis());
        visualizationInfo.setUpdateBy(AuthUtils.getUser().getUserId().toString());
        mapper.updateById(visualizationInfo);
    }

    private boolean isTopNode(Long pid) {
        return ObjectUtils.isEmpty(pid) || pid.equals(0L);
    }

    private VisualizationNodeBO rootNode() {
        return new VisualizationNodeBO(0L, "root", false, 3, -1L, 0);
    }

    private VisualizationNodeBO convert(VisualizationNodePO po) {
        return new VisualizationNodeBO(po.getId(), po.getName(), StringUtils.equals(po.getNodeType(), "leaf"), 3, po.getPid(), 0);
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

    List<VisualizationResourceVO> formatResult(List<VisualizationResourcePO> pos){
        if (CollectionUtil.isEmpty(pos)){
            return new ArrayList<>();
        }
        return pos.stream().map(po ->
                new VisualizationResourceVO(
                        po.getId(), po.getResourceId(), po.getName(),
                        po.getType(), String.valueOf(po.getCreator()),String.valueOf(po.getLastEditor()), po.getLastEditTime(),
                        po.getFavorite(),9)).toList();
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
        queryWrapper.eq("dvResource.last_editor", uid);
        queryWrapper.orderBy(true, request.isAsc(), "dvResource.last_edit_time");
        Page<VisualizationResourcePO> page = new Page<>(goPage, pageSize);
        return extDataVisualizationMapper.findRecent(page,uid, queryWrapper);
    }
}
