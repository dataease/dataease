package io.dataease.base.domain;

import java.util.ArrayList;
import java.util.List;

public class PanelOuterParamsTargetViewInfoExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public PanelOuterParamsTargetViewInfoExample() {
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

        public Criteria andTargetIdIsNull() {
            addCriterion("target_id is null");
            return (Criteria) this;
        }

        public Criteria andTargetIdIsNotNull() {
            addCriterion("target_id is not null");
            return (Criteria) this;
        }

        public Criteria andTargetIdEqualTo(String value) {
            addCriterion("target_id =", value, "targetId");
            return (Criteria) this;
        }

        public Criteria andTargetIdNotEqualTo(String value) {
            addCriterion("target_id <>", value, "targetId");
            return (Criteria) this;
        }

        public Criteria andTargetIdGreaterThan(String value) {
            addCriterion("target_id >", value, "targetId");
            return (Criteria) this;
        }

        public Criteria andTargetIdGreaterThanOrEqualTo(String value) {
            addCriterion("target_id >=", value, "targetId");
            return (Criteria) this;
        }

        public Criteria andTargetIdLessThan(String value) {
            addCriterion("target_id <", value, "targetId");
            return (Criteria) this;
        }

        public Criteria andTargetIdLessThanOrEqualTo(String value) {
            addCriterion("target_id <=", value, "targetId");
            return (Criteria) this;
        }

        public Criteria andTargetIdLike(String value) {
            addCriterion("target_id like", value, "targetId");
            return (Criteria) this;
        }

        public Criteria andTargetIdNotLike(String value) {
            addCriterion("target_id not like", value, "targetId");
            return (Criteria) this;
        }

        public Criteria andTargetIdIn(List<String> values) {
            addCriterion("target_id in", values, "targetId");
            return (Criteria) this;
        }

        public Criteria andTargetIdNotIn(List<String> values) {
            addCriterion("target_id not in", values, "targetId");
            return (Criteria) this;
        }

        public Criteria andTargetIdBetween(String value1, String value2) {
            addCriterion("target_id between", value1, value2, "targetId");
            return (Criteria) this;
        }

        public Criteria andTargetIdNotBetween(String value1, String value2) {
            addCriterion("target_id not between", value1, value2, "targetId");
            return (Criteria) this;
        }

        public Criteria andParamsInfoIdIsNull() {
            addCriterion("params_info_id is null");
            return (Criteria) this;
        }

        public Criteria andParamsInfoIdIsNotNull() {
            addCriterion("params_info_id is not null");
            return (Criteria) this;
        }

        public Criteria andParamsInfoIdEqualTo(String value) {
            addCriterion("params_info_id =", value, "paramsInfoId");
            return (Criteria) this;
        }

        public Criteria andParamsInfoIdNotEqualTo(String value) {
            addCriterion("params_info_id <>", value, "paramsInfoId");
            return (Criteria) this;
        }

        public Criteria andParamsInfoIdGreaterThan(String value) {
            addCriterion("params_info_id >", value, "paramsInfoId");
            return (Criteria) this;
        }

        public Criteria andParamsInfoIdGreaterThanOrEqualTo(String value) {
            addCriterion("params_info_id >=", value, "paramsInfoId");
            return (Criteria) this;
        }

        public Criteria andParamsInfoIdLessThan(String value) {
            addCriterion("params_info_id <", value, "paramsInfoId");
            return (Criteria) this;
        }

        public Criteria andParamsInfoIdLessThanOrEqualTo(String value) {
            addCriterion("params_info_id <=", value, "paramsInfoId");
            return (Criteria) this;
        }

        public Criteria andParamsInfoIdLike(String value) {
            addCriterion("params_info_id like", value, "paramsInfoId");
            return (Criteria) this;
        }

        public Criteria andParamsInfoIdNotLike(String value) {
            addCriterion("params_info_id not like", value, "paramsInfoId");
            return (Criteria) this;
        }

        public Criteria andParamsInfoIdIn(List<String> values) {
            addCriterion("params_info_id in", values, "paramsInfoId");
            return (Criteria) this;
        }

        public Criteria andParamsInfoIdNotIn(List<String> values) {
            addCriterion("params_info_id not in", values, "paramsInfoId");
            return (Criteria) this;
        }

        public Criteria andParamsInfoIdBetween(String value1, String value2) {
            addCriterion("params_info_id between", value1, value2, "paramsInfoId");
            return (Criteria) this;
        }

        public Criteria andParamsInfoIdNotBetween(String value1, String value2) {
            addCriterion("params_info_id not between", value1, value2, "paramsInfoId");
            return (Criteria) this;
        }

        public Criteria andTargetViewIdIsNull() {
            addCriterion("target_view_id is null");
            return (Criteria) this;
        }

        public Criteria andTargetViewIdIsNotNull() {
            addCriterion("target_view_id is not null");
            return (Criteria) this;
        }

        public Criteria andTargetViewIdEqualTo(String value) {
            addCriterion("target_view_id =", value, "targetViewId");
            return (Criteria) this;
        }

        public Criteria andTargetViewIdNotEqualTo(String value) {
            addCriterion("target_view_id <>", value, "targetViewId");
            return (Criteria) this;
        }

        public Criteria andTargetViewIdGreaterThan(String value) {
            addCriterion("target_view_id >", value, "targetViewId");
            return (Criteria) this;
        }

        public Criteria andTargetViewIdGreaterThanOrEqualTo(String value) {
            addCriterion("target_view_id >=", value, "targetViewId");
            return (Criteria) this;
        }

        public Criteria andTargetViewIdLessThan(String value) {
            addCriterion("target_view_id <", value, "targetViewId");
            return (Criteria) this;
        }

        public Criteria andTargetViewIdLessThanOrEqualTo(String value) {
            addCriterion("target_view_id <=", value, "targetViewId");
            return (Criteria) this;
        }

        public Criteria andTargetViewIdLike(String value) {
            addCriterion("target_view_id like", value, "targetViewId");
            return (Criteria) this;
        }

        public Criteria andTargetViewIdNotLike(String value) {
            addCriterion("target_view_id not like", value, "targetViewId");
            return (Criteria) this;
        }

        public Criteria andTargetViewIdIn(List<String> values) {
            addCriterion("target_view_id in", values, "targetViewId");
            return (Criteria) this;
        }

        public Criteria andTargetViewIdNotIn(List<String> values) {
            addCriterion("target_view_id not in", values, "targetViewId");
            return (Criteria) this;
        }

        public Criteria andTargetViewIdBetween(String value1, String value2) {
            addCriterion("target_view_id between", value1, value2, "targetViewId");
            return (Criteria) this;
        }

        public Criteria andTargetViewIdNotBetween(String value1, String value2) {
            addCriterion("target_view_id not between", value1, value2, "targetViewId");
            return (Criteria) this;
        }

        public Criteria andTargetFieldIdIsNull() {
            addCriterion("target_field_id is null");
            return (Criteria) this;
        }

        public Criteria andTargetFieldIdIsNotNull() {
            addCriterion("target_field_id is not null");
            return (Criteria) this;
        }

        public Criteria andTargetFieldIdEqualTo(String value) {
            addCriterion("target_field_id =", value, "targetFieldId");
            return (Criteria) this;
        }

        public Criteria andTargetFieldIdNotEqualTo(String value) {
            addCriterion("target_field_id <>", value, "targetFieldId");
            return (Criteria) this;
        }

        public Criteria andTargetFieldIdGreaterThan(String value) {
            addCriterion("target_field_id >", value, "targetFieldId");
            return (Criteria) this;
        }

        public Criteria andTargetFieldIdGreaterThanOrEqualTo(String value) {
            addCriterion("target_field_id >=", value, "targetFieldId");
            return (Criteria) this;
        }

        public Criteria andTargetFieldIdLessThan(String value) {
            addCriterion("target_field_id <", value, "targetFieldId");
            return (Criteria) this;
        }

        public Criteria andTargetFieldIdLessThanOrEqualTo(String value) {
            addCriterion("target_field_id <=", value, "targetFieldId");
            return (Criteria) this;
        }

        public Criteria andTargetFieldIdLike(String value) {
            addCriterion("target_field_id like", value, "targetFieldId");
            return (Criteria) this;
        }

        public Criteria andTargetFieldIdNotLike(String value) {
            addCriterion("target_field_id not like", value, "targetFieldId");
            return (Criteria) this;
        }

        public Criteria andTargetFieldIdIn(List<String> values) {
            addCriterion("target_field_id in", values, "targetFieldId");
            return (Criteria) this;
        }

        public Criteria andTargetFieldIdNotIn(List<String> values) {
            addCriterion("target_field_id not in", values, "targetFieldId");
            return (Criteria) this;
        }

        public Criteria andTargetFieldIdBetween(String value1, String value2) {
            addCriterion("target_field_id between", value1, value2, "targetFieldId");
            return (Criteria) this;
        }

        public Criteria andTargetFieldIdNotBetween(String value1, String value2) {
            addCriterion("target_field_id not between", value1, value2, "targetFieldId");
            return (Criteria) this;
        }

        public Criteria andCopyFromIsNull() {
            addCriterion("copy_from is null");
            return (Criteria) this;
        }

        public Criteria andCopyFromIsNotNull() {
            addCriterion("copy_from is not null");
            return (Criteria) this;
        }

        public Criteria andCopyFromEqualTo(String value) {
            addCriterion("copy_from =", value, "copyFrom");
            return (Criteria) this;
        }

        public Criteria andCopyFromNotEqualTo(String value) {
            addCriterion("copy_from <>", value, "copyFrom");
            return (Criteria) this;
        }

        public Criteria andCopyFromGreaterThan(String value) {
            addCriterion("copy_from >", value, "copyFrom");
            return (Criteria) this;
        }

        public Criteria andCopyFromGreaterThanOrEqualTo(String value) {
            addCriterion("copy_from >=", value, "copyFrom");
            return (Criteria) this;
        }

        public Criteria andCopyFromLessThan(String value) {
            addCriterion("copy_from <", value, "copyFrom");
            return (Criteria) this;
        }

        public Criteria andCopyFromLessThanOrEqualTo(String value) {
            addCriterion("copy_from <=", value, "copyFrom");
            return (Criteria) this;
        }

        public Criteria andCopyFromLike(String value) {
            addCriterion("copy_from like", value, "copyFrom");
            return (Criteria) this;
        }

        public Criteria andCopyFromNotLike(String value) {
            addCriterion("copy_from not like", value, "copyFrom");
            return (Criteria) this;
        }

        public Criteria andCopyFromIn(List<String> values) {
            addCriterion("copy_from in", values, "copyFrom");
            return (Criteria) this;
        }

        public Criteria andCopyFromNotIn(List<String> values) {
            addCriterion("copy_from not in", values, "copyFrom");
            return (Criteria) this;
        }

        public Criteria andCopyFromBetween(String value1, String value2) {
            addCriterion("copy_from between", value1, value2, "copyFrom");
            return (Criteria) this;
        }

        public Criteria andCopyFromNotBetween(String value1, String value2) {
            addCriterion("copy_from not between", value1, value2, "copyFrom");
            return (Criteria) this;
        }

        public Criteria andCopyIdIsNull() {
            addCriterion("copy_id is null");
            return (Criteria) this;
        }

        public Criteria andCopyIdIsNotNull() {
            addCriterion("copy_id is not null");
            return (Criteria) this;
        }

        public Criteria andCopyIdEqualTo(String value) {
            addCriterion("copy_id =", value, "copyId");
            return (Criteria) this;
        }

        public Criteria andCopyIdNotEqualTo(String value) {
            addCriterion("copy_id <>", value, "copyId");
            return (Criteria) this;
        }

        public Criteria andCopyIdGreaterThan(String value) {
            addCriterion("copy_id >", value, "copyId");
            return (Criteria) this;
        }

        public Criteria andCopyIdGreaterThanOrEqualTo(String value) {
            addCriterion("copy_id >=", value, "copyId");
            return (Criteria) this;
        }

        public Criteria andCopyIdLessThan(String value) {
            addCriterion("copy_id <", value, "copyId");
            return (Criteria) this;
        }

        public Criteria andCopyIdLessThanOrEqualTo(String value) {
            addCriterion("copy_id <=", value, "copyId");
            return (Criteria) this;
        }

        public Criteria andCopyIdLike(String value) {
            addCriterion("copy_id like", value, "copyId");
            return (Criteria) this;
        }

        public Criteria andCopyIdNotLike(String value) {
            addCriterion("copy_id not like", value, "copyId");
            return (Criteria) this;
        }

        public Criteria andCopyIdIn(List<String> values) {
            addCriterion("copy_id in", values, "copyId");
            return (Criteria) this;
        }

        public Criteria andCopyIdNotIn(List<String> values) {
            addCriterion("copy_id not in", values, "copyId");
            return (Criteria) this;
        }

        public Criteria andCopyIdBetween(String value1, String value2) {
            addCriterion("copy_id between", value1, value2, "copyId");
            return (Criteria) this;
        }

        public Criteria andCopyIdNotBetween(String value1, String value2) {
            addCriterion("copy_id not between", value1, value2, "copyId");
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