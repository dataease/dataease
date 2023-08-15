package io.dataease.datasource.manage;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.ds.vo.DatasourceDTO;
import io.dataease.constant.DataSourceType;
import io.dataease.datasource.dao.auto.entity.CoreDatasource;
import io.dataease.datasource.dao.auto.mapper.CoreDatasourceMapper;
import io.dataease.datasource.dao.ext.mapper.DataSourceExtMapper;
import io.dataease.datasource.dao.ext.po.DataSourceNodePO;
import io.dataease.datasource.dto.DatasourceNodeBO;
import io.dataease.exception.DEException;
import io.dataease.license.config.XpackInteract;
import io.dataease.model.BusiNodeRequest;
import io.dataease.model.BusiNodeVO;
import io.dataease.utils.TreeUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DataSourceManage {


    @Resource
    private DataSourceExtMapper dataSourceExtMapper;

    @Resource
    private CoreDatasourceMapper coreDatasourceMapper;

    private DatasourceNodeBO rootNode() {
        return new DatasourceNodeBO(0L, "root", false, 3, -1L, 0);
    }

    private DatasourceNodeBO convert(DataSourceNodePO po) {
        DataSourceType dataSourceType = DataSourceType.valueOf(po.getType());
        if (ObjectUtils.isEmpty(dataSourceType)) {
            dataSourceType = DataSourceType.mysql;
        }
        Integer flag = dataSourceType.getFlag();
        int extraFlag = StringUtils.equalsIgnoreCase("error", po.getStatus()) ? Math.negateExact(flag) : flag;
        return new DatasourceNodeBO(po.getId(), po.getName(), !StringUtils.equals(po.getType(), "folder"), 3, po.getPid(), extraFlag);
    }

    @XpackInteract(value = "authResourceTree", replace = true)
    public List<BusiNodeVO> tree(BusiNodeRequest request) {

        QueryWrapper<DataSourceNodePO> queryWrapper = new QueryWrapper<>();
        if (ObjectUtils.isNotEmpty(request.getLeaf()) && !request.getLeaf()) {
            queryWrapper.eq("type", "folder");
        }
        queryWrapper.orderByDesc("create_time");
        List<DatasourceNodeBO> nodes = new ArrayList<>();
        List<DataSourceNodePO> pos = dataSourceExtMapper.selectList(queryWrapper);
        if (ObjectUtils.isEmpty(request.getLeaf()) || !request.getLeaf()) nodes.add(rootNode());
        if (CollectionUtil.isNotEmpty(pos)) {
            nodes.addAll(pos.stream().map(this::convert).toList());
        }
        return TreeUtils.mergeTree(nodes, BusiNodeVO.class, false);
    }

    @XpackInteract(value = "datasourceResourceTree", before = false)
    public void innerSave(CoreDatasource coreDatasource) {
        coreDatasourceMapper.insert(coreDatasource);
    }

    @XpackInteract(value = "datasourceResourceTree", before = false)
    public void innerEdit(CoreDatasource coreDatasource) {
        coreDatasourceMapper.updateById(coreDatasource);
    }

    @XpackInteract(value = "datasourceResourceTree", before = false)
    public void move(DatasourceDTO dataSourceDTO) {
        Long id = dataSourceDTO.getId();
        CoreDatasource sourceData = null;
        if (ObjectUtils.isEmpty(id) || ObjectUtils.isEmpty(sourceData = coreDatasourceMapper.selectById(id))) {
            DEException.throwException("resource not exist");
        }
        sourceData.setPid(dataSourceDTO.getPid());
        sourceData.setName(dataSourceDTO.getName());
        coreDatasourceMapper.updateById(sourceData);
    }
}
