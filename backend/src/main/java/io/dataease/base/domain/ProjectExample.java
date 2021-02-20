package io.dataease.base.domain;

import java.util.ArrayList;
import java.util.List;

public class ProjectExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public ProjectExample() {
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

        public Criteria andWorkspaceIdIsNull() {
            addCriterion("workspace_id is null");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdIsNotNull() {
            addCriterion("workspace_id is not null");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdEqualTo(String value) {
            addCriterion("workspace_id =", value, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdNotEqualTo(String value) {
            addCriterion("workspace_id <>", value, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdGreaterThan(String value) {
            addCriterion("workspace_id >", value, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdGreaterThanOrEqualTo(String value) {
            addCriterion("workspace_id >=", value, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdLessThan(String value) {
            addCriterion("workspace_id <", value, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdLessThanOrEqualTo(String value) {
            addCriterion("workspace_id <=", value, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdLike(String value) {
            addCriterion("workspace_id like", value, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdNotLike(String value) {
            addCriterion("workspace_id not like", value, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdIn(List<String> values) {
            addCriterion("workspace_id in", values, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdNotIn(List<String> values) {
            addCriterion("workspace_id not in", values, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdBetween(String value1, String value2) {
            addCriterion("workspace_id between", value1, value2, "workspaceId");
            return (Criteria) this;
        }

        public Criteria andWorkspaceIdNotBetween(String value1, String value2) {
            addCriterion("workspace_id not between", value1, value2, "workspaceId");
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

        public Criteria andTapdIdIsNull() {
            addCriterion("tapd_id is null");
            return (Criteria) this;
        }

        public Criteria andTapdIdIsNotNull() {
            addCriterion("tapd_id is not null");
            return (Criteria) this;
        }

        public Criteria andTapdIdEqualTo(String value) {
            addCriterion("tapd_id =", value, "tapdId");
            return (Criteria) this;
        }

        public Criteria andTapdIdNotEqualTo(String value) {
            addCriterion("tapd_id <>", value, "tapdId");
            return (Criteria) this;
        }

        public Criteria andTapdIdGreaterThan(String value) {
            addCriterion("tapd_id >", value, "tapdId");
            return (Criteria) this;
        }

        public Criteria andTapdIdGreaterThanOrEqualTo(String value) {
            addCriterion("tapd_id >=", value, "tapdId");
            return (Criteria) this;
        }

        public Criteria andTapdIdLessThan(String value) {
            addCriterion("tapd_id <", value, "tapdId");
            return (Criteria) this;
        }

        public Criteria andTapdIdLessThanOrEqualTo(String value) {
            addCriterion("tapd_id <=", value, "tapdId");
            return (Criteria) this;
        }

        public Criteria andTapdIdLike(String value) {
            addCriterion("tapd_id like", value, "tapdId");
            return (Criteria) this;
        }

        public Criteria andTapdIdNotLike(String value) {
            addCriterion("tapd_id not like", value, "tapdId");
            return (Criteria) this;
        }

        public Criteria andTapdIdIn(List<String> values) {
            addCriterion("tapd_id in", values, "tapdId");
            return (Criteria) this;
        }

        public Criteria andTapdIdNotIn(List<String> values) {
            addCriterion("tapd_id not in", values, "tapdId");
            return (Criteria) this;
        }

        public Criteria andTapdIdBetween(String value1, String value2) {
            addCriterion("tapd_id between", value1, value2, "tapdId");
            return (Criteria) this;
        }

        public Criteria andTapdIdNotBetween(String value1, String value2) {
            addCriterion("tapd_id not between", value1, value2, "tapdId");
            return (Criteria) this;
        }

        public Criteria andJiraKeyIsNull() {
            addCriterion("jira_key is null");
            return (Criteria) this;
        }

        public Criteria andJiraKeyIsNotNull() {
            addCriterion("jira_key is not null");
            return (Criteria) this;
        }

        public Criteria andJiraKeyEqualTo(String value) {
            addCriterion("jira_key =", value, "jiraKey");
            return (Criteria) this;
        }

        public Criteria andJiraKeyNotEqualTo(String value) {
            addCriterion("jira_key <>", value, "jiraKey");
            return (Criteria) this;
        }

        public Criteria andJiraKeyGreaterThan(String value) {
            addCriterion("jira_key >", value, "jiraKey");
            return (Criteria) this;
        }

        public Criteria andJiraKeyGreaterThanOrEqualTo(String value) {
            addCriterion("jira_key >=", value, "jiraKey");
            return (Criteria) this;
        }

        public Criteria andJiraKeyLessThan(String value) {
            addCriterion("jira_key <", value, "jiraKey");
            return (Criteria) this;
        }

        public Criteria andJiraKeyLessThanOrEqualTo(String value) {
            addCriterion("jira_key <=", value, "jiraKey");
            return (Criteria) this;
        }

        public Criteria andJiraKeyLike(String value) {
            addCriterion("jira_key like", value, "jiraKey");
            return (Criteria) this;
        }

        public Criteria andJiraKeyNotLike(String value) {
            addCriterion("jira_key not like", value, "jiraKey");
            return (Criteria) this;
        }

        public Criteria andJiraKeyIn(List<String> values) {
            addCriterion("jira_key in", values, "jiraKey");
            return (Criteria) this;
        }

        public Criteria andJiraKeyNotIn(List<String> values) {
            addCriterion("jira_key not in", values, "jiraKey");
            return (Criteria) this;
        }

        public Criteria andJiraKeyBetween(String value1, String value2) {
            addCriterion("jira_key between", value1, value2, "jiraKey");
            return (Criteria) this;
        }

        public Criteria andJiraKeyNotBetween(String value1, String value2) {
            addCriterion("jira_key not between", value1, value2, "jiraKey");
            return (Criteria) this;
        }

        public Criteria andZentaoIdIsNull() {
            addCriterion("zentao_id is null");
            return (Criteria) this;
        }

        public Criteria andZentaoIdIsNotNull() {
            addCriterion("zentao_id is not null");
            return (Criteria) this;
        }

        public Criteria andZentaoIdEqualTo(String value) {
            addCriterion("zentao_id =", value, "zentaoId");
            return (Criteria) this;
        }

        public Criteria andZentaoIdNotEqualTo(String value) {
            addCriterion("zentao_id <>", value, "zentaoId");
            return (Criteria) this;
        }

        public Criteria andZentaoIdGreaterThan(String value) {
            addCriterion("zentao_id >", value, "zentaoId");
            return (Criteria) this;
        }

        public Criteria andZentaoIdGreaterThanOrEqualTo(String value) {
            addCriterion("zentao_id >=", value, "zentaoId");
            return (Criteria) this;
        }

        public Criteria andZentaoIdLessThan(String value) {
            addCriterion("zentao_id <", value, "zentaoId");
            return (Criteria) this;
        }

        public Criteria andZentaoIdLessThanOrEqualTo(String value) {
            addCriterion("zentao_id <=", value, "zentaoId");
            return (Criteria) this;
        }

        public Criteria andZentaoIdLike(String value) {
            addCriterion("zentao_id like", value, "zentaoId");
            return (Criteria) this;
        }

        public Criteria andZentaoIdNotLike(String value) {
            addCriterion("zentao_id not like", value, "zentaoId");
            return (Criteria) this;
        }

        public Criteria andZentaoIdIn(List<String> values) {
            addCriterion("zentao_id in", values, "zentaoId");
            return (Criteria) this;
        }

        public Criteria andZentaoIdNotIn(List<String> values) {
            addCriterion("zentao_id not in", values, "zentaoId");
            return (Criteria) this;
        }

        public Criteria andZentaoIdBetween(String value1, String value2) {
            addCriterion("zentao_id between", value1, value2, "zentaoId");
            return (Criteria) this;
        }

        public Criteria andZentaoIdNotBetween(String value1, String value2) {
            addCriterion("zentao_id not between", value1, value2, "zentaoId");
            return (Criteria) this;
        }

        public Criteria andRepeatableIsNull() {
            addCriterion("`repeatable` is null");
            return (Criteria) this;
        }

        public Criteria andRepeatableIsNotNull() {
            addCriterion("`repeatable` is not null");
            return (Criteria) this;
        }

        public Criteria andRepeatableEqualTo(Boolean value) {
            addCriterion("`repeatable` =", value, "repeatable");
            return (Criteria) this;
        }

        public Criteria andRepeatableNotEqualTo(Boolean value) {
            addCriterion("`repeatable` <>", value, "repeatable");
            return (Criteria) this;
        }

        public Criteria andRepeatableGreaterThan(Boolean value) {
            addCriterion("`repeatable` >", value, "repeatable");
            return (Criteria) this;
        }

        public Criteria andRepeatableGreaterThanOrEqualTo(Boolean value) {
            addCriterion("`repeatable` >=", value, "repeatable");
            return (Criteria) this;
        }

        public Criteria andRepeatableLessThan(Boolean value) {
            addCriterion("`repeatable` <", value, "repeatable");
            return (Criteria) this;
        }

        public Criteria andRepeatableLessThanOrEqualTo(Boolean value) {
            addCriterion("`repeatable` <=", value, "repeatable");
            return (Criteria) this;
        }

        public Criteria andRepeatableIn(List<Boolean> values) {
            addCriterion("`repeatable` in", values, "repeatable");
            return (Criteria) this;
        }

        public Criteria andRepeatableNotIn(List<Boolean> values) {
            addCriterion("`repeatable` not in", values, "repeatable");
            return (Criteria) this;
        }

        public Criteria andRepeatableBetween(Boolean value1, Boolean value2) {
            addCriterion("`repeatable` between", value1, value2, "repeatable");
            return (Criteria) this;
        }

        public Criteria andRepeatableNotBetween(Boolean value1, Boolean value2) {
            addCriterion("`repeatable` not between", value1, value2, "repeatable");
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
