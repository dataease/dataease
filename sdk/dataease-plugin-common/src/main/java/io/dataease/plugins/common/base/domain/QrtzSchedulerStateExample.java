package io.dataease.plugins.common.base.domain;

import java.util.ArrayList;
import java.util.List;

public class QrtzSchedulerStateExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public QrtzSchedulerStateExample() {
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

        public Criteria andSchedNameIsNull() {
            addCriterion("SCHED_NAME is null");
            return (Criteria) this;
        }

        public Criteria andSchedNameIsNotNull() {
            addCriterion("SCHED_NAME is not null");
            return (Criteria) this;
        }

        public Criteria andSchedNameEqualTo(String value) {
            addCriterion("SCHED_NAME =", value, "schedName");
            return (Criteria) this;
        }

        public Criteria andSchedNameNotEqualTo(String value) {
            addCriterion("SCHED_NAME <>", value, "schedName");
            return (Criteria) this;
        }

        public Criteria andSchedNameGreaterThan(String value) {
            addCriterion("SCHED_NAME >", value, "schedName");
            return (Criteria) this;
        }

        public Criteria andSchedNameGreaterThanOrEqualTo(String value) {
            addCriterion("SCHED_NAME >=", value, "schedName");
            return (Criteria) this;
        }

        public Criteria andSchedNameLessThan(String value) {
            addCriterion("SCHED_NAME <", value, "schedName");
            return (Criteria) this;
        }

        public Criteria andSchedNameLessThanOrEqualTo(String value) {
            addCriterion("SCHED_NAME <=", value, "schedName");
            return (Criteria) this;
        }

        public Criteria andSchedNameLike(String value) {
            addCriterion("SCHED_NAME like", value, "schedName");
            return (Criteria) this;
        }

        public Criteria andSchedNameNotLike(String value) {
            addCriterion("SCHED_NAME not like", value, "schedName");
            return (Criteria) this;
        }

        public Criteria andSchedNameIn(List<String> values) {
            addCriterion("SCHED_NAME in", values, "schedName");
            return (Criteria) this;
        }

        public Criteria andSchedNameNotIn(List<String> values) {
            addCriterion("SCHED_NAME not in", values, "schedName");
            return (Criteria) this;
        }

        public Criteria andSchedNameBetween(String value1, String value2) {
            addCriterion("SCHED_NAME between", value1, value2, "schedName");
            return (Criteria) this;
        }

        public Criteria andSchedNameNotBetween(String value1, String value2) {
            addCriterion("SCHED_NAME not between", value1, value2, "schedName");
            return (Criteria) this;
        }

        public Criteria andInstanceNameIsNull() {
            addCriterion("INSTANCE_NAME is null");
            return (Criteria) this;
        }

        public Criteria andInstanceNameIsNotNull() {
            addCriterion("INSTANCE_NAME is not null");
            return (Criteria) this;
        }

        public Criteria andInstanceNameEqualTo(String value) {
            addCriterion("INSTANCE_NAME =", value, "instanceName");
            return (Criteria) this;
        }

        public Criteria andInstanceNameNotEqualTo(String value) {
            addCriterion("INSTANCE_NAME <>", value, "instanceName");
            return (Criteria) this;
        }

        public Criteria andInstanceNameGreaterThan(String value) {
            addCriterion("INSTANCE_NAME >", value, "instanceName");
            return (Criteria) this;
        }

        public Criteria andInstanceNameGreaterThanOrEqualTo(String value) {
            addCriterion("INSTANCE_NAME >=", value, "instanceName");
            return (Criteria) this;
        }

        public Criteria andInstanceNameLessThan(String value) {
            addCriterion("INSTANCE_NAME <", value, "instanceName");
            return (Criteria) this;
        }

        public Criteria andInstanceNameLessThanOrEqualTo(String value) {
            addCriterion("INSTANCE_NAME <=", value, "instanceName");
            return (Criteria) this;
        }

        public Criteria andInstanceNameLike(String value) {
            addCriterion("INSTANCE_NAME like", value, "instanceName");
            return (Criteria) this;
        }

        public Criteria andInstanceNameNotLike(String value) {
            addCriterion("INSTANCE_NAME not like", value, "instanceName");
            return (Criteria) this;
        }

        public Criteria andInstanceNameIn(List<String> values) {
            addCriterion("INSTANCE_NAME in", values, "instanceName");
            return (Criteria) this;
        }

        public Criteria andInstanceNameNotIn(List<String> values) {
            addCriterion("INSTANCE_NAME not in", values, "instanceName");
            return (Criteria) this;
        }

        public Criteria andInstanceNameBetween(String value1, String value2) {
            addCriterion("INSTANCE_NAME between", value1, value2, "instanceName");
            return (Criteria) this;
        }

        public Criteria andInstanceNameNotBetween(String value1, String value2) {
            addCriterion("INSTANCE_NAME not between", value1, value2, "instanceName");
            return (Criteria) this;
        }

        public Criteria andLastCheckinTimeIsNull() {
            addCriterion("LAST_CHECKIN_TIME is null");
            return (Criteria) this;
        }

        public Criteria andLastCheckinTimeIsNotNull() {
            addCriterion("LAST_CHECKIN_TIME is not null");
            return (Criteria) this;
        }

        public Criteria andLastCheckinTimeEqualTo(Long value) {
            addCriterion("LAST_CHECKIN_TIME =", value, "lastCheckinTime");
            return (Criteria) this;
        }

        public Criteria andLastCheckinTimeNotEqualTo(Long value) {
            addCriterion("LAST_CHECKIN_TIME <>", value, "lastCheckinTime");
            return (Criteria) this;
        }

        public Criteria andLastCheckinTimeGreaterThan(Long value) {
            addCriterion("LAST_CHECKIN_TIME >", value, "lastCheckinTime");
            return (Criteria) this;
        }

        public Criteria andLastCheckinTimeGreaterThanOrEqualTo(Long value) {
            addCriterion("LAST_CHECKIN_TIME >=", value, "lastCheckinTime");
            return (Criteria) this;
        }

        public Criteria andLastCheckinTimeLessThan(Long value) {
            addCriterion("LAST_CHECKIN_TIME <", value, "lastCheckinTime");
            return (Criteria) this;
        }

        public Criteria andLastCheckinTimeLessThanOrEqualTo(Long value) {
            addCriterion("LAST_CHECKIN_TIME <=", value, "lastCheckinTime");
            return (Criteria) this;
        }

        public Criteria andLastCheckinTimeIn(List<Long> values) {
            addCriterion("LAST_CHECKIN_TIME in", values, "lastCheckinTime");
            return (Criteria) this;
        }

        public Criteria andLastCheckinTimeNotIn(List<Long> values) {
            addCriterion("LAST_CHECKIN_TIME not in", values, "lastCheckinTime");
            return (Criteria) this;
        }

        public Criteria andLastCheckinTimeBetween(Long value1, Long value2) {
            addCriterion("LAST_CHECKIN_TIME between", value1, value2, "lastCheckinTime");
            return (Criteria) this;
        }

        public Criteria andLastCheckinTimeNotBetween(Long value1, Long value2) {
            addCriterion("LAST_CHECKIN_TIME not between", value1, value2, "lastCheckinTime");
            return (Criteria) this;
        }

        public Criteria andCheckinIntervalIsNull() {
            addCriterion("CHECKIN_INTERVAL is null");
            return (Criteria) this;
        }

        public Criteria andCheckinIntervalIsNotNull() {
            addCriterion("CHECKIN_INTERVAL is not null");
            return (Criteria) this;
        }

        public Criteria andCheckinIntervalEqualTo(Long value) {
            addCriterion("CHECKIN_INTERVAL =", value, "checkinInterval");
            return (Criteria) this;
        }

        public Criteria andCheckinIntervalNotEqualTo(Long value) {
            addCriterion("CHECKIN_INTERVAL <>", value, "checkinInterval");
            return (Criteria) this;
        }

        public Criteria andCheckinIntervalGreaterThan(Long value) {
            addCriterion("CHECKIN_INTERVAL >", value, "checkinInterval");
            return (Criteria) this;
        }

        public Criteria andCheckinIntervalGreaterThanOrEqualTo(Long value) {
            addCriterion("CHECKIN_INTERVAL >=", value, "checkinInterval");
            return (Criteria) this;
        }

        public Criteria andCheckinIntervalLessThan(Long value) {
            addCriterion("CHECKIN_INTERVAL <", value, "checkinInterval");
            return (Criteria) this;
        }

        public Criteria andCheckinIntervalLessThanOrEqualTo(Long value) {
            addCriterion("CHECKIN_INTERVAL <=", value, "checkinInterval");
            return (Criteria) this;
        }

        public Criteria andCheckinIntervalIn(List<Long> values) {
            addCriterion("CHECKIN_INTERVAL in", values, "checkinInterval");
            return (Criteria) this;
        }

        public Criteria andCheckinIntervalNotIn(List<Long> values) {
            addCriterion("CHECKIN_INTERVAL not in", values, "checkinInterval");
            return (Criteria) this;
        }

        public Criteria andCheckinIntervalBetween(Long value1, Long value2) {
            addCriterion("CHECKIN_INTERVAL between", value1, value2, "checkinInterval");
            return (Criteria) this;
        }

        public Criteria andCheckinIntervalNotBetween(Long value1, Long value2) {
            addCriterion("CHECKIN_INTERVAL not between", value1, value2, "checkinInterval");
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