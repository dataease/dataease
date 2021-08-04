package io.dataease.base.domain;

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

        public Criteria andSourceFiledIsNull() {
            addCriterion("source_filed is null");
            return (Criteria) this;
        }

        public Criteria andSourceFiledIsNotNull() {
            addCriterion("source_filed is not null");
            return (Criteria) this;
        }

        public Criteria andSourceFiledEqualTo(String value) {
            addCriterion("source_filed =", value, "sourceFiled");
            return (Criteria) this;
        }

        public Criteria andSourceFiledNotEqualTo(String value) {
            addCriterion("source_filed <>", value, "sourceFiled");
            return (Criteria) this;
        }

        public Criteria andSourceFiledGreaterThan(String value) {
            addCriterion("source_filed >", value, "sourceFiled");
            return (Criteria) this;
        }

        public Criteria andSourceFiledGreaterThanOrEqualTo(String value) {
            addCriterion("source_filed >=", value, "sourceFiled");
            return (Criteria) this;
        }

        public Criteria andSourceFiledLessThan(String value) {
            addCriterion("source_filed <", value, "sourceFiled");
            return (Criteria) this;
        }

        public Criteria andSourceFiledLessThanOrEqualTo(String value) {
            addCriterion("source_filed <=", value, "sourceFiled");
            return (Criteria) this;
        }

        public Criteria andSourceFiledLike(String value) {
            addCriterion("source_filed like", value, "sourceFiled");
            return (Criteria) this;
        }

        public Criteria andSourceFiledNotLike(String value) {
            addCriterion("source_filed not like", value, "sourceFiled");
            return (Criteria) this;
        }

        public Criteria andSourceFiledIn(List<String> values) {
            addCriterion("source_filed in", values, "sourceFiled");
            return (Criteria) this;
        }

        public Criteria andSourceFiledNotIn(List<String> values) {
            addCriterion("source_filed not in", values, "sourceFiled");
            return (Criteria) this;
        }

        public Criteria andSourceFiledBetween(String value1, String value2) {
            addCriterion("source_filed between", value1, value2, "sourceFiled");
            return (Criteria) this;
        }

        public Criteria andSourceFiledNotBetween(String value1, String value2) {
            addCriterion("source_filed not between", value1, value2, "sourceFiled");
            return (Criteria) this;
        }

        public Criteria andTargetFiledIsNull() {
            addCriterion("target_filed is null");
            return (Criteria) this;
        }

        public Criteria andTargetFiledIsNotNull() {
            addCriterion("target_filed is not null");
            return (Criteria) this;
        }

        public Criteria andTargetFiledEqualTo(String value) {
            addCriterion("target_filed =", value, "targetFiled");
            return (Criteria) this;
        }

        public Criteria andTargetFiledNotEqualTo(String value) {
            addCriterion("target_filed <>", value, "targetFiled");
            return (Criteria) this;
        }

        public Criteria andTargetFiledGreaterThan(String value) {
            addCriterion("target_filed >", value, "targetFiled");
            return (Criteria) this;
        }

        public Criteria andTargetFiledGreaterThanOrEqualTo(String value) {
            addCriterion("target_filed >=", value, "targetFiled");
            return (Criteria) this;
        }

        public Criteria andTargetFiledLessThan(String value) {
            addCriterion("target_filed <", value, "targetFiled");
            return (Criteria) this;
        }

        public Criteria andTargetFiledLessThanOrEqualTo(String value) {
            addCriterion("target_filed <=", value, "targetFiled");
            return (Criteria) this;
        }

        public Criteria andTargetFiledLike(String value) {
            addCriterion("target_filed like", value, "targetFiled");
            return (Criteria) this;
        }

        public Criteria andTargetFiledNotLike(String value) {
            addCriterion("target_filed not like", value, "targetFiled");
            return (Criteria) this;
        }

        public Criteria andTargetFiledIn(List<String> values) {
            addCriterion("target_filed in", values, "targetFiled");
            return (Criteria) this;
        }

        public Criteria andTargetFiledNotIn(List<String> values) {
            addCriterion("target_filed not in", values, "targetFiled");
            return (Criteria) this;
        }

        public Criteria andTargetFiledBetween(String value1, String value2) {
            addCriterion("target_filed between", value1, value2, "targetFiled");
            return (Criteria) this;
        }

        public Criteria andTargetFiledNotBetween(String value1, String value2) {
            addCriterion("target_filed not between", value1, value2, "targetFiled");
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