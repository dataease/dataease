package io.dataease.plugins.common.base.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DataeaseCodeVersionExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public DataeaseCodeVersionExample() {
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

        public Criteria andInstalledRankIsNull() {
            addCriterion("installed_rank is null");
            return (Criteria) this;
        }

        public Criteria andInstalledRankIsNotNull() {
            addCriterion("installed_rank is not null");
            return (Criteria) this;
        }

        public Criteria andInstalledRankEqualTo(Integer value) {
            addCriterion("installed_rank =", value, "installedRank");
            return (Criteria) this;
        }

        public Criteria andInstalledRankNotEqualTo(Integer value) {
            addCriterion("installed_rank <>", value, "installedRank");
            return (Criteria) this;
        }

        public Criteria andInstalledRankGreaterThan(Integer value) {
            addCriterion("installed_rank >", value, "installedRank");
            return (Criteria) this;
        }

        public Criteria andInstalledRankGreaterThanOrEqualTo(Integer value) {
            addCriterion("installed_rank >=", value, "installedRank");
            return (Criteria) this;
        }

        public Criteria andInstalledRankLessThan(Integer value) {
            addCriterion("installed_rank <", value, "installedRank");
            return (Criteria) this;
        }

        public Criteria andInstalledRankLessThanOrEqualTo(Integer value) {
            addCriterion("installed_rank <=", value, "installedRank");
            return (Criteria) this;
        }

        public Criteria andInstalledRankIn(List<Integer> values) {
            addCriterion("installed_rank in", values, "installedRank");
            return (Criteria) this;
        }

        public Criteria andInstalledRankNotIn(List<Integer> values) {
            addCriterion("installed_rank not in", values, "installedRank");
            return (Criteria) this;
        }

        public Criteria andInstalledRankBetween(Integer value1, Integer value2) {
            addCriterion("installed_rank between", value1, value2, "installedRank");
            return (Criteria) this;
        }

        public Criteria andInstalledRankNotBetween(Integer value1, Integer value2) {
            addCriterion("installed_rank not between", value1, value2, "installedRank");
            return (Criteria) this;
        }

        public Criteria andDescriptionIsNull() {
            addCriterion("description is null");
            return (Criteria) this;
        }

        public Criteria andDescriptionIsNotNull() {
            addCriterion("description is not null");
            return (Criteria) this;
        }

        public Criteria andDescriptionEqualTo(String value) {
            addCriterion("description =", value, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionNotEqualTo(String value) {
            addCriterion("description <>", value, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionGreaterThan(String value) {
            addCriterion("description >", value, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionGreaterThanOrEqualTo(String value) {
            addCriterion("description >=", value, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionLessThan(String value) {
            addCriterion("description <", value, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionLessThanOrEqualTo(String value) {
            addCriterion("description <=", value, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionLike(String value) {
            addCriterion("description like", value, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionNotLike(String value) {
            addCriterion("description not like", value, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionIn(List<String> values) {
            addCriterion("description in", values, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionNotIn(List<String> values) {
            addCriterion("description not in", values, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionBetween(String value1, String value2) {
            addCriterion("description between", value1, value2, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionNotBetween(String value1, String value2) {
            addCriterion("description not between", value1, value2, "description");
            return (Criteria) this;
        }

        public Criteria andInstalledOnIsNull() {
            addCriterion("installed_on is null");
            return (Criteria) this;
        }

        public Criteria andInstalledOnIsNotNull() {
            addCriterion("installed_on is not null");
            return (Criteria) this;
        }

        public Criteria andInstalledOnEqualTo(Date value) {
            addCriterion("installed_on =", value, "installedOn");
            return (Criteria) this;
        }

        public Criteria andInstalledOnNotEqualTo(Date value) {
            addCriterion("installed_on <>", value, "installedOn");
            return (Criteria) this;
        }

        public Criteria andInstalledOnGreaterThan(Date value) {
            addCriterion("installed_on >", value, "installedOn");
            return (Criteria) this;
        }

        public Criteria andInstalledOnGreaterThanOrEqualTo(Date value) {
            addCriterion("installed_on >=", value, "installedOn");
            return (Criteria) this;
        }

        public Criteria andInstalledOnLessThan(Date value) {
            addCriterion("installed_on <", value, "installedOn");
            return (Criteria) this;
        }

        public Criteria andInstalledOnLessThanOrEqualTo(Date value) {
            addCriterion("installed_on <=", value, "installedOn");
            return (Criteria) this;
        }

        public Criteria andInstalledOnIn(List<Date> values) {
            addCriterion("installed_on in", values, "installedOn");
            return (Criteria) this;
        }

        public Criteria andInstalledOnNotIn(List<Date> values) {
            addCriterion("installed_on not in", values, "installedOn");
            return (Criteria) this;
        }

        public Criteria andInstalledOnBetween(Date value1, Date value2) {
            addCriterion("installed_on between", value1, value2, "installedOn");
            return (Criteria) this;
        }

        public Criteria andInstalledOnNotBetween(Date value1, Date value2) {
            addCriterion("installed_on not between", value1, value2, "installedOn");
            return (Criteria) this;
        }

        public Criteria andSuccessIsNull() {
            addCriterion("success is null");
            return (Criteria) this;
        }

        public Criteria andSuccessIsNotNull() {
            addCriterion("success is not null");
            return (Criteria) this;
        }

        public Criteria andSuccessEqualTo(Boolean value) {
            addCriterion("success =", value, "success");
            return (Criteria) this;
        }

        public Criteria andSuccessNotEqualTo(Boolean value) {
            addCriterion("success <>", value, "success");
            return (Criteria) this;
        }

        public Criteria andSuccessGreaterThan(Boolean value) {
            addCriterion("success >", value, "success");
            return (Criteria) this;
        }

        public Criteria andSuccessGreaterThanOrEqualTo(Boolean value) {
            addCriterion("success >=", value, "success");
            return (Criteria) this;
        }

        public Criteria andSuccessLessThan(Boolean value) {
            addCriterion("success <", value, "success");
            return (Criteria) this;
        }

        public Criteria andSuccessLessThanOrEqualTo(Boolean value) {
            addCriterion("success <=", value, "success");
            return (Criteria) this;
        }

        public Criteria andSuccessIn(List<Boolean> values) {
            addCriterion("success in", values, "success");
            return (Criteria) this;
        }

        public Criteria andSuccessNotIn(List<Boolean> values) {
            addCriterion("success not in", values, "success");
            return (Criteria) this;
        }

        public Criteria andSuccessBetween(Boolean value1, Boolean value2) {
            addCriterion("success between", value1, value2, "success");
            return (Criteria) this;
        }

        public Criteria andSuccessNotBetween(Boolean value1, Boolean value2) {
            addCriterion("success not between", value1, value2, "success");
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