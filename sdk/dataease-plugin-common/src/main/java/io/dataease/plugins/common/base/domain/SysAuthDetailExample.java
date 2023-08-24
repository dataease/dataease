package io.dataease.plugins.common.base.domain;

import java.util.ArrayList;
import java.util.List;

public class SysAuthDetailExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public SysAuthDetailExample() {
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

        public Criteria andAuthIdIsNull() {
            addCriterion("auth_id is null");
            return (Criteria) this;
        }

        public Criteria andAuthIdIsNotNull() {
            addCriterion("auth_id is not null");
            return (Criteria) this;
        }

        public Criteria andAuthIdEqualTo(String value) {
            addCriterion("auth_id =", value, "authId");
            return (Criteria) this;
        }

        public Criteria andAuthIdNotEqualTo(String value) {
            addCriterion("auth_id <>", value, "authId");
            return (Criteria) this;
        }

        public Criteria andAuthIdGreaterThan(String value) {
            addCriterion("auth_id >", value, "authId");
            return (Criteria) this;
        }

        public Criteria andAuthIdGreaterThanOrEqualTo(String value) {
            addCriterion("auth_id >=", value, "authId");
            return (Criteria) this;
        }

        public Criteria andAuthIdLessThan(String value) {
            addCriterion("auth_id <", value, "authId");
            return (Criteria) this;
        }

        public Criteria andAuthIdLessThanOrEqualTo(String value) {
            addCriterion("auth_id <=", value, "authId");
            return (Criteria) this;
        }

        public Criteria andAuthIdLike(String value) {
            addCriterion("auth_id like", value, "authId");
            return (Criteria) this;
        }

        public Criteria andAuthIdNotLike(String value) {
            addCriterion("auth_id not like", value, "authId");
            return (Criteria) this;
        }

        public Criteria andAuthIdIn(List<String> values) {
            addCriterion("auth_id in", values, "authId");
            return (Criteria) this;
        }

        public Criteria andAuthIdNotIn(List<String> values) {
            addCriterion("auth_id not in", values, "authId");
            return (Criteria) this;
        }

        public Criteria andAuthIdBetween(String value1, String value2) {
            addCriterion("auth_id between", value1, value2, "authId");
            return (Criteria) this;
        }

        public Criteria andAuthIdNotBetween(String value1, String value2) {
            addCriterion("auth_id not between", value1, value2, "authId");
            return (Criteria) this;
        }

        public Criteria andPrivilegeNameIsNull() {
            addCriterion("privilege_name is null");
            return (Criteria) this;
        }

        public Criteria andPrivilegeNameIsNotNull() {
            addCriterion("privilege_name is not null");
            return (Criteria) this;
        }

        public Criteria andPrivilegeNameEqualTo(String value) {
            addCriterion("privilege_name =", value, "privilegeName");
            return (Criteria) this;
        }

        public Criteria andPrivilegeNameNotEqualTo(String value) {
            addCriterion("privilege_name <>", value, "privilegeName");
            return (Criteria) this;
        }

        public Criteria andPrivilegeNameGreaterThan(String value) {
            addCriterion("privilege_name >", value, "privilegeName");
            return (Criteria) this;
        }

        public Criteria andPrivilegeNameGreaterThanOrEqualTo(String value) {
            addCriterion("privilege_name >=", value, "privilegeName");
            return (Criteria) this;
        }

        public Criteria andPrivilegeNameLessThan(String value) {
            addCriterion("privilege_name <", value, "privilegeName");
            return (Criteria) this;
        }

        public Criteria andPrivilegeNameLessThanOrEqualTo(String value) {
            addCriterion("privilege_name <=", value, "privilegeName");
            return (Criteria) this;
        }

        public Criteria andPrivilegeNameLike(String value) {
            addCriterion("privilege_name like", value, "privilegeName");
            return (Criteria) this;
        }

        public Criteria andPrivilegeNameNotLike(String value) {
            addCriterion("privilege_name not like", value, "privilegeName");
            return (Criteria) this;
        }

        public Criteria andPrivilegeNameIn(List<String> values) {
            addCriterion("privilege_name in", values, "privilegeName");
            return (Criteria) this;
        }

        public Criteria andPrivilegeNameNotIn(List<String> values) {
            addCriterion("privilege_name not in", values, "privilegeName");
            return (Criteria) this;
        }

        public Criteria andPrivilegeNameBetween(String value1, String value2) {
            addCriterion("privilege_name between", value1, value2, "privilegeName");
            return (Criteria) this;
        }

        public Criteria andPrivilegeNameNotBetween(String value1, String value2) {
            addCriterion("privilege_name not between", value1, value2, "privilegeName");
            return (Criteria) this;
        }

        public Criteria andPrivilegeTypeIsNull() {
            addCriterion("privilege_type is null");
            return (Criteria) this;
        }

        public Criteria andPrivilegeTypeIsNotNull() {
            addCriterion("privilege_type is not null");
            return (Criteria) this;
        }

        public Criteria andPrivilegeTypeEqualTo(Integer value) {
            addCriterion("privilege_type =", value, "privilegeType");
            return (Criteria) this;
        }

        public Criteria andPrivilegeTypeNotEqualTo(Integer value) {
            addCriterion("privilege_type <>", value, "privilegeType");
            return (Criteria) this;
        }

        public Criteria andPrivilegeTypeGreaterThan(Integer value) {
            addCriterion("privilege_type >", value, "privilegeType");
            return (Criteria) this;
        }

        public Criteria andPrivilegeTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("privilege_type >=", value, "privilegeType");
            return (Criteria) this;
        }

        public Criteria andPrivilegeTypeLessThan(Integer value) {
            addCriterion("privilege_type <", value, "privilegeType");
            return (Criteria) this;
        }

        public Criteria andPrivilegeTypeLessThanOrEqualTo(Integer value) {
            addCriterion("privilege_type <=", value, "privilegeType");
            return (Criteria) this;
        }

        public Criteria andPrivilegeTypeIn(List<Integer> values) {
            addCriterion("privilege_type in", values, "privilegeType");
            return (Criteria) this;
        }

        public Criteria andPrivilegeTypeNotIn(List<Integer> values) {
            addCriterion("privilege_type not in", values, "privilegeType");
            return (Criteria) this;
        }

        public Criteria andPrivilegeTypeBetween(Integer value1, Integer value2) {
            addCriterion("privilege_type between", value1, value2, "privilegeType");
            return (Criteria) this;
        }

        public Criteria andPrivilegeTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("privilege_type not between", value1, value2, "privilegeType");
            return (Criteria) this;
        }

        public Criteria andPrivilegeValueIsNull() {
            addCriterion("privilege_value is null");
            return (Criteria) this;
        }

        public Criteria andPrivilegeValueIsNotNull() {
            addCriterion("privilege_value is not null");
            return (Criteria) this;
        }

        public Criteria andPrivilegeValueEqualTo(Integer value) {
            addCriterion("privilege_value =", value, "privilegeValue");
            return (Criteria) this;
        }

        public Criteria andPrivilegeValueNotEqualTo(Integer value) {
            addCriterion("privilege_value <>", value, "privilegeValue");
            return (Criteria) this;
        }

        public Criteria andPrivilegeValueGreaterThan(Integer value) {
            addCriterion("privilege_value >", value, "privilegeValue");
            return (Criteria) this;
        }

        public Criteria andPrivilegeValueGreaterThanOrEqualTo(Integer value) {
            addCriterion("privilege_value >=", value, "privilegeValue");
            return (Criteria) this;
        }

        public Criteria andPrivilegeValueLessThan(Integer value) {
            addCriterion("privilege_value <", value, "privilegeValue");
            return (Criteria) this;
        }

        public Criteria andPrivilegeValueLessThanOrEqualTo(Integer value) {
            addCriterion("privilege_value <=", value, "privilegeValue");
            return (Criteria) this;
        }

        public Criteria andPrivilegeValueIn(List<Integer> values) {
            addCriterion("privilege_value in", values, "privilegeValue");
            return (Criteria) this;
        }

        public Criteria andPrivilegeValueNotIn(List<Integer> values) {
            addCriterion("privilege_value not in", values, "privilegeValue");
            return (Criteria) this;
        }

        public Criteria andPrivilegeValueBetween(Integer value1, Integer value2) {
            addCriterion("privilege_value between", value1, value2, "privilegeValue");
            return (Criteria) this;
        }

        public Criteria andPrivilegeValueNotBetween(Integer value1, Integer value2) {
            addCriterion("privilege_value not between", value1, value2, "privilegeValue");
            return (Criteria) this;
        }

        public Criteria andPrivilegeExtendIsNull() {
            addCriterion("privilege_extend is null");
            return (Criteria) this;
        }

        public Criteria andPrivilegeExtendIsNotNull() {
            addCriterion("privilege_extend is not null");
            return (Criteria) this;
        }

        public Criteria andPrivilegeExtendEqualTo(String value) {
            addCriterion("privilege_extend =", value, "privilegeExtend");
            return (Criteria) this;
        }

        public Criteria andPrivilegeExtendNotEqualTo(String value) {
            addCriterion("privilege_extend <>", value, "privilegeExtend");
            return (Criteria) this;
        }

        public Criteria andPrivilegeExtendGreaterThan(String value) {
            addCriterion("privilege_extend >", value, "privilegeExtend");
            return (Criteria) this;
        }

        public Criteria andPrivilegeExtendGreaterThanOrEqualTo(String value) {
            addCriterion("privilege_extend >=", value, "privilegeExtend");
            return (Criteria) this;
        }

        public Criteria andPrivilegeExtendLessThan(String value) {
            addCriterion("privilege_extend <", value, "privilegeExtend");
            return (Criteria) this;
        }

        public Criteria andPrivilegeExtendLessThanOrEqualTo(String value) {
            addCriterion("privilege_extend <=", value, "privilegeExtend");
            return (Criteria) this;
        }

        public Criteria andPrivilegeExtendLike(String value) {
            addCriterion("privilege_extend like", value, "privilegeExtend");
            return (Criteria) this;
        }

        public Criteria andPrivilegeExtendNotLike(String value) {
            addCriterion("privilege_extend not like", value, "privilegeExtend");
            return (Criteria) this;
        }

        public Criteria andPrivilegeExtendIn(List<String> values) {
            addCriterion("privilege_extend in", values, "privilegeExtend");
            return (Criteria) this;
        }

        public Criteria andPrivilegeExtendNotIn(List<String> values) {
            addCriterion("privilege_extend not in", values, "privilegeExtend");
            return (Criteria) this;
        }

        public Criteria andPrivilegeExtendBetween(String value1, String value2) {
            addCriterion("privilege_extend between", value1, value2, "privilegeExtend");
            return (Criteria) this;
        }

        public Criteria andPrivilegeExtendNotBetween(String value1, String value2) {
            addCriterion("privilege_extend not between", value1, value2, "privilegeExtend");
            return (Criteria) this;
        }

        public Criteria andRemarkIsNull() {
            addCriterion("remark is null");
            return (Criteria) this;
        }

        public Criteria andRemarkIsNotNull() {
            addCriterion("remark is not null");
            return (Criteria) this;
        }

        public Criteria andRemarkEqualTo(String value) {
            addCriterion("remark =", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkNotEqualTo(String value) {
            addCriterion("remark <>", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkGreaterThan(String value) {
            addCriterion("remark >", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkGreaterThanOrEqualTo(String value) {
            addCriterion("remark >=", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkLessThan(String value) {
            addCriterion("remark <", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkLessThanOrEqualTo(String value) {
            addCriterion("remark <=", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkLike(String value) {
            addCriterion("remark like", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkNotLike(String value) {
            addCriterion("remark not like", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkIn(List<String> values) {
            addCriterion("remark in", values, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkNotIn(List<String> values) {
            addCriterion("remark not in", values, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkBetween(String value1, String value2) {
            addCriterion("remark between", value1, value2, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkNotBetween(String value1, String value2) {
            addCriterion("remark not between", value1, value2, "remark");
            return (Criteria) this;
        }

        public Criteria andCreateUserIsNull() {
            addCriterion("create_user is null");
            return (Criteria) this;
        }

        public Criteria andCreateUserIsNotNull() {
            addCriterion("create_user is not null");
            return (Criteria) this;
        }

        public Criteria andCreateUserEqualTo(String value) {
            addCriterion("create_user =", value, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserNotEqualTo(String value) {
            addCriterion("create_user <>", value, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserGreaterThan(String value) {
            addCriterion("create_user >", value, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserGreaterThanOrEqualTo(String value) {
            addCriterion("create_user >=", value, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserLessThan(String value) {
            addCriterion("create_user <", value, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserLessThanOrEqualTo(String value) {
            addCriterion("create_user <=", value, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserLike(String value) {
            addCriterion("create_user like", value, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserNotLike(String value) {
            addCriterion("create_user not like", value, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserIn(List<String> values) {
            addCriterion("create_user in", values, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserNotIn(List<String> values) {
            addCriterion("create_user not in", values, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserBetween(String value1, String value2) {
            addCriterion("create_user between", value1, value2, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserNotBetween(String value1, String value2) {
            addCriterion("create_user not between", value1, value2, "createUser");
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