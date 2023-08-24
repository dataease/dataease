package io.dataease.plugins.common.base.domain;

import java.util.ArrayList;
import java.util.List;

public class PanelLinkJumpExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public PanelLinkJumpExample() {
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

        public Criteria andSourcePanelIdIsNull() {
            addCriterion("source_panel_id is null");
            return (Criteria) this;
        }

        public Criteria andSourcePanelIdIsNotNull() {
            addCriterion("source_panel_id is not null");
            return (Criteria) this;
        }

        public Criteria andSourcePanelIdEqualTo(String value) {
            addCriterion("source_panel_id =", value, "sourcePanelId");
            return (Criteria) this;
        }

        public Criteria andSourcePanelIdNotEqualTo(String value) {
            addCriterion("source_panel_id <>", value, "sourcePanelId");
            return (Criteria) this;
        }

        public Criteria andSourcePanelIdGreaterThan(String value) {
            addCriterion("source_panel_id >", value, "sourcePanelId");
            return (Criteria) this;
        }

        public Criteria andSourcePanelIdGreaterThanOrEqualTo(String value) {
            addCriterion("source_panel_id >=", value, "sourcePanelId");
            return (Criteria) this;
        }

        public Criteria andSourcePanelIdLessThan(String value) {
            addCriterion("source_panel_id <", value, "sourcePanelId");
            return (Criteria) this;
        }

        public Criteria andSourcePanelIdLessThanOrEqualTo(String value) {
            addCriterion("source_panel_id <=", value, "sourcePanelId");
            return (Criteria) this;
        }

        public Criteria andSourcePanelIdLike(String value) {
            addCriterion("source_panel_id like", value, "sourcePanelId");
            return (Criteria) this;
        }

        public Criteria andSourcePanelIdNotLike(String value) {
            addCriterion("source_panel_id not like", value, "sourcePanelId");
            return (Criteria) this;
        }

        public Criteria andSourcePanelIdIn(List<String> values) {
            addCriterion("source_panel_id in", values, "sourcePanelId");
            return (Criteria) this;
        }

        public Criteria andSourcePanelIdNotIn(List<String> values) {
            addCriterion("source_panel_id not in", values, "sourcePanelId");
            return (Criteria) this;
        }

        public Criteria andSourcePanelIdBetween(String value1, String value2) {
            addCriterion("source_panel_id between", value1, value2, "sourcePanelId");
            return (Criteria) this;
        }

        public Criteria andSourcePanelIdNotBetween(String value1, String value2) {
            addCriterion("source_panel_id not between", value1, value2, "sourcePanelId");
            return (Criteria) this;
        }

        public Criteria andSourceViewIdIsNull() {
            addCriterion("source_view_id is null");
            return (Criteria) this;
        }

        public Criteria andSourceViewIdIsNotNull() {
            addCriterion("source_view_id is not null");
            return (Criteria) this;
        }

        public Criteria andSourceViewIdEqualTo(String value) {
            addCriterion("source_view_id =", value, "sourceViewId");
            return (Criteria) this;
        }

        public Criteria andSourceViewIdNotEqualTo(String value) {
            addCriterion("source_view_id <>", value, "sourceViewId");
            return (Criteria) this;
        }

        public Criteria andSourceViewIdGreaterThan(String value) {
            addCriterion("source_view_id >", value, "sourceViewId");
            return (Criteria) this;
        }

        public Criteria andSourceViewIdGreaterThanOrEqualTo(String value) {
            addCriterion("source_view_id >=", value, "sourceViewId");
            return (Criteria) this;
        }

        public Criteria andSourceViewIdLessThan(String value) {
            addCriterion("source_view_id <", value, "sourceViewId");
            return (Criteria) this;
        }

        public Criteria andSourceViewIdLessThanOrEqualTo(String value) {
            addCriterion("source_view_id <=", value, "sourceViewId");
            return (Criteria) this;
        }

        public Criteria andSourceViewIdLike(String value) {
            addCriterion("source_view_id like", value, "sourceViewId");
            return (Criteria) this;
        }

        public Criteria andSourceViewIdNotLike(String value) {
            addCriterion("source_view_id not like", value, "sourceViewId");
            return (Criteria) this;
        }

        public Criteria andSourceViewIdIn(List<String> values) {
            addCriterion("source_view_id in", values, "sourceViewId");
            return (Criteria) this;
        }

        public Criteria andSourceViewIdNotIn(List<String> values) {
            addCriterion("source_view_id not in", values, "sourceViewId");
            return (Criteria) this;
        }

        public Criteria andSourceViewIdBetween(String value1, String value2) {
            addCriterion("source_view_id between", value1, value2, "sourceViewId");
            return (Criteria) this;
        }

        public Criteria andSourceViewIdNotBetween(String value1, String value2) {
            addCriterion("source_view_id not between", value1, value2, "sourceViewId");
            return (Criteria) this;
        }

        public Criteria andLinkJumpInfoIsNull() {
            addCriterion("link_jump_info is null");
            return (Criteria) this;
        }

        public Criteria andLinkJumpInfoIsNotNull() {
            addCriterion("link_jump_info is not null");
            return (Criteria) this;
        }

        public Criteria andLinkJumpInfoEqualTo(String value) {
            addCriterion("link_jump_info =", value, "linkJumpInfo");
            return (Criteria) this;
        }

        public Criteria andLinkJumpInfoNotEqualTo(String value) {
            addCriterion("link_jump_info <>", value, "linkJumpInfo");
            return (Criteria) this;
        }

        public Criteria andLinkJumpInfoGreaterThan(String value) {
            addCriterion("link_jump_info >", value, "linkJumpInfo");
            return (Criteria) this;
        }

        public Criteria andLinkJumpInfoGreaterThanOrEqualTo(String value) {
            addCriterion("link_jump_info >=", value, "linkJumpInfo");
            return (Criteria) this;
        }

        public Criteria andLinkJumpInfoLessThan(String value) {
            addCriterion("link_jump_info <", value, "linkJumpInfo");
            return (Criteria) this;
        }

        public Criteria andLinkJumpInfoLessThanOrEqualTo(String value) {
            addCriterion("link_jump_info <=", value, "linkJumpInfo");
            return (Criteria) this;
        }

        public Criteria andLinkJumpInfoLike(String value) {
            addCriterion("link_jump_info like", value, "linkJumpInfo");
            return (Criteria) this;
        }

        public Criteria andLinkJumpInfoNotLike(String value) {
            addCriterion("link_jump_info not like", value, "linkJumpInfo");
            return (Criteria) this;
        }

        public Criteria andLinkJumpInfoIn(List<String> values) {
            addCriterion("link_jump_info in", values, "linkJumpInfo");
            return (Criteria) this;
        }

        public Criteria andLinkJumpInfoNotIn(List<String> values) {
            addCriterion("link_jump_info not in", values, "linkJumpInfo");
            return (Criteria) this;
        }

        public Criteria andLinkJumpInfoBetween(String value1, String value2) {
            addCriterion("link_jump_info between", value1, value2, "linkJumpInfo");
            return (Criteria) this;
        }

        public Criteria andLinkJumpInfoNotBetween(String value1, String value2) {
            addCriterion("link_jump_info not between", value1, value2, "linkJumpInfo");
            return (Criteria) this;
        }

        public Criteria andCheckedIsNull() {
            addCriterion("`checked` is null");
            return (Criteria) this;
        }

        public Criteria andCheckedIsNotNull() {
            addCriterion("`checked` is not null");
            return (Criteria) this;
        }

        public Criteria andCheckedEqualTo(Boolean value) {
            addCriterion("`checked` =", value, "checked");
            return (Criteria) this;
        }

        public Criteria andCheckedNotEqualTo(Boolean value) {
            addCriterion("`checked` <>", value, "checked");
            return (Criteria) this;
        }

        public Criteria andCheckedGreaterThan(Boolean value) {
            addCriterion("`checked` >", value, "checked");
            return (Criteria) this;
        }

        public Criteria andCheckedGreaterThanOrEqualTo(Boolean value) {
            addCriterion("`checked` >=", value, "checked");
            return (Criteria) this;
        }

        public Criteria andCheckedLessThan(Boolean value) {
            addCriterion("`checked` <", value, "checked");
            return (Criteria) this;
        }

        public Criteria andCheckedLessThanOrEqualTo(Boolean value) {
            addCriterion("`checked` <=", value, "checked");
            return (Criteria) this;
        }

        public Criteria andCheckedIn(List<Boolean> values) {
            addCriterion("`checked` in", values, "checked");
            return (Criteria) this;
        }

        public Criteria andCheckedNotIn(List<Boolean> values) {
            addCriterion("`checked` not in", values, "checked");
            return (Criteria) this;
        }

        public Criteria andCheckedBetween(Boolean value1, Boolean value2) {
            addCriterion("`checked` between", value1, value2, "checked");
            return (Criteria) this;
        }

        public Criteria andCheckedNotBetween(Boolean value1, Boolean value2) {
            addCriterion("`checked` not between", value1, value2, "checked");
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