package io.dataease.request;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.util.List;

public class BaseGridRequest implements Serializable {

    private String keyword;

    private List<ConditionEntity> conditions;

    private List<String> orders;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public List<ConditionEntity> getConditions() {
        return conditions;
    }

    public void setConditions(List<ConditionEntity> conditions) {
        this.conditions = conditions;
    }

    public List<String> getOrders() {
        return orders;
    }

    public void setOrders(List<String> orders) {
        this.orders = orders;
    }

    public QueryWrapper convertQueryWrapper(QueryWrapper queryWrapper) {
        if (ObjectUtils.isEmpty(queryWrapper)) {
            queryWrapper = new QueryWrapper();
        }
        for (int i = 0; i < conditions.size(); i++) {
            ConditionEntity condition = conditions.get(i);
            String operator = condition.getOperator();
            Object value = condition.getValue();
            if (StringUtils.equalsIgnoreCase("eq", operator)) {
                queryWrapper.eq(condition.getField(), value);
            }
            if (StringUtils.equalsIgnoreCase("in", operator)) {
                List list = (List) value;
                queryWrapper.in(condition.getField(), list);
            }
            if (StringUtils.equalsIgnoreCase("between", operator)) {
                List list = (List) value;
                queryWrapper.between(condition.getField(), list.get(0), list.get(1));
            }
        }
        return queryWrapper;
    }

    public GridExample convertExample() {
        GridExample gridExample = new GridExample();
        if (!CollectionUtils.isEmpty(conditions)) {
            GridExample.Criteria criteria = gridExample.createCriteria();
            conditions.forEach(criteria::addCondition);
        }

        if (!CollectionUtils.isEmpty(orders)) {
            String orderByClause = String.join(", ", orders);
            gridExample.setOrderByClause(orderByClause);
        }

        return gridExample;
    }


}
