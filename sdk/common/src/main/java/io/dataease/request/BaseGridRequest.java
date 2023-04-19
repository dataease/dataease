package io.dataease.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.List;

@ApiModel("查询条件")
public class BaseGridRequest implements Serializable {

    private String keyword;

    @ApiModelProperty("条件集合")
    private List<ConditionEntity> conditions;

    @ApiModelProperty("排序描述")
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

    public GridExample convertExample(){
        GridExample gridExample = new GridExample();
        if (!CollectionUtils.isEmpty(conditions)) {
            GridExample.Criteria criteria = gridExample.createCriteria();
            conditions.forEach(criteria::addCondition);
        }

        if (!CollectionUtils.isEmpty(orders)){
            String orderByClause = String.join(", ", orders);
            gridExample.setOrderByClause(orderByClause);
        }

        return gridExample;
    }


}
