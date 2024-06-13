package io.dataease.chart.manage;

import io.dataease.extensions.view.filter.FilterTreeItem;
import io.dataease.extensions.view.filter.FilterTreeObj;
import io.dataease.dataset.dao.auto.entity.CoreDatasetTableField;
import io.dataease.dataset.dao.auto.mapper.CoreDatasetTableFieldMapper;
import io.dataease.extensions.view.dto.DatasetTableFieldDTO;
import io.dataease.engine.utils.SQLUtils;
import io.dataease.utils.BeanUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author Junjun
 */
@Service
public class ChartFilterTreeService {
    @Resource
    private CoreDatasetTableFieldMapper coreDatasetTableFieldMapper;

    public void searchFieldAndSet(FilterTreeObj tree) {
        if (ObjectUtils.isNotEmpty(tree)) {
            if (ObjectUtils.isNotEmpty(tree.getItems())) {
                for (FilterTreeItem item : tree.getItems()) {
                    if (ObjectUtils.isNotEmpty(item)) {
                        if (StringUtils.equalsIgnoreCase(item.getType(), "item") || ObjectUtils.isEmpty(item.getSubTree())) {
                            CoreDatasetTableField coreDatasetTableField = coreDatasetTableFieldMapper.selectById(item.getFieldId());
                            DatasetTableFieldDTO dto = new DatasetTableFieldDTO();
                            BeanUtils.copyBean(dto, coreDatasetTableField);
                            item.setField(dto);
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
