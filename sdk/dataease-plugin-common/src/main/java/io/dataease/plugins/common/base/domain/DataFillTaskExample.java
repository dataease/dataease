package io.dataease.plugins.common.base.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DataFillTaskExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public DataFillTaskExample() {
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

        public Criteria andIdEqualTo(Long value) {
            addCriterion("id =", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(Long value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(Long value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(Long value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThan(Long value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(Long value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdIn(List<Long> values) {
            addCriterion("id in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotIn(List<Long> values) {
            addCriterion("id not in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdBetween(Long value1, Long value2) {
            addCriterion("id between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotBetween(Long value1, Long value2) {
            addCriterion("id not between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andNameIsNull() {
            addCriterion("`name` is null");
            return (Criteria) this;
        }

        public Criteria andNameIsNotNull() {
            addCriterion("`name` is not null");
            return (Criteria) this;
        }

        public Criteria andNameEqualTo(String value) {
            addCriterion("`name` =", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotEqualTo(String value) {
            addCriterion("`name` <>", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameGreaterThan(String value) {
            addCriterion("`name` >", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameGreaterThanOrEqualTo(String value) {
            addCriterion("`name` >=", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLessThan(String value) {
            addCriterion("`name` <", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLessThanOrEqualTo(String value) {
            addCriterion("`name` <=", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLike(String value) {
            addCriterion("`name` like", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotLike(String value) {
            addCriterion("`name` not like", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameIn(List<String> values) {
            addCriterion("`name` in", values, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotIn(List<String> values) {
            addCriterion("`name` not in", values, "name");
            return (Criteria) this;
        }

        public Criteria andNameBetween(String value1, String value2) {
            addCriterion("`name` between", value1, value2, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotBetween(String value1, String value2) {
            addCriterion("`name` not between", value1, value2, "name");
            return (Criteria) this;
        }

        public Criteria andFormIdIsNull() {
            addCriterion("form_id is null");
            return (Criteria) this;
        }

        public Criteria andFormIdIsNotNull() {
            addCriterion("form_id is not null");
            return (Criteria) this;
        }

        public Criteria andFormIdEqualTo(String value) {
            addCriterion("form_id =", value, "formId");
            return (Criteria) this;
        }

        public Criteria andFormIdNotEqualTo(String value) {
            addCriterion("form_id <>", value, "formId");
            return (Criteria) this;
        }

        public Criteria andFormIdGreaterThan(String value) {
            addCriterion("form_id >", value, "formId");
            return (Criteria) this;
        }

        public Criteria andFormIdGreaterThanOrEqualTo(String value) {
            addCriterion("form_id >=", value, "formId");
            return (Criteria) this;
        }

        public Criteria andFormIdLessThan(String value) {
            addCriterion("form_id <", value, "formId");
            return (Criteria) this;
        }

        public Criteria andFormIdLessThanOrEqualTo(String value) {
            addCriterion("form_id <=", value, "formId");
            return (Criteria) this;
        }

        public Criteria andFormIdLike(String value) {
            addCriterion("form_id like", value, "formId");
            return (Criteria) this;
        }

        public Criteria andFormIdNotLike(String value) {
            addCriterion("form_id not like", value, "formId");
            return (Criteria) this;
        }

        public Criteria andFormIdIn(List<String> values) {
            addCriterion("form_id in", values, "formId");
            return (Criteria) this;
        }

        public Criteria andFormIdNotIn(List<String> values) {
            addCriterion("form_id not in", values, "formId");
            return (Criteria) this;
        }

        public Criteria andFormIdBetween(String value1, String value2) {
            addCriterion("form_id between", value1, value2, "formId");
            return (Criteria) this;
        }

        public Criteria andFormIdNotBetween(String value1, String value2) {
            addCriterion("form_id not between", value1, value2, "formId");
            return (Criteria) this;
        }

        public Criteria andStartTimeIsNull() {
            addCriterion("start_time is null");
            return (Criteria) this;
        }

        public Criteria andStartTimeIsNotNull() {
            addCriterion("start_time is not null");
            return (Criteria) this;
        }

        public Criteria andStartTimeEqualTo(Date value) {
            addCriterion("start_time =", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeNotEqualTo(Date value) {
            addCriterion("start_time <>", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeGreaterThan(Date value) {
            addCriterion("start_time >", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("start_time >=", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeLessThan(Date value) {
            addCriterion("start_time <", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeLessThanOrEqualTo(Date value) {
            addCriterion("start_time <=", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeIn(List<Date> values) {
            addCriterion("start_time in", values, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeNotIn(List<Date> values) {
            addCriterion("start_time not in", values, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeBetween(Date value1, Date value2) {
            addCriterion("start_time between", value1, value2, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeNotBetween(Date value1, Date value2) {
            addCriterion("start_time not between", value1, value2, "startTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeIsNull() {
            addCriterion("end_time is null");
            return (Criteria) this;
        }

        public Criteria andEndTimeIsNotNull() {
            addCriterion("end_time is not null");
            return (Criteria) this;
        }

        public Criteria andEndTimeEqualTo(Date value) {
            addCriterion("end_time =", value, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeNotEqualTo(Date value) {
            addCriterion("end_time <>", value, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeGreaterThan(Date value) {
            addCriterion("end_time >", value, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("end_time >=", value, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeLessThan(Date value) {
            addCriterion("end_time <", value, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeLessThanOrEqualTo(Date value) {
            addCriterion("end_time <=", value, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeIn(List<Date> values) {
            addCriterion("end_time in", values, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeNotIn(List<Date> values) {
            addCriterion("end_time not in", values, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeBetween(Date value1, Date value2) {
            addCriterion("end_time between", value1, value2, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeNotBetween(Date value1, Date value2) {
            addCriterion("end_time not between", value1, value2, "endTime");
            return (Criteria) this;
        }

        public Criteria andRateTypeIsNull() {
            addCriterion("rate_type is null");
            return (Criteria) this;
        }

        public Criteria andRateTypeIsNotNull() {
            addCriterion("rate_type is not null");
            return (Criteria) this;
        }

        public Criteria andRateTypeEqualTo(Integer value) {
            addCriterion("rate_type =", value, "rateType");
            return (Criteria) this;
        }

        public Criteria andRateTypeNotEqualTo(Integer value) {
            addCriterion("rate_type <>", value, "rateType");
            return (Criteria) this;
        }

        public Criteria andRateTypeGreaterThan(Integer value) {
            addCriterion("rate_type >", value, "rateType");
            return (Criteria) this;
        }

        public Criteria andRateTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("rate_type >=", value, "rateType");
            return (Criteria) this;
        }

        public Criteria andRateTypeLessThan(Integer value) {
            addCriterion("rate_type <", value, "rateType");
            return (Criteria) this;
        }

        public Criteria andRateTypeLessThanOrEqualTo(Integer value) {
            addCriterion("rate_type <=", value, "rateType");
            return (Criteria) this;
        }

        public Criteria andRateTypeIn(List<Integer> values) {
            addCriterion("rate_type in", values, "rateType");
            return (Criteria) this;
        }

        public Criteria andRateTypeNotIn(List<Integer> values) {
            addCriterion("rate_type not in", values, "rateType");
            return (Criteria) this;
        }

        public Criteria andRateTypeBetween(Integer value1, Integer value2) {
            addCriterion("rate_type between", value1, value2, "rateType");
            return (Criteria) this;
        }

        public Criteria andRateTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("rate_type not between", value1, value2, "rateType");
            return (Criteria) this;
        }

        public Criteria andRateValIsNull() {
            addCriterion("rate_val is null");
            return (Criteria) this;
        }

        public Criteria andRateValIsNotNull() {
            addCriterion("rate_val is not null");
            return (Criteria) this;
        }

        public Criteria andRateValEqualTo(String value) {
            addCriterion("rate_val =", value, "rateVal");
            return (Criteria) this;
        }

        public Criteria andRateValNotEqualTo(String value) {
            addCriterion("rate_val <>", value, "rateVal");
            return (Criteria) this;
        }

        public Criteria andRateValGreaterThan(String value) {
            addCriterion("rate_val >", value, "rateVal");
            return (Criteria) this;
        }

        public Criteria andRateValGreaterThanOrEqualTo(String value) {
            addCriterion("rate_val >=", value, "rateVal");
            return (Criteria) this;
        }

        public Criteria andRateValLessThan(String value) {
            addCriterion("rate_val <", value, "rateVal");
            return (Criteria) this;
        }

        public Criteria andRateValLessThanOrEqualTo(String value) {
            addCriterion("rate_val <=", value, "rateVal");
            return (Criteria) this;
        }

        public Criteria andRateValLike(String value) {
            addCriterion("rate_val like", value, "rateVal");
            return (Criteria) this;
        }

        public Criteria andRateValNotLike(String value) {
            addCriterion("rate_val not like", value, "rateVal");
            return (Criteria) this;
        }

        public Criteria andRateValIn(List<String> values) {
            addCriterion("rate_val in", values, "rateVal");
            return (Criteria) this;
        }

        public Criteria andRateValNotIn(List<String> values) {
            addCriterion("rate_val not in", values, "rateVal");
            return (Criteria) this;
        }

        public Criteria andRateValBetween(String value1, String value2) {
            addCriterion("rate_val between", value1, value2, "rateVal");
            return (Criteria) this;
        }

        public Criteria andRateValNotBetween(String value1, String value2) {
            addCriterion("rate_val not between", value1, value2, "rateVal");
            return (Criteria) this;
        }

        public Criteria andPublishStartTimeIsNull() {
            addCriterion("publish_start_time is null");
            return (Criteria) this;
        }

        public Criteria andPublishStartTimeIsNotNull() {
            addCriterion("publish_start_time is not null");
            return (Criteria) this;
        }

        public Criteria andPublishStartTimeEqualTo(Date value) {
            addCriterion("publish_start_time =", value, "publishStartTime");
            return (Criteria) this;
        }

        public Criteria andPublishStartTimeNotEqualTo(Date value) {
            addCriterion("publish_start_time <>", value, "publishStartTime");
            return (Criteria) this;
        }

        public Criteria andPublishStartTimeGreaterThan(Date value) {
            addCriterion("publish_start_time >", value, "publishStartTime");
            return (Criteria) this;
        }

        public Criteria andPublishStartTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("publish_start_time >=", value, "publishStartTime");
            return (Criteria) this;
        }

        public Criteria andPublishStartTimeLessThan(Date value) {
            addCriterion("publish_start_time <", value, "publishStartTime");
            return (Criteria) this;
        }

        public Criteria andPublishStartTimeLessThanOrEqualTo(Date value) {
            addCriterion("publish_start_time <=", value, "publishStartTime");
            return (Criteria) this;
        }

        public Criteria andPublishStartTimeIn(List<Date> values) {
            addCriterion("publish_start_time in", values, "publishStartTime");
            return (Criteria) this;
        }

        public Criteria andPublishStartTimeNotIn(List<Date> values) {
            addCriterion("publish_start_time not in", values, "publishStartTime");
            return (Criteria) this;
        }

        public Criteria andPublishStartTimeBetween(Date value1, Date value2) {
            addCriterion("publish_start_time between", value1, value2, "publishStartTime");
            return (Criteria) this;
        }

        public Criteria andPublishStartTimeNotBetween(Date value1, Date value2) {
            addCriterion("publish_start_time not between", value1, value2, "publishStartTime");
            return (Criteria) this;
        }

        public Criteria andPublishEndTimeIsNull() {
            addCriterion("publish_end_time is null");
            return (Criteria) this;
        }

        public Criteria andPublishEndTimeIsNotNull() {
            addCriterion("publish_end_time is not null");
            return (Criteria) this;
        }

        public Criteria andPublishEndTimeEqualTo(Date value) {
            addCriterion("publish_end_time =", value, "publishEndTime");
            return (Criteria) this;
        }

        public Criteria andPublishEndTimeNotEqualTo(Date value) {
            addCriterion("publish_end_time <>", value, "publishEndTime");
            return (Criteria) this;
        }

        public Criteria andPublishEndTimeGreaterThan(Date value) {
            addCriterion("publish_end_time >", value, "publishEndTime");
            return (Criteria) this;
        }

        public Criteria andPublishEndTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("publish_end_time >=", value, "publishEndTime");
            return (Criteria) this;
        }

        public Criteria andPublishEndTimeLessThan(Date value) {
            addCriterion("publish_end_time <", value, "publishEndTime");
            return (Criteria) this;
        }

        public Criteria andPublishEndTimeLessThanOrEqualTo(Date value) {
            addCriterion("publish_end_time <=", value, "publishEndTime");
            return (Criteria) this;
        }

        public Criteria andPublishEndTimeIn(List<Date> values) {
            addCriterion("publish_end_time in", values, "publishEndTime");
            return (Criteria) this;
        }

        public Criteria andPublishEndTimeNotIn(List<Date> values) {
            addCriterion("publish_end_time not in", values, "publishEndTime");
            return (Criteria) this;
        }

        public Criteria andPublishEndTimeBetween(Date value1, Date value2) {
            addCriterion("publish_end_time between", value1, value2, "publishEndTime");
            return (Criteria) this;
        }

        public Criteria andPublishEndTimeNotBetween(Date value1, Date value2) {
            addCriterion("publish_end_time not between", value1, value2, "publishEndTime");
            return (Criteria) this;
        }

        public Criteria andPublishRangeTimeTypeIsNull() {
            addCriterion("publish_range_time_type is null");
            return (Criteria) this;
        }

        public Criteria andPublishRangeTimeTypeIsNotNull() {
            addCriterion("publish_range_time_type is not null");
            return (Criteria) this;
        }

        public Criteria andPublishRangeTimeTypeEqualTo(Integer value) {
            addCriterion("publish_range_time_type =", value, "publishRangeTimeType");
            return (Criteria) this;
        }

        public Criteria andPublishRangeTimeTypeNotEqualTo(Integer value) {
            addCriterion("publish_range_time_type <>", value, "publishRangeTimeType");
            return (Criteria) this;
        }

        public Criteria andPublishRangeTimeTypeGreaterThan(Integer value) {
            addCriterion("publish_range_time_type >", value, "publishRangeTimeType");
            return (Criteria) this;
        }

        public Criteria andPublishRangeTimeTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("publish_range_time_type >=", value, "publishRangeTimeType");
            return (Criteria) this;
        }

        public Criteria andPublishRangeTimeTypeLessThan(Integer value) {
            addCriterion("publish_range_time_type <", value, "publishRangeTimeType");
            return (Criteria) this;
        }

        public Criteria andPublishRangeTimeTypeLessThanOrEqualTo(Integer value) {
            addCriterion("publish_range_time_type <=", value, "publishRangeTimeType");
            return (Criteria) this;
        }

        public Criteria andPublishRangeTimeTypeIn(List<Integer> values) {
            addCriterion("publish_range_time_type in", values, "publishRangeTimeType");
            return (Criteria) this;
        }

        public Criteria andPublishRangeTimeTypeNotIn(List<Integer> values) {
            addCriterion("publish_range_time_type not in", values, "publishRangeTimeType");
            return (Criteria) this;
        }

        public Criteria andPublishRangeTimeTypeBetween(Integer value1, Integer value2) {
            addCriterion("publish_range_time_type between", value1, value2, "publishRangeTimeType");
            return (Criteria) this;
        }

        public Criteria andPublishRangeTimeTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("publish_range_time_type not between", value1, value2, "publishRangeTimeType");
            return (Criteria) this;
        }

        public Criteria andPublishRangeTimeIsNull() {
            addCriterion("publish_range_time is null");
            return (Criteria) this;
        }

        public Criteria andPublishRangeTimeIsNotNull() {
            addCriterion("publish_range_time is not null");
            return (Criteria) this;
        }

        public Criteria andPublishRangeTimeEqualTo(Integer value) {
            addCriterion("publish_range_time =", value, "publishRangeTime");
            return (Criteria) this;
        }

        public Criteria andPublishRangeTimeNotEqualTo(Integer value) {
            addCriterion("publish_range_time <>", value, "publishRangeTime");
            return (Criteria) this;
        }

        public Criteria andPublishRangeTimeGreaterThan(Integer value) {
            addCriterion("publish_range_time >", value, "publishRangeTime");
            return (Criteria) this;
        }

        public Criteria andPublishRangeTimeGreaterThanOrEqualTo(Integer value) {
            addCriterion("publish_range_time >=", value, "publishRangeTime");
            return (Criteria) this;
        }

        public Criteria andPublishRangeTimeLessThan(Integer value) {
            addCriterion("publish_range_time <", value, "publishRangeTime");
            return (Criteria) this;
        }

        public Criteria andPublishRangeTimeLessThanOrEqualTo(Integer value) {
            addCriterion("publish_range_time <=", value, "publishRangeTime");
            return (Criteria) this;
        }

        public Criteria andPublishRangeTimeIn(List<Integer> values) {
            addCriterion("publish_range_time in", values, "publishRangeTime");
            return (Criteria) this;
        }

        public Criteria andPublishRangeTimeNotIn(List<Integer> values) {
            addCriterion("publish_range_time not in", values, "publishRangeTime");
            return (Criteria) this;
        }

        public Criteria andPublishRangeTimeBetween(Integer value1, Integer value2) {
            addCriterion("publish_range_time between", value1, value2, "publishRangeTime");
            return (Criteria) this;
        }

        public Criteria andPublishRangeTimeNotBetween(Integer value1, Integer value2) {
            addCriterion("publish_range_time not between", value1, value2, "publishRangeTime");
            return (Criteria) this;
        }

        public Criteria andCreatorIsNull() {
            addCriterion("creator is null");
            return (Criteria) this;
        }

        public Criteria andCreatorIsNotNull() {
            addCriterion("creator is not null");
            return (Criteria) this;
        }

        public Criteria andCreatorEqualTo(Long value) {
            addCriterion("creator =", value, "creator");
            return (Criteria) this;
        }

        public Criteria andCreatorNotEqualTo(Long value) {
            addCriterion("creator <>", value, "creator");
            return (Criteria) this;
        }

        public Criteria andCreatorGreaterThan(Long value) {
            addCriterion("creator >", value, "creator");
            return (Criteria) this;
        }

        public Criteria andCreatorGreaterThanOrEqualTo(Long value) {
            addCriterion("creator >=", value, "creator");
            return (Criteria) this;
        }

        public Criteria andCreatorLessThan(Long value) {
            addCriterion("creator <", value, "creator");
            return (Criteria) this;
        }

        public Criteria andCreatorLessThanOrEqualTo(Long value) {
            addCriterion("creator <=", value, "creator");
            return (Criteria) this;
        }

        public Criteria andCreatorIn(List<Long> values) {
            addCriterion("creator in", values, "creator");
            return (Criteria) this;
        }

        public Criteria andCreatorNotIn(List<Long> values) {
            addCriterion("creator not in", values, "creator");
            return (Criteria) this;
        }

        public Criteria andCreatorBetween(Long value1, Long value2) {
            addCriterion("creator between", value1, value2, "creator");
            return (Criteria) this;
        }

        public Criteria andCreatorNotBetween(Long value1, Long value2) {
            addCriterion("creator not between", value1, value2, "creator");
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

        public Criteria andCreateTimeEqualTo(Date value) {
            addCriterion("create_time =", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotEqualTo(Date value) {
            addCriterion("create_time <>", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThan(Date value) {
            addCriterion("create_time >", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("create_time >=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThan(Date value) {
            addCriterion("create_time <", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThanOrEqualTo(Date value) {
            addCriterion("create_time <=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIn(List<Date> values) {
            addCriterion("create_time in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotIn(List<Date> values) {
            addCriterion("create_time not in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeBetween(Date value1, Date value2) {
            addCriterion("create_time between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotBetween(Date value1, Date value2) {
            addCriterion("create_time not between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andStatusIsNull() {
            addCriterion("`status` is null");
            return (Criteria) this;
        }

        public Criteria andStatusIsNotNull() {
            addCriterion("`status` is not null");
            return (Criteria) this;
        }

        public Criteria andStatusEqualTo(Boolean value) {
            addCriterion("`status` =", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotEqualTo(Boolean value) {
            addCriterion("`status` <>", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThan(Boolean value) {
            addCriterion("`status` >", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThanOrEqualTo(Boolean value) {
            addCriterion("`status` >=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThan(Boolean value) {
            addCriterion("`status` <", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThanOrEqualTo(Boolean value) {
            addCriterion("`status` <=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusIn(List<Boolean> values) {
            addCriterion("`status` in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotIn(List<Boolean> values) {
            addCriterion("`status` not in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusBetween(Boolean value1, Boolean value2) {
            addCriterion("`status` between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotBetween(Boolean value1, Boolean value2) {
            addCriterion("`status` not between", value1, value2, "status");
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