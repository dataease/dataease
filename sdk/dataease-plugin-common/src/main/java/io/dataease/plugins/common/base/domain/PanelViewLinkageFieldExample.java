package io.dataease.plugins.common.base.domain;

import java.util.ArrayList;
import java.util.List;

public class PanelViewLinkageFieldExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public PanelViewLinkageFieldExample() {
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

        public Criteria andLinkageIdIsNull() {
            addCriterion("linkage_id is null");
            return (Criteria) this;
        }

        public Criteria andLinkageIdIsNotNull() {
            addCriterion("linkage_id is not null");
            return (Criteria) this;
        }

        public Criteria andLinkageIdEqualTo(String value) {
            addCriterion("linkage_id =", value, "linkageId");
            return (Criteria) this;
        }

        public Criteria andLinkageIdNotEqualTo(String value) {
            addCriterion("linkage_id <>", value, "linkageId");
            return (Criteria) this;
        }

        public Criteria andLinkageIdGreaterThan(String value) {
            addCriterion("linkage_id >", value, "linkageId");
            return (Criteria) this;
        }

        public Criteria andLinkageIdGreaterThanOrEqualTo(String value) {
            addCriterion("linkage_id >=", value, "linkageId");
            return (Criteria) this;
        }

        public Criteria andLinkageIdLessThan(String value) {
            addCriterion("linkage_id <", value, "linkageId");
            return (Criteria) this;
        }

        public Criteria andLinkageIdLessThanOrEqualTo(String value) {
            addCriterion("linkage_id <=", value, "linkageId");
            return (Criteria) this;
        }

        public Criteria andLinkageIdLike(String value) {
            addCriterion("linkage_id like", value, "linkageId");
            return (Criteria) this;
        }

        public Criteria andLinkageIdNotLike(String value) {
            addCriterion("linkage_id not like", value, "linkageId");
            return (Criteria) this;
        }

        public Criteria andLinkageIdIn(List<String> values) {
            addCriterion("linkage_id in", values, "linkageId");
            return (Criteria) this;
        }

        public Criteria andLinkageIdNotIn(List<String> values) {
            addCriterion("linkage_id not in", values, "linkageId");
            return (Criteria) this;
        }

        public Criteria andLinkageIdBetween(String value1, String value2) {
            addCriterion("linkage_id between", value1, value2, "linkageId");
            return (Criteria) this;
        }

        public Criteria andLinkageIdNotBetween(String value1, String value2) {
            addCriterion("linkage_id not between", value1, value2, "linkageId");
            return (Criteria) this;
        }

        public Criteria andSourceFieldIsNull() {
            addCriterion("source_field is null");
            return (Criteria) this;
        }

        public Criteria andSourceFieldIsNotNull() {
            addCriterion("source_field is not null");
            return (Criteria) this;
        }

        public Criteria andSourceFieldEqualTo(String value) {
            addCriterion("source_field =", value, "sourceField");
            return (Criteria) this;
        }

        public Criteria andSourceFieldNotEqualTo(String value) {
            addCriterion("source_field <>", value, "sourceField");
            return (Criteria) this;
        }

        public Criteria andSourceFieldGreaterThan(String value) {
            addCriterion("source_field >", value, "sourceField");
            return (Criteria) this;
        }

        public Criteria andSourceFieldGreaterThanOrEqualTo(String value) {
            addCriterion("source_field >=", value, "sourceField");
            return (Criteria) this;
        }

        public Criteria andSourceFieldLessThan(String value) {
            addCriterion("source_field <", value, "sourceField");
            return (Criteria) this;
        }

        public Criteria andSourceFieldLessThanOrEqualTo(String value) {
            addCriterion("source_field <=", value, "sourceField");
            return (Criteria) this;
        }

        public Criteria andSourceFieldLike(String value) {
            addCriterion("source_field like", value, "sourceField");
            return (Criteria) this;
        }

        public Criteria andSourceFieldNotLike(String value) {
            addCriterion("source_field not like", value, "sourceField");
            return (Criteria) this;
        }

        public Criteria andSourceFieldIn(List<String> values) {
            addCriterion("source_field in", values, "sourceField");
            return (Criteria) this;
        }

        public Criteria andSourceFieldNotIn(List<String> values) {
            addCriterion("source_field not in", values, "sourceField");
            return (Criteria) this;
        }

        public Criteria andSourceFieldBetween(String value1, String value2) {
            addCriterion("source_field between", value1, value2, "sourceField");
            return (Criteria) this;
        }

        public Criteria andSourceFieldNotBetween(String value1, String value2) {
            addCriterion("source_field not between", value1, value2, "sourceField");
            return (Criteria) this;
        }

        public Criteria andTargetFieldIsNull() {
            addCriterion("target_field is null");
            return (Criteria) this;
        }

        public Criteria andTargetFieldIsNotNull() {
            addCriterion("target_field is not null");
            return (Criteria) this;
        }

        public Criteria andTargetFieldEqualTo(String value) {
            addCriterion("target_field =", value, "targetField");
            return (Criteria) this;
        }

        public Criteria andTargetFieldNotEqualTo(String value) {
            addCriterion("target_field <>", value, "targetField");
            return (Criteria) this;
        }

        public Criteria andTargetFieldGreaterThan(String value) {
            addCriterion("target_field >", value, "targetField");
            return (Criteria) this;
        }

        public Criteria andTargetFieldGreaterThanOrEqualTo(String value) {
            addCriterion("target_field >=", value, "targetField");
            return (Criteria) this;
        }

        public Criteria andTargetFieldLessThan(String value) {
            addCriterion("target_field <", value, "targetField");
            return (Criteria) this;
        }

        public Criteria andTargetFieldLessThanOrEqualTo(String value) {
            addCriterion("target_field <=", value, "targetField");
            return (Criteria) this;
        }

        public Criteria andTargetFieldLike(String value) {
            addCriterion("target_field like", value, "targetField");
            return (Criteria) this;
        }

        public Criteria andTargetFieldNotLike(String value) {
            addCriterion("target_field not like", value, "targetField");
            return (Criteria) this;
        }

        public Criteria andTargetFieldIn(List<String> values) {
            addCriterion("target_field in", values, "targetField");
            return (Criteria) this;
        }

        public Criteria andTargetFieldNotIn(List<String> values) {
            addCriterion("target_field not in", values, "targetField");
            return (Criteria) this;
        }

        public Criteria andTargetFieldBetween(String value1, String value2) {
            addCriterion("target_field between", value1, value2, "targetField");
            return (Criteria) this;
        }

        public Criteria andTargetFieldNotBetween(String value1, String value2) {
            addCriterion("target_field not between", value1, value2, "targetField");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIsNull() {
            addCriterion("update_time is null");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIsNotNull() {
            addCriterion("update_time is not null");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeEqualTo(Long value) {
            addCriterion("update_time =", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotEqualTo(Long value) {
            addCriterion("update_time <>", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeGreaterThan(Long value) {
            addCriterion("update_time >", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeGreaterThanOrEqualTo(Long value) {
            addCriterion("update_time >=", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeLessThan(Long value) {
            addCriterion("update_time <", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeLessThanOrEqualTo(Long value) {
            addCriterion("update_time <=", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIn(List<Long> values) {
            addCriterion("update_time in", values, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotIn(List<Long> values) {
            addCriterion("update_time not in", values, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeBetween(Long value1, Long value2) {
            addCriterion("update_time between", value1, value2, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotBetween(Long value1, Long value2) {
            addCriterion("update_time not between", value1, value2, "updateTime");
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