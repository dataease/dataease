package io.dataease.datasource.manage;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.ds.vo.TreeNodeVO;
import io.dataease.model.BusiNodeVO;
import io.dataease.commons.constants.DataSourceType;
import io.dataease.datasource.dao.ext.mapper.DataSourceExtMapper;
import io.dataease.datasource.dao.ext.po.DataSourceNodePO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Component
public class DataSourceManage {

    @Resource
    private DataSourceExtMapper dataSourceExtMapper;
    public TreeNodeVO convertTreeVO(BusiNodeVO perVO) {
        int extraFlag = perVO.getExtraFlag();
        boolean valid = extraFlag >= 0;
        int abs = Math.abs(extraFlag);
        DataSourceType dataSourceType = DataSourceType.fromFlag(abs);
        if (ObjectUtils.isEmpty(dataSourceType)) {
            dataSourceType = DataSourceType.mysql;
        }
        return new TreeNodeVO(perVO.getId(), perVO.getName(), dataSourceType.name(), valid);
    }

    public List<TreeNodeVO> treeNodeQuery() {
        QueryWrapper<DataSourceNodePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        List<DataSourceNodePO> poList = dataSourceExtMapper.selectList(queryWrapper);
        if (CollectionUtil.isNotEmpty(poList))
            return poList.stream().map(po -> new TreeNodeVO(po.getId(), po.getName(), po.getType(), po.getStatus().equals("Success"))).toList();
        return null;
    }
}
