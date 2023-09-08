package io.dataease.plugins.common.entity;

import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.List;

@Data
public class XpackGridRequest implements Serializable {

    private List<XpackConditionEntity> conditions;

    private List<String> orders;

    public XpackGridExample convertExample(){
        XpackGridExample gridExample = new XpackGridExample();
        if (!CollectionUtils.isEmpty(conditions)){
            XpackGridExample.Criteria criteria = gridExample.createCriteria();
            conditions.forEach(criteria::addCondition);
        }
        if (!CollectionUtils.isEmpty(orders)) {
            String orderByClause = String.join(", ", orders);
            gridExample.setOrderByClause(orderByClause);
        }

        return gridExample;
    }
}
