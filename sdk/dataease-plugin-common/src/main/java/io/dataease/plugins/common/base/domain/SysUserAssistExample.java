package io.dataease.plugins.common.base.domain;

import java.util.ArrayList;
import java.util.List;

public class SysUserAssistExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public SysUserAssistExample() {
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

        public Criteria andUserIdIsNull() {
            addCriterion("user_id is null");
            return (Criteria) this;
        }

        public Criteria andUserIdIsNotNull() {
            addCriterion("user_id is not null");
            return (Criteria) this;
        }

        public Criteria andUserIdEqualTo(Long value) {
            addCriterion("user_id =", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdNotEqualTo(Long value) {
            addCriterion("user_id <>", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdGreaterThan(Long value) {
            addCriterion("user_id >", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdGreaterThanOrEqualTo(Long value) {
            addCriterion("user_id >=", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdLessThan(Long value) {
            addCriterion("user_id <", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdLessThanOrEqualTo(Long value) {
            addCriterion("user_id <=", value, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdIn(List<Long> values) {
            addCriterion("user_id in", values, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdNotIn(List<Long> values) {
            addCriterion("user_id not in", values, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdBetween(Long value1, Long value2) {
            addCriterion("user_id between", value1, value2, "userId");
            return (Criteria) this;
        }

        public Criteria andUserIdNotBetween(Long value1, Long value2) {
            addCriterion("user_id not between", value1, value2, "userId");
            return (Criteria) this;
        }

        public Criteria andNeedFirstNotiIsNull() {
            addCriterion("need_first_noti is null");
            return (Criteria) this;
        }

        public Criteria andNeedFirstNotiIsNotNull() {
            addCriterion("need_first_noti is not null");
            return (Criteria) this;
        }

        public Criteria andNeedFirstNotiEqualTo(Boolean value) {
            addCriterion("need_first_noti =", value, "needFirstNoti");
            return (Criteria) this;
        }

        public Criteria andNeedFirstNotiNotEqualTo(Boolean value) {
            addCriterion("need_first_noti <>", value, "needFirstNoti");
            return (Criteria) this;
        }

        public Criteria andNeedFirstNotiGreaterThan(Boolean value) {
            addCriterion("need_first_noti >", value, "needFirstNoti");
            return (Criteria) this;
        }

        public Criteria andNeedFirstNotiGreaterThanOrEqualTo(Boolean value) {
            addCriterion("need_first_noti >=", value, "needFirstNoti");
            return (Criteria) this;
        }

        public Criteria andNeedFirstNotiLessThan(Boolean value) {
            addCriterion("need_first_noti <", value, "needFirstNoti");
            return (Criteria) this;
        }

        public Criteria andNeedFirstNotiLessThanOrEqualTo(Boolean value) {
            addCriterion("need_first_noti <=", value, "needFirstNoti");
            return (Criteria) this;
        }

        public Criteria andNeedFirstNotiIn(List<Boolean> values) {
            addCriterion("need_first_noti in", values, "needFirstNoti");
            return (Criteria) this;
        }

        public Criteria andNeedFirstNotiNotIn(List<Boolean> values) {
            addCriterion("need_first_noti not in", values, "needFirstNoti");
            return (Criteria) this;
        }

        public Criteria andNeedFirstNotiBetween(Boolean value1, Boolean value2) {
            addCriterion("need_first_noti between", value1, value2, "needFirstNoti");
            return (Criteria) this;
        }

        public Criteria andNeedFirstNotiNotBetween(Boolean value1, Boolean value2) {
            addCriterion("need_first_noti not between", value1, value2, "needFirstNoti");
            return (Criteria) this;
        }

        public Criteria andWecomIdIsNull() {
            addCriterion("wecom_id is null");
            return (Criteria) this;
        }

        public Criteria andWecomIdIsNotNull() {
            addCriterion("wecom_id is not null");
            return (Criteria) this;
        }

        public Criteria andWecomIdEqualTo(String value) {
            addCriterion("wecom_id =", value, "wecomId");
            return (Criteria) this;
        }

        public Criteria andWecomIdNotEqualTo(String value) {
            addCriterion("wecom_id <>", value, "wecomId");
            return (Criteria) this;
        }

        public Criteria andWecomIdGreaterThan(String value) {
            addCriterion("wecom_id >", value, "wecomId");
            return (Criteria) this;
        }

        public Criteria andWecomIdGreaterThanOrEqualTo(String value) {
            addCriterion("wecom_id >=", value, "wecomId");
            return (Criteria) this;
        }

        public Criteria andWecomIdLessThan(String value) {
            addCriterion("wecom_id <", value, "wecomId");
            return (Criteria) this;
        }

        public Criteria andWecomIdLessThanOrEqualTo(String value) {
            addCriterion("wecom_id <=", value, "wecomId");
            return (Criteria) this;
        }

        public Criteria andWecomIdLike(String value) {
            addCriterion("wecom_id like", value, "wecomId");
            return (Criteria) this;
        }

        public Criteria andWecomIdNotLike(String value) {
            addCriterion("wecom_id not like", value, "wecomId");
            return (Criteria) this;
        }

        public Criteria andWecomIdIn(List<String> values) {
            addCriterion("wecom_id in", values, "wecomId");
            return (Criteria) this;
        }

        public Criteria andWecomIdNotIn(List<String> values) {
            addCriterion("wecom_id not in", values, "wecomId");
            return (Criteria) this;
        }

        public Criteria andWecomIdBetween(String value1, String value2) {
            addCriterion("wecom_id between", value1, value2, "wecomId");
            return (Criteria) this;
        }

        public Criteria andWecomIdNotBetween(String value1, String value2) {
            addCriterion("wecom_id not between", value1, value2, "wecomId");
            return (Criteria) this;
        }

        public Criteria andDingtalkIdIsNull() {
            addCriterion("dingtalk_id is null");
            return (Criteria) this;
        }

        public Criteria andDingtalkIdIsNotNull() {
            addCriterion("dingtalk_id is not null");
            return (Criteria) this;
        }

        public Criteria andDingtalkIdEqualTo(String value) {
            addCriterion("dingtalk_id =", value, "dingtalkId");
            return (Criteria) this;
        }

        public Criteria andDingtalkIdNotEqualTo(String value) {
            addCriterion("dingtalk_id <>", value, "dingtalkId");
            return (Criteria) this;
        }

        public Criteria andDingtalkIdGreaterThan(String value) {
            addCriterion("dingtalk_id >", value, "dingtalkId");
            return (Criteria) this;
        }

        public Criteria andDingtalkIdGreaterThanOrEqualTo(String value) {
            addCriterion("dingtalk_id >=", value, "dingtalkId");
            return (Criteria) this;
        }

        public Criteria andDingtalkIdLessThan(String value) {
            addCriterion("dingtalk_id <", value, "dingtalkId");
            return (Criteria) this;
        }

        public Criteria andDingtalkIdLessThanOrEqualTo(String value) {
            addCriterion("dingtalk_id <=", value, "dingtalkId");
            return (Criteria) this;
        }

        public Criteria andDingtalkIdLike(String value) {
            addCriterion("dingtalk_id like", value, "dingtalkId");
            return (Criteria) this;
        }

        public Criteria andDingtalkIdNotLike(String value) {
            addCriterion("dingtalk_id not like", value, "dingtalkId");
            return (Criteria) this;
        }

        public Criteria andDingtalkIdIn(List<String> values) {
            addCriterion("dingtalk_id in", values, "dingtalkId");
            return (Criteria) this;
        }

        public Criteria andDingtalkIdNotIn(List<String> values) {
            addCriterion("dingtalk_id not in", values, "dingtalkId");
            return (Criteria) this;
        }

        public Criteria andDingtalkIdBetween(String value1, String value2) {
            addCriterion("dingtalk_id between", value1, value2, "dingtalkId");
            return (Criteria) this;
        }

        public Criteria andDingtalkIdNotBetween(String value1, String value2) {
            addCriterion("dingtalk_id not between", value1, value2, "dingtalkId");
            return (Criteria) this;
        }

        public Criteria andLarkIdIsNull() {
            addCriterion("lark_id is null");
            return (Criteria) this;
        }

        public Criteria andLarkIdIsNotNull() {
            addCriterion("lark_id is not null");
            return (Criteria) this;
        }

        public Criteria andLarkIdEqualTo(String value) {
            addCriterion("lark_id =", value, "larkId");
            return (Criteria) this;
        }

        public Criteria andLarkIdNotEqualTo(String value) {
            addCriterion("lark_id <>", value, "larkId");
            return (Criteria) this;
        }

        public Criteria andLarkIdGreaterThan(String value) {
            addCriterion("lark_id >", value, "larkId");
            return (Criteria) this;
        }

        public Criteria andLarkIdGreaterThanOrEqualTo(String value) {
            addCriterion("lark_id >=", value, "larkId");
            return (Criteria) this;
        }

        public Criteria andLarkIdLessThan(String value) {
            addCriterion("lark_id <", value, "larkId");
            return (Criteria) this;
        }

        public Criteria andLarkIdLessThanOrEqualTo(String value) {
            addCriterion("lark_id <=", value, "larkId");
            return (Criteria) this;
        }

        public Criteria andLarkIdLike(String value) {
            addCriterion("lark_id like", value, "larkId");
            return (Criteria) this;
        }

        public Criteria andLarkIdNotLike(String value) {
            addCriterion("lark_id not like", value, "larkId");
            return (Criteria) this;
        }

        public Criteria andLarkIdIn(List<String> values) {
            addCriterion("lark_id in", values, "larkId");
            return (Criteria) this;
        }

        public Criteria andLarkIdNotIn(List<String> values) {
            addCriterion("lark_id not in", values, "larkId");
            return (Criteria) this;
        }

        public Criteria andLarkIdBetween(String value1, String value2) {
            addCriterion("lark_id between", value1, value2, "larkId");
            return (Criteria) this;
        }

        public Criteria andLarkIdNotBetween(String value1, String value2) {
            addCriterion("lark_id not between", value1, value2, "larkId");
            return (Criteria) this;
        }

        public Criteria andLarksuiteIdIsNull() {
            addCriterion("larksuite_id is null");
            return (Criteria) this;
        }

        public Criteria andLarksuiteIdIsNotNull() {
            addCriterion("larksuite_id is not null");
            return (Criteria) this;
        }

        public Criteria andLarksuiteIdEqualTo(String value) {
            addCriterion("larksuite_id =", value, "larksuiteId");
            return (Criteria) this;
        }

        public Criteria andLarksuiteIdNotEqualTo(String value) {
            addCriterion("larksuite_id <>", value, "larksuiteId");
            return (Criteria) this;
        }

        public Criteria andLarksuiteIdGreaterThan(String value) {
            addCriterion("larksuite_id >", value, "larksuiteId");
            return (Criteria) this;
        }

        public Criteria andLarksuiteIdGreaterThanOrEqualTo(String value) {
            addCriterion("larksuite_id >=", value, "larksuiteId");
            return (Criteria) this;
        }

        public Criteria andLarksuiteIdLessThan(String value) {
            addCriterion("larksuite_id <", value, "larksuiteId");
            return (Criteria) this;
        }

        public Criteria andLarksuiteIdLessThanOrEqualTo(String value) {
            addCriterion("larksuite_id <=", value, "larksuiteId");
            return (Criteria) this;
        }

        public Criteria andLarksuiteIdLike(String value) {
            addCriterion("larksuite_id like", value, "larksuiteId");
            return (Criteria) this;
        }

        public Criteria andLarksuiteIdNotLike(String value) {
            addCriterion("larksuite_id not like", value, "larksuiteId");
            return (Criteria) this;
        }

        public Criteria andLarksuiteIdIn(List<String> values) {
            addCriterion("larksuite_id in", values, "larksuiteId");
            return (Criteria) this;
        }

        public Criteria andLarksuiteIdNotIn(List<String> values) {
            addCriterion("larksuite_id not in", values, "larksuiteId");
            return (Criteria) this;
        }

        public Criteria andLarksuiteIdBetween(String value1, String value2) {
            addCriterion("larksuite_id between", value1, value2, "larksuiteId");
            return (Criteria) this;
        }

        public Criteria andLarksuiteIdNotBetween(String value1, String value2) {
            addCriterion("larksuite_id not between", value1, value2, "larksuiteId");
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