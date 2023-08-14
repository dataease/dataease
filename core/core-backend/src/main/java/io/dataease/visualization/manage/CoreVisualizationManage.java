package io.dataease.visualization.manage;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.visualization.request.DataVisualizationBaseRequest;
import io.dataease.commons.constants.DataVisualizationConstants;
import io.dataease.exception.DEException;
import io.dataease.license.config.XpackInteract;
import io.dataease.model.BusiNodeRequest;
import io.dataease.model.BusiNodeVO;
import io.dataease.utils.AuthUtils;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.IDUtils;
import io.dataease.utils.TreeUtils;
import io.dataease.visualization.dao.auto.entity.DataVisualizationInfo;
import io.dataease.visualization.dao.auto.mapper.DataVisualizationInfoMapper;
import io.dataease.visualization.dao.ext.mapper.CoreVisualiationExtMapper;
import io.dataease.visualization.dao.ext.po.VisualizationNodePO;
import io.dataease.visualization.dto.VisualizationNodeBO;
import jakarta.annotation.Resource;
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

    @XpackInteract(value = "visualizationResourceTree", replace = true)
    public List<BusiNodeVO> tree(BusiNodeRequest request) {

        List<VisualizationNodeBO> nodes = new ArrayList<>();
        if (ObjectUtils.isEmpty(request.getLeaf()) || !request.getLeaf()) nodes.add(rootNode());
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
        DataVisualizationInfo visualizationInfo = new DataVisualizationInfo();
        BeanUtils.copyBean(visualizationInfo, request);
        if (ObjectUtils.isEmpty(visualizationInfo.getId())) {
            DEException.throwException("resource not exist");
        }
        visualizationInfo.setUpdateTime(System.currentTimeMillis());
        mapper.updateById(visualizationInfo);
    }

    @XpackInteract(value = "visualizationResourceTree", before = false)
    public void innerSave(DataVisualizationInfo visualizationInfo) {
        visualizationInfo.setDeleteFlag(DataVisualizationConstants.DELETE_FLAG.AVAILABLE);
        visualizationInfo.setCreateBy(AuthUtils.getUser().getUserId().toString());
        visualizationInfo.setCreateTime(System.currentTimeMillis());
        mapper.insert(visualizationInfo);
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
}
