package io.dataease.service.chart;

import io.dataease.plugins.common.base.mapper.DatasetTableFieldMapper;
import io.dataease.plugins.common.request.chart.filter.FilterTreeItem;
import io.dataease.plugins.common.request.chart.filter.FilterTreeObj;
import io.dataease.provider.query.SQLUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author Junjun
 */
@Service
public class ChartFilterTreeService {
    @Resource
    private DatasetTableFieldMapper datasetTableFieldMapper;

    public void searchFieldAndSet(FilterTreeObj tree) {
        if (ObjectUtils.isNotEmpty(tree)) {
            if (ObjectUtils.isNotEmpty(tree.getItems())) {
                for (FilterTreeItem item : tree.getItems()) {
                    if (ObjectUtils.isNotEmpty(item)) {
                        if (StringUtils.equalsIgnoreCase(item.getType(), "item") || ObjectUtils.isEmpty(item.getSubTree())) {
                            item.setField(datasetTableFieldMapper.selectByPrimaryKey(item.getFieldId()));
                        } else if (StringUtils.equalsIgnoreCase(item.getType(), "tree") || (ObjectUtils.isNotEmpty(item.getSubTree()) && StringUtils.isNotEmpty(item.getSubTree().getLogic()))) {
                            searchFieldAndSet(item.getSubTree());
                        }
                    }
                }
            }
        }
    }

    public FilterTreeObj charReplace(FilterTreeObj tree) {
        if (ObjectUtils.isNotEmpty(tree)) {
            if (ObjectUtils.isNotEmpty(tree.getItems())) {
                for (FilterTreeItem item : tree.getItems()) {
                    if (ObjectUtils.isNotEmpty(item)) {
                        if (StringUtils.equalsIgnoreCase(item.getType(), "item") || ObjectUtils.isEmpty(item.getSubTree())) {
                            if (CollectionUtils.isNotEmpty(item.getEnumValue())) {
                                List<String> collect = item.getEnumValue().stream().map(SQLUtils::transKeyword).collect(Collectors.toList());
                                item.setEnumValue(collect);
                            }
                            item.setValue(SQLUtils.transKeyword(item.getValue()));
                        } else if (StringUtils.equalsIgnoreCase(item.getType(), "tree") || (ObjectUtils.isNotEmpty(item.getSubTree()) && StringUtils.isNotEmpty(item.getSubTree().getLogic()))) {
                            charReplace(item.getSubTree());
                        }
                    }
                }
            }
        }
        return tree;
    }
}
