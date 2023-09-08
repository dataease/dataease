package io.dataease.plugins.common.base.domain;

import java.util.ArrayList;
import java.util.List;

public class DatasetTableUnionExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public DatasetTableUnionExample() {
        oredCriteria = new ArrayList<Criteria>();
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<Criterion>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andIdIsNull() {
            addCriterion("id is null");
            return (Criteria) this;
        }

        public Criteria andIdIsNotNull() {
            addCriterion("id is not null");
            return (Criteria) this;
        }

        public Criteria andIdEqualTo(String value) {
            addCriterion("id =", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(String value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(String value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(String value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThan(String value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(String value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLike(String value) {
            addCriterion("id like", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotLike(String value) {
            addCriterion("id not like", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdIn(List<String> values) {
            addCriterion("id in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotIn(List<String> values) {
            addCriterion("id not in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdBetween(String value1, String value2) {
            addCriterion("id between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotBetween(String value1, String value2) {
            addCriterion("id not between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andSourceTableIdIsNull() {
            addCriterion("source_table_id is null");
            return (Criteria) this;
        }

        public Criteria andSourceTableIdIsNotNull() {
            addCriterion("source_table_id is not null");
            return (Criteria) this;
        }

        public Criteria andSourceTableIdEqualTo(String value) {
            addCriterion("source_table_id =", value, "sourceTableId");
            return (Criteria) this;
        }

        public Criteria andSourceTableIdNotEqualTo(String value) {
            addCriterion("source_table_id <>", value, "sourceTableId");
            return (Criteria) this;
        }

        public Criteria andSourceTableIdGreaterThan(String value) {
            addCriterion("source_table_id >", value, "sourceTableId");
            return (Criteria) this;
        }

        public Criteria andSourceTableIdGreaterThanOrEqualTo(String value) {
            addCriterion("source_table_id >=", value, "sourceTableId");
            return (Criteria) this;
        }

        public Criteria andSourceTableIdLessThan(String value) {
            addCriterion("source_table_id <", value, "sourceTableId");
            return (Criteria) this;
        }

        public Criteria andSourceTableIdLessThanOrEqualTo(String value) {
            addCriterion("source_table_id <=", value, "sourceTableId");
            return (Criteria) this;
        }

        public Criteria andSourceTableIdLike(String value) {
            addCriterion("source_table_id like", value, "sourceTableId");
            return (Criteria) this;
        }

        public Criteria andSourceTableIdNotLike(String value) {
            addCriterion("source_table_id not like", value, "sourceTableId");
            return (Criteria) this;
        }

        public Criteria andSourceTableIdIn(List<String> values) {
            addCriterion("source_table_id in", values, "sourceTableId");
            return (Criteria) this;
        }

        public Criteria andSourceTableIdNotIn(List<String> values) {
            addCriterion("source_table_id not in", values, "sourceTableId");
            return (Criteria) this;
        }

        public Criteria andSourceTableIdBetween(String value1, String value2) {
            addCriterion("source_table_id between", value1, value2, "sourceTableId");
            return (Criteria) this;
        }

        public Criteria andSourceTableIdNotBetween(String value1, String value2) {
            addCriterion("source_table_id not between", value1, value2, "sourceTableId");
            return (Criteria) this;
        }

        public Criteria andSourceTableFieldIdIsNull() {
            addCriterion("source_table_field_id is null");
            return (Criteria) this;
        }

        public Criteria andSourceTableFieldIdIsNotNull() {
            addCriterion("source_table_field_id is not null");
            return (Criteria) this;
        }

        public Criteria andSourceTableFieldIdEqualTo(String value) {
            addCriterion("source_table_field_id =", value, "sourceTableFieldId");
            return (Criteria) this;
        }

        public Criteria andSourceTableFieldIdNotEqualTo(String value) {
            addCriterion("source_table_field_id <>", value, "sourceTableFieldId");
            return (Criteria) this;
        }

        public Criteria andSourceTableFieldIdGreaterThan(String value) {
            addCriterion("source_table_field_id >", value, "sourceTableFieldId");
            return (Criteria) this;
        }

        public Criteria andSourceTableFieldIdGreaterThanOrEqualTo(String value) {
            addCriterion("source_table_field_id >=", value, "sourceTableFieldId");
            return (Criteria) this;
        }

        public Criteria andSourceTableFieldIdLessThan(String value) {
            addCriterion("source_table_field_id <", value, "sourceTableFieldId");
            return (Criteria) this;
        }

        public Criteria andSourceTableFieldIdLessThanOrEqualTo(String value) {
            addCriterion("source_table_field_id <=", value, "sourceTableFieldId");
            return (Criteria) this;
        }

        public Criteria andSourceTableFieldIdLike(String value) {
            addCriterion("source_table_field_id like", value, "sourceTableFieldId");
            return (Criteria) this;
        }

        public Criteria andSourceTableFieldIdNotLike(String value) {
            addCriterion("source_table_field_id not like", value, "sourceTableFieldId");
            return (Criteria) this;
        }

        public Criteria andSourceTableFieldIdIn(List<String> values) {
            addCriterion("source_table_field_id in", values, "sourceTableFieldId");
            return (Criteria) this;
        }

        public Criteria andSourceTableFieldIdNotIn(List<String> values) {
            addCriterion("source_table_field_id not in", values, "sourceTableFieldId");
            return (Criteria) this;
        }

        public Criteria andSourceTableFieldIdBetween(String value1, String value2) {
            addCriterion("source_table_field_id between", value1, value2, "sourceTableFieldId");
            return (Criteria) this;
        }

        public Criteria andSourceTableFieldIdNotBetween(String value1, String value2) {
            addCriterion("source_table_field_id not between", value1, value2, "sourceTableFieldId");
            return (Criteria) this;
        }

        public Criteria andSourceUnionRelationIsNull() {
            addCriterion("source_union_relation is null");
            return (Criteria) this;
        }

        public Criteria andSourceUnionRelationIsNotNull() {
            addCriterion("source_union_relation is not null");
            return (Criteria) this;
        }

        public Criteria andSourceUnionRelationEqualTo(String value) {
            addCriterion("source_union_relation =", value, "sourceUnionRelation");
            return (Criteria) this;
        }

        public Criteria andSourceUnionRelationNotEqualTo(String value) {
            addCriterion("source_union_relation <>", value, "sourceUnionRelation");
            return (Criteria) this;
        }

        public Criteria andSourceUnionRelationGreaterThan(String value) {
            addCriterion("source_union_relation >", value, "sourceUnionRelation");
            return (Criteria) this;
        }

        public Criteria andSourceUnionRelationGreaterThanOrEqualTo(String value) {
            addCriterion("source_union_relation >=", value, "sourceUnionRelation");
            return (Criteria) this;
        }

        public Criteria andSourceUnionRelationLessThan(String value) {
            addCriterion("source_union_relation <", value, "sourceUnionRelation");
            return (Criteria) this;
        }

        public Criteria andSourceUnionRelationLessThanOrEqualTo(String value) {
            addCriterion("source_union_relation <=", value, "sourceUnionRelation");
            return (Criteria) this;
        }

        public Criteria andSourceUnionRelationLike(String value) {
            addCriterion("source_union_relation like", value, "sourceUnionRelation");
            return (Criteria) this;
        }

        public Criteria andSourceUnionRelationNotLike(String value) {
            addCriterion("source_union_relation not like", value, "sourceUnionRelation");
            return (Criteria) this;
        }

        public Criteria andSourceUnionRelationIn(List<String> values) {
            addCriterion("source_union_relation in", values, "sourceUnionRelation");
            return (Criteria) this;
        }

        public Criteria andSourceUnionRelationNotIn(List<String> values) {
            addCriterion("source_union_relation not in", values, "sourceUnionRelation");
            return (Criteria) this;
        }

        public Criteria andSourceUnionRelationBetween(String value1, String value2) {
            addCriterion("source_union_relation between", value1, value2, "sourceUnionRelation");
            return (Criteria) this;
        }

        public Criteria andSourceUnionRelationNotBetween(String value1, String value2) {
            addCriterion("source_union_relation not between", value1, value2, "sourceUnionRelation");
            return (Criteria) this;
        }

        public Criteria andTargetTableIdIsNull() {
            addCriterion("target_table_id is null");
            return (Criteria) this;
        }

        public Criteria andTargetTableIdIsNotNull() {
            addCriterion("target_table_id is not null");
            return (Criteria) this;
        }

        public Criteria andTargetTableIdEqualTo(String value) {
            addCriterion("target_table_id =", value, "targetTableId");
            return (Criteria) this;
        }

        public Criteria andTargetTableIdNotEqualTo(String value) {
            addCriterion("target_table_id <>", value, "targetTableId");
            return (Criteria) this;
        }

        public Criteria andTargetTableIdGreaterThan(String value) {
            addCriterion("target_table_id >", value, "targetTableId");
            return (Criteria) this;
        }

        public Criteria andTargetTableIdGreaterThanOrEqualTo(String value) {
            addCriterion("target_table_id >=", value, "targetTableId");
            return (Criteria) this;
        }

        public Criteria andTargetTableIdLessThan(String value) {
            addCriterion("target_table_id <", value, "targetTableId");
            return (Criteria) this;
        }

        public Criteria andTargetTableIdLessThanOrEqualTo(String value) {
            addCriterion("target_table_id <=", value, "targetTableId");
            return (Criteria) this;
        }

        public Criteria andTargetTableIdLike(String value) {
            addCriterion("target_table_id like", value, "targetTableId");
            return (Criteria) this;
        }

        public Criteria andTargetTableIdNotLike(String value) {
            addCriterion("target_table_id not like", value, "targetTableId");
            return (Criteria) this;
        }

        public Criteria andTargetTableIdIn(List<String> values) {
            addCriterion("target_table_id in", values, "targetTableId");
            return (Criteria) this;
        }

        public Criteria andTargetTableIdNotIn(List<String> values) {
            addCriterion("target_table_id not in", values, "targetTableId");
            return (Criteria) this;
        }

        public Criteria andTargetTableIdBetween(String value1, String value2) {
            addCriterion("target_table_id between", value1, value2, "targetTableId");
            return (Criteria) this;
        }

        public Criteria andTargetTableIdNotBetween(String value1, String value2) {
            addCriterion("target_table_id not between", value1, value2, "targetTableId");
            return (Criteria) this;
        }

        public Criteria andTargetTableFieldIdIsNull() {
            addCriterion("target_table_field_id is null");
            return (Criteria) this;
        }

        public Criteria andTargetTableFieldIdIsNotNull() {
            addCriterion("target_table_field_id is not null");
            return (Criteria) this;
        }

        public Criteria andTargetTableFieldIdEqualTo(String value) {
            addCriterion("target_table_field_id =", value, "targetTableFieldId");
            return (Criteria) this;
        }

        public Criteria andTargetTableFieldIdNotEqualTo(String value) {
            addCriterion("target_table_field_id <>", value, "targetTableFieldId");
            return (Criteria) this;
        }

        public Criteria andTargetTableFieldIdGreaterThan(String value) {
            addCriterion("target_table_field_id >", value, "targetTableFieldId");
            return (Criteria) this;
        }

        public Criteria andTargetTableFieldIdGreaterThanOrEqualTo(String value) {
            addCriterion("target_table_field_id >=", value, "targetTableFieldId");
            return (Criteria) this;
        }

        public Criteria andTargetTableFieldIdLessThan(String value) {
            addCriterion("target_table_field_id <", value, "targetTableFieldId");
            return (Criteria) this;
        }

        public Criteria andTargetTableFieldIdLessThanOrEqualTo(String value) {
            addCriterion("target_table_field_id <=", value, "targetTableFieldId");
            return (Criteria) this;
        }

        public Criteria andTargetTableFieldIdLike(String value) {
            addCriterion("target_table_field_id like", value, "targetTableFieldId");
            return (Criteria) this;
        }

        public Criteria andTargetTableFieldIdNotLike(String value) {
            addCriterion("target_table_field_id not like", value, "targetTableFieldId");
            return (Criteria) this;
        }

        public Criteria andTargetTableFieldIdIn(List<String> values) {
            addCriterion("target_table_field_id in", values, "targetTableFieldId");
            return (Criteria) this;
        }

        public Criteria andTargetTableFieldIdNotIn(List<String> values) {
            addCriterion("target_table_field_id not in", values, "targetTableFieldId");
            return (Criteria) this;
        }

        public Criteria andTargetTableFieldIdBetween(String value1, String value2) {
            addCriterion("target_table_field_id between", value1, value2, "targetTableFieldId");
            return (Criteria) this;
        }

        public Criteria andTargetTableFieldIdNotBetween(String value1, String value2) {
            addCriterion("target_table_field_id not between", value1, value2, "targetTableFieldId");
            return (Criteria) this;
        }

        public Criteria andTargetUnionRelationIsNull() {
            addCriterion("target_union_relation is null");
            return (Criteria) this;
        }

        public Criteria andTargetUnionRelationIsNotNull() {
            addCriterion("target_union_relation is not null");
            return (Criteria) this;
        }

        public Criteria andTargetUnionRelationEqualTo(String value) {
            addCriterion("target_union_relation =", value, "targetUnionRelation");
            return (Criteria) this;
        }

        public Criteria andTargetUnionRelationNotEqualTo(String value) {
            addCriterion("target_union_relation <>", value, "targetUnionRelation");
            return (Criteria) this;
        }

        public Criteria andTargetUnionRelationGreaterThan(String value) {
            addCriterion("target_union_relation >", value, "targetUnionRelation");
            return (Criteria) this;
        }

        public Criteria andTargetUnionRelationGreaterThanOrEqualTo(String value) {
            addCriterion("target_union_relation >=", value, "targetUnionRelation");
            return (Criteria) this;
        }

        public Criteria andTargetUnionRelationLessThan(String value) {
            addCriterion("target_union_relation <", value, "targetUnionRelation");
            return (Criteria) this;
        }

        public Criteria andTargetUnionRelationLessThanOrEqualTo(String value) {
            addCriterion("target_union_relation <=", value, "targetUnionRelation");
            return (Criteria) this;
        }

        public Criteria andTargetUnionRelationLike(String value) {
            addCriterion("target_union_relation like", value, "targetUnionRelation");
            return (Criteria) this;
        }

        public Criteria andTargetUnionRelationNotLike(String value) {
            addCriterion("target_union_relation not like", value, "targetUnionRelation");
            return (Criteria) this;
        }

        public Criteria andTargetUnionRelationIn(List<String> values) {
            addCriterion("target_union_relation in", values, "targetUnionRelation");
            return (Criteria) this;
        }

        public Criteria andTargetUnionRelationNotIn(List<String> values) {
            addCriterion("target_union_relation not in", values, "targetUnionRelation");
            return (Criteria) this;
        }

        public Criteria andTargetUnionRelationBetween(String value1, String value2) {
            addCriterion("target_union_relation between", value1, value2, "targetUnionRelation");
            return (Criteria) this;
        }

        public Criteria andTargetUnionRelationNotBetween(String value1, String value2) {
            addCriterion("target_union_relation not between", value1, value2, "targetUnionRelation");
            return (Criteria) this;
        }

        public Criteria andCreateByIsNull() {
            addCriterion("create_by is null");
            return (Criteria) this;
        }

        public Criteria andCreateByIsNotNull() {
            addCriterion("create_by is not null");
            return (Criteria) this;
        }

        public Criteria andCreateByEqualTo(String value) {
            addCriterion("create_by =", value, "createBy");
            return (Criteria) this;
        }

        public Criteria andCreateByNotEqualTo(String value) {
            addCriterion("create_by <>", value, "createBy");
            return (Criteria) this;
        }

        public Criteria andCreateByGreaterThan(String value) {
            addCriterion("create_by >", value, "createBy");
            return (Criteria) this;
        }

        public Criteria andCreateByGreaterThanOrEqualTo(String value) {
            addCriterion("create_by >=", value, "createBy");
            return (Criteria) this;
        }

        public Criteria andCreateByLessThan(String value) {
            addCriterion("create_by <", value, "createBy");
            return (Criteria) this;
        }

        public Criteria andCreateByLessThanOrEqualTo(String value) {
            addCriterion("create_by <=", value, "createBy");
            return (Criteria) this;
        }

        public Criteria andCreateByLike(String value) {
            addCriterion("create_by like", value, "createBy");
            return (Criteria) this;
        }

        public Criteria andCreateByNotLike(String value) {
            addCriterion("create_by not like", value, "createBy");
            return (Criteria) this;
        }

        public Criteria andCreateByIn(List<String> values) {
            addCriterion("create_by in", values, "createBy");
            return (Criteria) this;
        }

        public Criteria andCreateByNotIn(List<String> values) {
            addCriterion("create_by not in", values, "createBy");
            return (Criteria) this;
        }

        public Criteria andCreateByBetween(String value1, String value2) {
            addCriterion("create_by between", value1, value2, "createBy");
            return (Criteria) this;
        }

        public Criteria andCreateByNotBetween(String value1, String value2) {
            addCriterion("create_by not between", value1, value2, "createBy");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNull() {
            addCriterion("create_time is null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNotNull() {
            addCriterion("create_time is not null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeEqualTo(Long value) {
            addCriterion("create_time =", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotEqualTo(Long value) {
            addCriterion("create_time <>", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThan(Long value) {
            addCriterion("create_time >", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThanOrEqualTo(Long value) {
            addCriterion("create_time >=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThan(Long value) {
            addCriterion("create_time <", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThanOrEqualTo(Long value) {
            addCriterion("create_time <=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIn(List<Long> values) {
            addCriterion("create_time in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotIn(List<Long> values) {
            addCriterion("create_time not in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeBetween(Long value1, Long value2) {
            addCriterion("create_time between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotBetween(Long value1, Long value2) {
            addCriterion("create_time not between", value1, value2, "createTime");
            return (Criteria) this;
        }
    }

    public static class Criteria extends GeneratedCriteria {

        protected Criteria() {
            super();
        }
    }

    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}