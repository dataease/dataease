package io.dataease.ext.query;

import io.dataease.controller.sys.base.ConditionEntity;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class GridExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    protected String extendCondition;

    public GridExample() {
        oredCriteria = new ArrayList<Criteria>();
    }

    public String getExtendCondition() {
        return extendCondition;
    }

    public void setExtendCondition(String extendCondition) {
        this.extendCondition = extendCondition;
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

        protected void addNotNullCriterion(String condition) {
            criteria.add(new Criterion(condition, null));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addSqlCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            Criterion criterion = new Criterion(condition, value);
            criterion.sqlValue = true;
            criteria.add(criterion);
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }



        public Criteria addCondition(ConditionEntity conditionEntity){
            String field = conditionEntity.getField();
            Object value = conditionEntity.getValue();
            String operator = conditionEntity.getOperator();
            if (StringUtils.isEmpty(operator))
                operator = "like";
            switch (operator){
                case "eq":
                    addCriterion(field+" = ", value, field);
                    break;
                case "ne":
                    addCriterion(field+" <> ", value, field);
                    break;
                case "like":
                    addCriterion(field+" like ", "%"+value+"%", field);
                    break;
                case "not like":
                    addCriterion(field+" not like ", "%"+value+"%", field);
                    break;
                case "in":
                    List<Object> invalues = (List<Object>)value;
                    addCriterion(field+" in", invalues, field);
                    break;
                case "not in":
                    List<Object> notinvalues = (List<Object>)value;
                    addCriterion(field+" not in", notinvalues, field);
                    break;
                case "between":
                    List<Object> values = (List<Object>)value;
                    Object v1 = values.get(0);
                    Object v2 = values.get(1);
                    addCriterion(field+" between", v1, v2, field);
                    break;
                case "gt":
                    addCriterion(field+" > ", value, field);
                    break;
                case "ge":
                    addCriterion(field+" >= ", value, field);
                    break;
                case "lt":
                    addCriterion(field+" < ", value, field);
                    break;
                case "le":
                    addCriterion(field+" <= ", value, field);
                    break;
                case "not null":
                    addNotNullCriterion(field + " is not null ");
                    break;
                case "extra":
                    addCriterion(field);
                    break;
                case "sql in":
                    addCriterion(field+" in ", value, field);
                    break;
            }
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

        public boolean isSqlValue() {
            return sqlValue;
        }

        public void setSqlValue(boolean sqlValue) {
            this.sqlValue = sqlValue;
        }

        private boolean sqlValue;

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
            if(value == null){
                this.noValue = true;
            }else if (value instanceof List<?>) {
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
