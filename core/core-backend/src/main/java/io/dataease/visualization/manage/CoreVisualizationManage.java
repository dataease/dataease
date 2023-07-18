package io.dataease.visualization.manage;

import cn.hutool.core.collection.CollectionUtil;
import io.dataease.api.permissions.auth.api.InteractiveAuthApi;
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

    public List<BusiNodeVO> tree(String busiType) {

        if (ObjectUtils.isNotEmpty(interactiveAuthApi)) {
            String authFlag = StringUtils.equals("dataV", busiType) ? "screen" : "panel";
            return interactiveAuthApi.resource(authFlag);
        }
        List<VisualizationNodeBO> nodes = new ArrayList<>();
        nodes.add(rootNode());
        List<VisualizationNodePO> pos = coreVisualiationExtMapper.queryNodes(busiType);
        if (CollectionUtil.isNotEmpty(pos)) {
            nodes.addAll(pos.stream().map(item -> convert(item, busiType)).collect(Collectors.toList()));
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
