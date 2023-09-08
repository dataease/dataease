package io.dataease.plugins.common.base.domain;

import java.util.ArrayList;
import java.util.List;

public class PanelDesignExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public PanelDesignExample() {
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

        public Criteria andPanelIdIsNull() {
            addCriterion("panel_id is null");
            return (Criteria) this;
        }

        public Criteria andPanelIdIsNotNull() {
            addCriterion("panel_id is not null");
            return (Criteria) this;
        }

        public Criteria andPanelIdEqualTo(String value) {
            addCriterion("panel_id =", value, "panelId");
            return (Criteria) this;
        }

        public Criteria andPanelIdNotEqualTo(String value) {
            addCriterion("panel_id <>", value, "panelId");
            return (Criteria) this;
        }

        public Criteria andPanelIdGreaterThan(String value) {
            addCriterion("panel_id >", value, "panelId");
            return (Criteria) this;
        }

        public Criteria andPanelIdGreaterThanOrEqualTo(String value) {
            addCriterion("panel_id >=", value, "panelId");
            return (Criteria) this;
        }

        public Criteria andPanelIdLessThan(String value) {
            addCriterion("panel_id <", value, "panelId");
            return (Criteria) this;
        }

        public Criteria andPanelIdLessThanOrEqualTo(String value) {
            addCriterion("panel_id <=", value, "panelId");
            return (Criteria) this;
        }

        public Criteria andPanelIdLike(String value) {
            addCriterion("panel_id like", value, "panelId");
            return (Criteria) this;
        }

        public Criteria andPanelIdNotLike(String value) {
            addCriterion("panel_id not like", value, "panelId");
            return (Criteria) this;
        }

        public Criteria andPanelIdIn(List<String> values) {
            addCriterion("panel_id in", values, "panelId");
            return (Criteria) this;
        }

        public Criteria andPanelIdNotIn(List<String> values) {
            addCriterion("panel_id not in", values, "panelId");
            return (Criteria) this;
        }

        public Criteria andPanelIdBetween(String value1, String value2) {
            addCriterion("panel_id between", value1, value2, "panelId");
            return (Criteria) this;
        }

        public Criteria andPanelIdNotBetween(String value1, String value2) {
            addCriterion("panel_id not between", value1, value2, "panelId");
            return (Criteria) this;
        }

        public Criteria andComponentIdIsNull() {
            addCriterion("component_id is null");
            return (Criteria) this;
        }

        public Criteria andComponentIdIsNotNull() {
            addCriterion("component_id is not null");
            return (Criteria) this;
        }

        public Criteria andComponentIdEqualTo(String value) {
            addCriterion("component_id =", value, "componentId");
            return (Criteria) this;
        }

        public Criteria andComponentIdNotEqualTo(String value) {
            addCriterion("component_id <>", value, "componentId");
            return (Criteria) this;
        }

        public Criteria andComponentIdGreaterThan(String value) {
            addCriterion("component_id >", value, "componentId");
            return (Criteria) this;
        }

        public Criteria andComponentIdGreaterThanOrEqualTo(String value) {
            addCriterion("component_id >=", value, "componentId");
            return (Criteria) this;
        }

        public Criteria andComponentIdLessThan(String value) {
            addCriterion("component_id <", value, "componentId");
            return (Criteria) this;
        }

        public Criteria andComponentIdLessThanOrEqualTo(String value) {
            addCriterion("component_id <=", value, "componentId");
            return (Criteria) this;
        }

        public Criteria andComponentIdLike(String value) {
            addCriterion("component_id like", value, "componentId");
            return (Criteria) this;
        }

        public Criteria andComponentIdNotLike(String value) {
            addCriterion("component_id not like", value, "componentId");
            return (Criteria) this;
        }

        public Criteria andComponentIdIn(List<String> values) {
            addCriterion("component_id in", values, "componentId");
            return (Criteria) this;
        }

        public Criteria andComponentIdNotIn(List<String> values) {
            addCriterion("component_id not in", values, "componentId");
            return (Criteria) this;
        }

        public Criteria andComponentIdBetween(String value1, String value2) {
            addCriterion("component_id between", value1, value2, "componentId");
            return (Criteria) this;
        }

        public Criteria andComponentIdNotBetween(String value1, String value2) {
            addCriterion("component_id not between", value1, value2, "componentId");
            return (Criteria) this;
        }

        public Criteria andComponentStyleIsNull() {
            addCriterion("component_style is null");
            return (Criteria) this;
        }

        public Criteria andComponentStyleIsNotNull() {
            addCriterion("component_style is not null");
            return (Criteria) this;
        }

        public Criteria andComponentStyleEqualTo(String value) {
            addCriterion("component_style =", value, "componentStyle");
            return (Criteria) this;
        }

        public Criteria andComponentStyleNotEqualTo(String value) {
            addCriterion("component_style <>", value, "componentStyle");
            return (Criteria) this;
        }

        public Criteria andComponentStyleGreaterThan(String value) {
            addCriterion("component_style >", value, "componentStyle");
            return (Criteria) this;
        }

        public Criteria andComponentStyleGreaterThanOrEqualTo(String value) {
            addCriterion("component_style >=", value, "componentStyle");
            return (Criteria) this;
        }

        public Criteria andComponentStyleLessThan(String value) {
            addCriterion("component_style <", value, "componentStyle");
            return (Criteria) this;
        }

        public Criteria andComponentStyleLessThanOrEqualTo(String value) {
            addCriterion("component_style <=", value, "componentStyle");
            return (Criteria) this;
        }

        public Criteria andComponentStyleLike(String value) {
            addCriterion("component_style like", value, "componentStyle");
            return (Criteria) this;
        }

        public Criteria andComponentStyleNotLike(String value) {
            addCriterion("component_style not like", value, "componentStyle");
            return (Criteria) this;
        }

        public Criteria andComponentStyleIn(List<String> values) {
            addCriterion("component_style in", values, "componentStyle");
            return (Criteria) this;
        }

        public Criteria andComponentStyleNotIn(List<String> values) {
            addCriterion("component_style not in", values, "componentStyle");
            return (Criteria) this;
        }

        public Criteria andComponentStyleBetween(String value1, String value2) {
            addCriterion("component_style between", value1, value2, "componentStyle");
            return (Criteria) this;
        }

        public Criteria andComponentStyleNotBetween(String value1, String value2) {
            addCriterion("component_style not between", value1, value2, "componentStyle");
            return (Criteria) this;
        }

        public Criteria andComponentPositionIsNull() {
            addCriterion("component_position is null");
            return (Criteria) this;
        }

        public Criteria andComponentPositionIsNotNull() {
            addCriterion("component_position is not null");
            return (Criteria) this;
        }

        public Criteria andComponentPositionEqualTo(String value) {
            addCriterion("component_position =", value, "componentPosition");
            return (Criteria) this;
        }

        public Criteria andComponentPositionNotEqualTo(String value) {
            addCriterion("component_position <>", value, "componentPosition");
            return (Criteria) this;
        }

        public Criteria andComponentPositionGreaterThan(String value) {
            addCriterion("component_position >", value, "componentPosition");
            return (Criteria) this;
        }

        public Criteria andComponentPositionGreaterThanOrEqualTo(String value) {
            addCriterion("component_position >=", value, "componentPosition");
            return (Criteria) this;
        }

        public Criteria andComponentPositionLessThan(String value) {
            addCriterion("component_position <", value, "componentPosition");
            return (Criteria) this;
        }

        public Criteria andComponentPositionLessThanOrEqualTo(String value) {
            addCriterion("component_position <=", value, "componentPosition");
            return (Criteria) this;
        }

        public Criteria andComponentPositionLike(String value) {
            addCriterion("component_position like", value, "componentPosition");
            return (Criteria) this;
        }

        public Criteria andComponentPositionNotLike(String value) {
            addCriterion("component_position not like", value, "componentPosition");
            return (Criteria) this;
        }

        public Criteria andComponentPositionIn(List<String> values) {
            addCriterion("component_position in", values, "componentPosition");
            return (Criteria) this;
        }

        public Criteria andComponentPositionNotIn(List<String> values) {
            addCriterion("component_position not in", values, "componentPosition");
            return (Criteria) this;
        }

        public Criteria andComponentPositionBetween(String value1, String value2) {
            addCriterion("component_position between", value1, value2, "componentPosition");
            return (Criteria) this;
        }

        public Criteria andComponentPositionNotBetween(String value1, String value2) {
            addCriterion("component_position not between", value1, value2, "componentPosition");
            return (Criteria) this;
        }

        public Criteria andComponentTypeIsNull() {
            addCriterion("component_type is null");
            return (Criteria) this;
        }

        public Criteria andComponentTypeIsNotNull() {
            addCriterion("component_type is not null");
            return (Criteria) this;
        }

        public Criteria andComponentTypeEqualTo(String value) {
            addCriterion("component_type =", value, "componentType");
            return (Criteria) this;
        }

        public Criteria andComponentTypeNotEqualTo(String value) {
            addCriterion("component_type <>", value, "componentType");
            return (Criteria) this;
        }

        public Criteria andComponentTypeGreaterThan(String value) {
            addCriterion("component_type >", value, "componentType");
            return (Criteria) this;
        }

        public Criteria andComponentTypeGreaterThanOrEqualTo(String value) {
            addCriterion("component_type >=", value, "componentType");
            return (Criteria) this;
        }

        public Criteria andComponentTypeLessThan(String value) {
            addCriterion("component_type <", value, "componentType");
            return (Criteria) this;
        }

        public Criteria andComponentTypeLessThanOrEqualTo(String value) {
            addCriterion("component_type <=", value, "componentType");
            return (Criteria) this;
        }

        public Criteria andComponentTypeLike(String value) {
            addCriterion("component_type like", value, "componentType");
            return (Criteria) this;
        }

        public Criteria andComponentTypeNotLike(String value) {
            addCriterion("component_type not like", value, "componentType");
            return (Criteria) this;
        }

        public Criteria andComponentTypeIn(List<String> values) {
            addCriterion("component_type in", values, "componentType");
            return (Criteria) this;
        }

        public Criteria andComponentTypeNotIn(List<String> values) {
            addCriterion("component_type not in", values, "componentType");
            return (Criteria) this;
        }

        public Criteria andComponentTypeBetween(String value1, String value2) {
            addCriterion("component_type between", value1, value2, "componentType");
            return (Criteria) this;
        }

        public Criteria andComponentTypeNotBetween(String value1, String value2) {
            addCriterion("component_type not between", value1, value2, "componentType");
            return (Criteria) this;
        }

        public Criteria andComponentDetailsIsNull() {
            addCriterion("component_details is null");
            return (Criteria) this;
        }

        public Criteria andComponentDetailsIsNotNull() {
            addCriterion("component_details is not null");
            return (Criteria) this;
        }

        public Criteria andComponentDetailsEqualTo(String value) {
            addCriterion("component_details =", value, "componentDetails");
            return (Criteria) this;
        }

        public Criteria andComponentDetailsNotEqualTo(String value) {
            addCriterion("component_details <>", value, "componentDetails");
            return (Criteria) this;
        }

        public Criteria andComponentDetailsGreaterThan(String value) {
            addCriterion("component_details >", value, "componentDetails");
            return (Criteria) this;
        }

        public Criteria andComponentDetailsGreaterThanOrEqualTo(String value) {
            addCriterion("component_details >=", value, "componentDetails");
            return (Criteria) this;
        }

        public Criteria andComponentDetailsLessThan(String value) {
            addCriterion("component_details <", value, "componentDetails");
            return (Criteria) this;
        }

        public Criteria andComponentDetailsLessThanOrEqualTo(String value) {
            addCriterion("component_details <=", value, "componentDetails");
            return (Criteria) this;
        }

        public Criteria andComponentDetailsLike(String value) {
            addCriterion("component_details like", value, "componentDetails");
            return (Criteria) this;
        }

        public Criteria andComponentDetailsNotLike(String value) {
            addCriterion("component_details not like", value, "componentDetails");
            return (Criteria) this;
        }

        public Criteria andComponentDetailsIn(List<String> values) {
            addCriterion("component_details in", values, "componentDetails");
            return (Criteria) this;
        }

        public Criteria andComponentDetailsNotIn(List<String> values) {
            addCriterion("component_details not in", values, "componentDetails");
            return (Criteria) this;
        }

        public Criteria andComponentDetailsBetween(String value1, String value2) {
            addCriterion("component_details between", value1, value2, "componentDetails");
            return (Criteria) this;
        }

        public Criteria andComponentDetailsNotBetween(String value1, String value2) {
            addCriterion("component_details not between", value1, value2, "componentDetails");
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

        public Criteria andUpdatePersonIsNull() {
            addCriterion("update_person is null");
            return (Criteria) this;
        }

        public Criteria andUpdatePersonIsNotNull() {
            addCriterion("update_person is not null");
            return (Criteria) this;
        }

        public Criteria andUpdatePersonEqualTo(String value) {
            addCriterion("update_person =", value, "updatePerson");
            return (Criteria) this;
        }

        public Criteria andUpdatePersonNotEqualTo(String value) {
            addCriterion("update_person <>", value, "updatePerson");
            return (Criteria) this;
        }

        public Criteria andUpdatePersonGreaterThan(String value) {
            addCriterion("update_person >", value, "updatePerson");
            return (Criteria) this;
        }

        public Criteria andUpdatePersonGreaterThanOrEqualTo(String value) {
            addCriterion("update_person >=", value, "updatePerson");
            return (Criteria) this;
        }

        public Criteria andUpdatePersonLessThan(String value) {
            addCriterion("update_person <", value, "updatePerson");
            return (Criteria) this;
        }

        public Criteria andUpdatePersonLessThanOrEqualTo(String value) {
            addCriterion("update_person <=", value, "updatePerson");
            return (Criteria) this;
        }

        public Criteria andUpdatePersonLike(String value) {
            addCriterion("update_person like", value, "updatePerson");
            return (Criteria) this;
        }

        public Criteria andUpdatePersonNotLike(String value) {
            addCriterion("update_person not like", value, "updatePerson");
            return (Criteria) this;
        }

        public Criteria andUpdatePersonIn(List<String> values) {
            addCriterion("update_person in", values, "updatePerson");
            return (Criteria) this;
        }

        public Criteria andUpdatePersonNotIn(List<String> values) {
            addCriterion("update_person not in", values, "updatePerson");
            return (Criteria) this;
        }

        public Criteria andUpdatePersonBetween(String value1, String value2) {
            addCriterion("update_person between", value1, value2, "updatePerson");
            return (Criteria) this;
        }

        public Criteria andUpdatePersonNotBetween(String value1, String value2) {
            addCriterion("update_person not between", value1, value2, "updatePerson");
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