package io.dataease.visualization.manage;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.permissions.auth.api.InteractiveAuthApi;
import io.dataease.model.BusiNodeRequest;
import io.dataease.model.BusiNodeVO;
import io.dataease.utils.TreeUtils;
import io.dataease.visualization.dao.ext.mapper.CoreVisualiationExtMapper;
import io.dataease.visualization.dao.ext.po.VisualizationNodePO;
import io.dataease.visualization.dto.VisualizationNodeBO;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CoreVisualizationManage {

    @Autowired(required = false)
    private InteractiveAuthApi interactiveAuthApi;

    @Resource
    private CoreVisualiationExtMapper coreVisualiationExtMapper;

    public List<BusiNodeVO> tree(BusiNodeRequest request) {
        String busyType = request.getBusiFlag();
        if (ObjectUtils.isNotEmpty(interactiveAuthApi)) {
            String authFlag = StringUtils.equals("dataV", busyType) ? "screen" : "panel";
            request.setBusiFlag(authFlag);
            return interactiveAuthApi.resource(request);
        }
        List<VisualizationNodeBO> nodes = new ArrayList<>();
        if (ObjectUtils.isEmpty(request.getLeaf()) || !request.getLeaf()) nodes.add(rootNode());
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("delete_flag", false);
        queryWrapper.eq(ObjectUtils.isNotEmpty(request.getLeaf()), "node_type", ObjectUtils.isNotEmpty(request.getLeaf()) && request.getLeaf() ? "leaf" : "folder");
        queryWrapper.eq("type", busyType);
        queryWrapper.orderByDesc("create_time");
        List<VisualizationNodePO> pos = coreVisualiationExtMapper.queryNodes(queryWrapper);
        if (CollectionUtil.isNotEmpty(pos)) {
            nodes.addAll(pos.stream().map(item -> convert(item, busyType)).collect(Collectors.toList()));
        }
        return TreeUtils.mergeTree(nodes, BusiNodeVO.class, false);
    }

    private VisualizationNodeBO rootNode() {
        return new VisualizationNodeBO(0L, "root", false, 3, -1L, 0);
    }

    private VisualizationNodeBO convert(VisualizationNodePO po, String busiType) {
        return new VisualizationNodeBO(po.getId(), po.getName(), StringUtils.equals(po.getNodeType(), "leaf"), 3, po.getPid(), 0);
    }
}
