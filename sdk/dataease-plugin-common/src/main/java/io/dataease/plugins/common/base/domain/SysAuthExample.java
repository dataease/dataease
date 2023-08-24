package io.dataease.plugins.common.base.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SysAuthExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public SysAuthExample() {
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

        public Criteria andAuthSourceIsNull() {
            addCriterion("auth_source is null");
            return (Criteria) this;
        }

        public Criteria andAuthSourceIsNotNull() {
            addCriterion("auth_source is not null");
            return (Criteria) this;
        }

        public Criteria andAuthSourceEqualTo(String value) {
            addCriterion("auth_source =", value, "authSource");
            return (Criteria) this;
        }

        public Criteria andAuthSourceNotEqualTo(String value) {
            addCriterion("auth_source <>", value, "authSource");
            return (Criteria) this;
        }

        public Criteria andAuthSourceGreaterThan(String value) {
            addCriterion("auth_source >", value, "authSource");
            return (Criteria) this;
        }

        public Criteria andAuthSourceGreaterThanOrEqualTo(String value) {
            addCriterion("auth_source >=", value, "authSource");
            return (Criteria) this;
        }

        public Criteria andAuthSourceLessThan(String value) {
            addCriterion("auth_source <", value, "authSource");
            return (Criteria) this;
        }

        public Criteria andAuthSourceLessThanOrEqualTo(String value) {
            addCriterion("auth_source <=", value, "authSource");
            return (Criteria) this;
        }

        public Criteria andAuthSourceLike(String value) {
            addCriterion("auth_source like", value, "authSource");
            return (Criteria) this;
        }

        public Criteria andAuthSourceNotLike(String value) {
            addCriterion("auth_source not like", value, "authSource");
            return (Criteria) this;
        }

        public Criteria andAuthSourceIn(List<String> values) {
            addCriterion("auth_source in", values, "authSource");
            return (Criteria) this;
        }

        public Criteria andAuthSourceNotIn(List<String> values) {
            addCriterion("auth_source not in", values, "authSource");
            return (Criteria) this;
        }

        public Criteria andAuthSourceBetween(String value1, String value2) {
            addCriterion("auth_source between", value1, value2, "authSource");
            return (Criteria) this;
        }

        public Criteria andAuthSourceNotBetween(String value1, String value2) {
            addCriterion("auth_source not between", value1, value2, "authSource");
            return (Criteria) this;
        }

        public Criteria andAuthSourceTypeIsNull() {
            addCriterion("auth_source_type is null");
            return (Criteria) this;
        }

        public Criteria andAuthSourceTypeIsNotNull() {
            addCriterion("auth_source_type is not null");
            return (Criteria) this;
        }

        public Criteria andAuthSourceTypeEqualTo(String value) {
            addCriterion("auth_source_type =", value, "authSourceType");
            return (Criteria) this;
        }

        public Criteria andAuthSourceTypeNotEqualTo(String value) {
            addCriterion("auth_source_type <>", value, "authSourceType");
            return (Criteria) this;
        }

        public Criteria andAuthSourceTypeGreaterThan(String value) {
            addCriterion("auth_source_type >", value, "authSourceType");
            return (Criteria) this;
        }

        public Criteria andAuthSourceTypeGreaterThanOrEqualTo(String value) {
            addCriterion("auth_source_type >=", value, "authSourceType");
            return (Criteria) this;
        }

        public Criteria andAuthSourceTypeLessThan(String value) {
            addCriterion("auth_source_type <", value, "authSourceType");
            return (Criteria) this;
        }

        public Criteria andAuthSourceTypeLessThanOrEqualTo(String value) {
            addCriterion("auth_source_type <=", value, "authSourceType");
            return (Criteria) this;
        }

        public Criteria andAuthSourceTypeLike(String value) {
            addCriterion("auth_source_type like", value, "authSourceType");
            return (Criteria) this;
        }

        public Criteria andAuthSourceTypeNotLike(String value) {
            addCriterion("auth_source_type not like", value, "authSourceType");
            return (Criteria) this;
        }

        public Criteria andAuthSourceTypeIn(List<String> values) {
            addCriterion("auth_source_type in", values, "authSourceType");
            return (Criteria) this;
        }

        public Criteria andAuthSourceTypeNotIn(List<String> values) {
            addCriterion("auth_source_type not in", values, "authSourceType");
            return (Criteria) this;
        }

        public Criteria andAuthSourceTypeBetween(String value1, String value2) {
            addCriterion("auth_source_type between", value1, value2, "authSourceType");
            return (Criteria) this;
        }

        public Criteria andAuthSourceTypeNotBetween(String value1, String value2) {
            addCriterion("auth_source_type not between", value1, value2, "authSourceType");
            return (Criteria) this;
        }

        public Criteria andAuthTargetIsNull() {
            addCriterion("auth_target is null");
            return (Criteria) this;
        }

        public Criteria andAuthTargetIsNotNull() {
            addCriterion("auth_target is not null");
            return (Criteria) this;
        }

        public Criteria andAuthTargetEqualTo(String value) {
            addCriterion("auth_target =", value, "authTarget");
            return (Criteria) this;
        }

        public Criteria andAuthTargetNotEqualTo(String value) {
            addCriterion("auth_target <>", value, "authTarget");
            return (Criteria) this;
        }

        public Criteria andAuthTargetGreaterThan(String value) {
            addCriterion("auth_target >", value, "authTarget");
            return (Criteria) this;
        }

        public Criteria andAuthTargetGreaterThanOrEqualTo(String value) {
            addCriterion("auth_target >=", value, "authTarget");
            return (Criteria) this;
        }

        public Criteria andAuthTargetLessThan(String value) {
            addCriterion("auth_target <", value, "authTarget");
            return (Criteria) this;
        }

        public Criteria andAuthTargetLessThanOrEqualTo(String value) {
            addCriterion("auth_target <=", value, "authTarget");
            return (Criteria) this;
        }

        public Criteria andAuthTargetLike(String value) {
            addCriterion("auth_target like", value, "authTarget");
            return (Criteria) this;
        }

        public Criteria andAuthTargetNotLike(String value) {
            addCriterion("auth_target not like", value, "authTarget");
            return (Criteria) this;
        }

        public Criteria andAuthTargetIn(List<String> values) {
            addCriterion("auth_target in", values, "authTarget");
            return (Criteria) this;
        }

        public Criteria andAuthTargetNotIn(List<String> values) {
            addCriterion("auth_target not in", values, "authTarget");
            return (Criteria) this;
        }

        public Criteria andAuthTargetBetween(String value1, String value2) {
            addCriterion("auth_target between", value1, value2, "authTarget");
            return (Criteria) this;
        }

        public Criteria andAuthTargetNotBetween(String value1, String value2) {
            addCriterion("auth_target not between", value1, value2, "authTarget");
            return (Criteria) this;
        }

        public Criteria andAuthTargetTypeIsNull() {
            addCriterion("auth_target_type is null");
            return (Criteria) this;
        }

        public Criteria andAuthTargetTypeIsNotNull() {
            addCriterion("auth_target_type is not null");
            return (Criteria) this;
        }

        public Criteria andAuthTargetTypeEqualTo(String value) {
            addCriterion("auth_target_type =", value, "authTargetType");
            return (Criteria) this;
        }

        public Criteria andAuthTargetTypeNotEqualTo(String value) {
            addCriterion("auth_target_type <>", value, "authTargetType");
            return (Criteria) this;
        }

        public Criteria andAuthTargetTypeGreaterThan(String value) {
            addCriterion("auth_target_type >", value, "authTargetType");
            return (Criteria) this;
        }

        public Criteria andAuthTargetTypeGreaterThanOrEqualTo(String value) {
            addCriterion("auth_target_type >=", value, "authTargetType");
            return (Criteria) this;
        }

        public Criteria andAuthTargetTypeLessThan(String value) {
            addCriterion("auth_target_type <", value, "authTargetType");
            return (Criteria) this;
        }

        public Criteria andAuthTargetTypeLessThanOrEqualTo(String value) {
            addCriterion("auth_target_type <=", value, "authTargetType");
            return (Criteria) this;
        }

        public Criteria andAuthTargetTypeLike(String value) {
            addCriterion("auth_target_type like", value, "authTargetType");
            return (Criteria) this;
        }

        public Criteria andAuthTargetTypeNotLike(String value) {
            addCriterion("auth_target_type not like", value, "authTargetType");
            return (Criteria) this;
        }

        public Criteria andAuthTargetTypeIn(List<String> values) {
            addCriterion("auth_target_type in", values, "authTargetType");
            return (Criteria) this;
        }

        public Criteria andAuthTargetTypeNotIn(List<String> values) {
            addCriterion("auth_target_type not in", values, "authTargetType");
            return (Criteria) this;
        }

        public Criteria andAuthTargetTypeBetween(String value1, String value2) {
            addCriterion("auth_target_type between", value1, value2, "authTargetType");
            return (Criteria) this;
        }

        public Criteria andAuthTargetTypeNotBetween(String value1, String value2) {
            addCriterion("auth_target_type not between", value1, value2, "authTargetType");
            return (Criteria) this;
        }

        public Criteria andAuthTimeIsNull() {
            addCriterion("auth_time is null");
            return (Criteria) this;
        }

        public Criteria andAuthTimeIsNotNull() {
            addCriterion("auth_time is not null");
            return (Criteria) this;
        }

        public Criteria andAuthTimeEqualTo(Long value) {
            addCriterion("auth_time =", value, "authTime");
            return (Criteria) this;
        }

        public Criteria andAuthTimeNotEqualTo(Long value) {
            addCriterion("auth_time <>", value, "authTime");
            return (Criteria) this;
        }

        public Criteria andAuthTimeGreaterThan(Long value) {
            addCriterion("auth_time >", value, "authTime");
            return (Criteria) this;
        }

        public Criteria andAuthTimeGreaterThanOrEqualTo(Long value) {
            addCriterion("auth_time >=", value, "authTime");
            return (Criteria) this;
        }

        public Criteria andAuthTimeLessThan(Long value) {
            addCriterion("auth_time <", value, "authTime");
            return (Criteria) this;
        }

        public Criteria andAuthTimeLessThanOrEqualTo(Long value) {
            addCriterion("auth_time <=", value, "authTime");
            return (Criteria) this;
        }

        public Criteria andAuthTimeIn(List<Long> values) {
            addCriterion("auth_time in", values, "authTime");
            return (Criteria) this;
        }

        public Criteria andAuthTimeNotIn(List<Long> values) {
            addCriterion("auth_time not in", values, "authTime");
            return (Criteria) this;
        }

        public Criteria andAuthTimeBetween(Long value1, Long value2) {
            addCriterion("auth_time between", value1, value2, "authTime");
            return (Criteria) this;
        }

        public Criteria andAuthTimeNotBetween(Long value1, Long value2) {
            addCriterion("auth_time not between", value1, value2, "authTime");
            return (Criteria) this;
        }

        public Criteria andAuthDetailsIsNull() {
            addCriterion("auth_details is null");
            return (Criteria) this;
        }

        public Criteria andAuthDetailsIsNotNull() {
            addCriterion("auth_details is not null");
            return (Criteria) this;
        }

        public Criteria andAuthDetailsEqualTo(String value) {
            addCriterion("auth_details =", value, "authDetails");
            return (Criteria) this;
        }

        public Criteria andAuthDetailsNotEqualTo(String value) {
            addCriterion("auth_details <>", value, "authDetails");
            return (Criteria) this;
        }

        public Criteria andAuthDetailsGreaterThan(String value) {
            addCriterion("auth_details >", value, "authDetails");
            return (Criteria) this;
        }

        public Criteria andAuthDetailsGreaterThanOrEqualTo(String value) {
            addCriterion("auth_details >=", value, "authDetails");
            return (Criteria) this;
        }

        public Criteria andAuthDetailsLessThan(String value) {
            addCriterion("auth_details <", value, "authDetails");
            return (Criteria) this;
        }

        public Criteria andAuthDetailsLessThanOrEqualTo(String value) {
            addCriterion("auth_details <=", value, "authDetails");
            return (Criteria) this;
        }

        public Criteria andAuthDetailsLike(String value) {
            addCriterion("auth_details like", value, "authDetails");
            return (Criteria) this;
        }

        public Criteria andAuthDetailsNotLike(String value) {
            addCriterion("auth_details not like", value, "authDetails");
            return (Criteria) this;
        }

        public Criteria andAuthDetailsIn(List<String> values) {
            addCriterion("auth_details in", values, "authDetails");
            return (Criteria) this;
        }

        public Criteria andAuthDetailsNotIn(List<String> values) {
            addCriterion("auth_details not in", values, "authDetails");
            return (Criteria) this;
        }

        public Criteria andAuthDetailsBetween(String value1, String value2) {
            addCriterion("auth_details between", value1, value2, "authDetails");
            return (Criteria) this;
        }

        public Criteria andAuthDetailsNotBetween(String value1, String value2) {
            addCriterion("auth_details not between", value1, value2, "authDetails");
            return (Criteria) this;
        }

        public Criteria andAuthUserIsNull() {
            addCriterion("auth_user is null");
            return (Criteria) this;
        }

        public Criteria andAuthUserIsNotNull() {
            addCriterion("auth_user is not null");
            return (Criteria) this;
        }

        public Criteria andAuthUserEqualTo(String value) {
            addCriterion("auth_user =", value, "authUser");
            return (Criteria) this;
        }

        public Criteria andAuthUserNotEqualTo(String value) {
            addCriterion("auth_user <>", value, "authUser");
            return (Criteria) this;
        }

        public Criteria andAuthUserGreaterThan(String value) {
            addCriterion("auth_user >", value, "authUser");
            return (Criteria) this;
        }

        public Criteria andAuthUserGreaterThanOrEqualTo(String value) {
            addCriterion("auth_user >=", value, "authUser");
            return (Criteria) this;
        }

        public Criteria andAuthUserLessThan(String value) {
            addCriterion("auth_user <", value, "authUser");
            return (Criteria) this;
        }

        public Criteria andAuthUserLessThanOrEqualTo(String value) {
            addCriterion("auth_user <=", value, "authUser");
            return (Criteria) this;
        }

        public Criteria andAuthUserLike(String value) {
            addCriterion("auth_user like", value, "authUser");
            return (Criteria) this;
        }

        public Criteria andAuthUserNotLike(String value) {
            addCriterion("auth_user not like", value, "authUser");
            return (Criteria) this;
        }

        public Criteria andAuthUserIn(List<String> values) {
            addCriterion("auth_user in", values, "authUser");
            return (Criteria) this;
        }

        public Criteria andAuthUserNotIn(List<String> values) {
            addCriterion("auth_user not in", values, "authUser");
            return (Criteria) this;
        }

        public Criteria andAuthUserBetween(String value1, String value2) {
            addCriterion("auth_user between", value1, value2, "authUser");
            return (Criteria) this;
        }

        public Criteria andAuthUserNotBetween(String value1, String value2) {
            addCriterion("auth_user not between", value1, value2, "authUser");
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

        public Criteria andUpdateTimeEqualTo(Date value) {
            addCriterion("update_time =", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotEqualTo(Date value) {
            addCriterion("update_time <>", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeGreaterThan(Date value) {
            addCriterion("update_time >", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("update_time >=", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeLessThan(Date value) {
            addCriterion("update_time <", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeLessThanOrEqualTo(Date value) {
            addCriterion("update_time <=", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIn(List<Date> values) {
            addCriterion("update_time in", values, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotIn(List<Date> values) {
            addCriterion("update_time not in", values, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeBetween(Date value1, Date value2) {
            addCriterion("update_time between", value1, value2, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotBetween(Date value1, Date value2) {
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