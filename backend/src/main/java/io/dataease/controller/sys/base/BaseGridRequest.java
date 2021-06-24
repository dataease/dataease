package io.dataease.controller.sys.base;

import io.dataease.base.mapper.ext.query.GridExample;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;

import java.io.Serializable;
import java.util.List;


@Data
public class BaseGridRequest implements Serializable {

    private List<ConditionEntity> conditions;

    private List<String> orders;

    public GridExample convertExample(){
        GridExample gridExample = new GridExample();
        if (CollectionUtils.isNotEmpty(conditions)) {
            GridExample.Criteria criteria = gridExample.createCriteria();
            conditions.forEach(criteria::addCondtion);
        }

        if (CollectionUtils.isNotEmpty(orders)){
            String orderByClause = String.join(", ", orders);
            gridExample.setOrderByClause(orderByClause);
        }

        return gridExample;
    }
}
