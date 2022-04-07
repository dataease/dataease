package io.dataease.base.domain;

import java.util.ArrayList;
import java.util.List;

public class ChartViewExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public ChartViewExample() {
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

        public Criteria andTitleIsNull() {
            addCriterion("title is null");
            return (Criteria) this;
        }

        public Criteria andTitleIsNotNull() {
            addCriterion("title is not null");
            return (Criteria) this;
        }

        public Criteria andTitleEqualTo(String value) {
            addCriterion("title =", value, "title");
            return (Criteria) this;
        }

        public Criteria andTitleNotEqualTo(String value) {
            addCriterion("title <>", value, "title");
            return (Criteria) this;
        }

        public Criteria andTitleGreaterThan(String value) {
            addCriterion("title >", value, "title");
            return (Criteria) this;
        }

        public Criteria andTitleGreaterThanOrEqualTo(String value) {
            addCriterion("title >=", value, "title");
            return (Criteria) this;
        }

        public Criteria andTitleLessThan(String value) {
            addCriterion("title <", value, "title");
            return (Criteria) this;
        }

        public Criteria andTitleLessThanOrEqualTo(String value) {
            addCriterion("title <=", value, "title");
            return (Criteria) this;
        }

        public Criteria andTitleLike(String value) {
            addCriterion("title like", value, "title");
            return (Criteria) this;
        }

        public Criteria andTitleNotLike(String value) {
            addCriterion("title not like", value, "title");
            return (Criteria) this;
        }

        public Criteria andTitleIn(List<String> values) {
            addCriterion("title in", values, "title");
            return (Criteria) this;
        }

        public Criteria andTitleNotIn(List<String> values) {
            addCriterion("title not in", values, "title");
            return (Criteria) this;
        }

        public Criteria andTitleBetween(String value1, String value2) {
            addCriterion("title between", value1, value2, "title");
            return (Criteria) this;
        }

        public Criteria andTitleNotBetween(String value1, String value2) {
            addCriterion("title not between", value1, value2, "title");
            return (Criteria) this;
        }

        public Criteria andSceneIdIsNull() {
            addCriterion("scene_id is null");
            return (Criteria) this;
        }

        public Criteria andSceneIdIsNotNull() {
            addCriterion("scene_id is not null");
            return (Criteria) this;
        }

        public Criteria andSceneIdEqualTo(String value) {
            addCriterion("scene_id =", value, "sceneId");
            return (Criteria) this;
        }

        public Criteria andSceneIdNotEqualTo(String value) {
            addCriterion("scene_id <>", value, "sceneId");
            return (Criteria) this;
        }

        public Criteria andSceneIdGreaterThan(String value) {
            addCriterion("scene_id >", value, "sceneId");
            return (Criteria) this;
        }

        public Criteria andSceneIdGreaterThanOrEqualTo(String value) {
            addCriterion("scene_id >=", value, "sceneId");
            return (Criteria) this;
        }

        public Criteria andSceneIdLessThan(String value) {
            addCriterion("scene_id <", value, "sceneId");
            return (Criteria) this;
        }

        public Criteria andSceneIdLessThanOrEqualTo(String value) {
            addCriterion("scene_id <=", value, "sceneId");
            return (Criteria) this;
        }

        public Criteria andSceneIdLike(String value) {
            addCriterion("scene_id like", value, "sceneId");
            return (Criteria) this;
        }

        public Criteria andSceneIdNotLike(String value) {
            addCriterion("scene_id not like", value, "sceneId");
            return (Criteria) this;
        }

        public Criteria andSceneIdIn(List<String> values) {
            addCriterion("scene_id in", values, "sceneId");
            return (Criteria) this;
        }

        public Criteria andSceneIdNotIn(List<String> values) {
            addCriterion("scene_id not in", values, "sceneId");
            return (Criteria) this;
        }

        public Criteria andSceneIdBetween(String value1, String value2) {
            addCriterion("scene_id between", value1, value2, "sceneId");
            return (Criteria) this;
        }

        public Criteria andSceneIdNotBetween(String value1, String value2) {
            addCriterion("scene_id not between", value1, value2, "sceneId");
            return (Criteria) this;
        }

        public Criteria andTableIdIsNull() {
            addCriterion("table_id is null");
            return (Criteria) this;
        }

        public Criteria andTableIdIsNotNull() {
            addCriterion("table_id is not null");
            return (Criteria) this;
        }

        public Criteria andTableIdEqualTo(String value) {
            addCriterion("table_id =", value, "tableId");
            return (Criteria) this;
        }

        public Criteria andTableIdNotEqualTo(String value) {
            addCriterion("table_id <>", value, "tableId");
            return (Criteria) this;
        }

        public Criteria andTableIdGreaterThan(String value) {
            addCriterion("table_id >", value, "tableId");
            return (Criteria) this;
        }

        public Criteria andTableIdGreaterThanOrEqualTo(String value) {
            addCriterion("table_id >=", value, "tableId");
            return (Criteria) this;
        }

        public Criteria andTableIdLessThan(String value) {
            addCriterion("table_id <", value, "tableId");
            return (Criteria) this;
        }

        public Criteria andTableIdLessThanOrEqualTo(String value) {
            addCriterion("table_id <=", value, "tableId");
            return (Criteria) this;
        }

        public Criteria andTableIdLike(String value) {
            addCriterion("table_id like", value, "tableId");
            return (Criteria) this;
        }

        public Criteria andTableIdNotLike(String value) {
            addCriterion("table_id not like", value, "tableId");
            return (Criteria) this;
        }

        public Criteria andTableIdIn(List<String> values) {
            addCriterion("table_id in", values, "tableId");
            return (Criteria) this;
        }

        public Criteria andTableIdNotIn(List<String> values) {
            addCriterion("table_id not in", values, "tableId");
            return (Criteria) this;
        }

        public Criteria andTableIdBetween(String value1, String value2) {
            addCriterion("table_id between", value1, value2, "tableId");
            return (Criteria) this;
        }

        public Criteria andTableIdNotBetween(String value1, String value2) {
            addCriterion("table_id not between", value1, value2, "tableId");
            return (Criteria) this;
        }

        public Criteria andTypeIsNull() {
            addCriterion("`type` is null");
            return (Criteria) this;
        }

        public Criteria andTypeIsNotNull() {
            addCriterion("`type` is not null");
            return (Criteria) this;
        }

        public Criteria andTypeEqualTo(String value) {
            addCriterion("`type` =", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotEqualTo(String value) {
            addCriterion("`type` <>", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeGreaterThan(String value) {
            addCriterion("`type` >", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeGreaterThanOrEqualTo(String value) {
            addCriterion("`type` >=", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeLessThan(String value) {
            addCriterion("`type` <", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeLessThanOrEqualTo(String value) {
            addCriterion("`type` <=", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeLike(String value) {
            addCriterion("`type` like", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotLike(String value) {
            addCriterion("`type` not like", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeIn(List<String> values) {
            addCriterion("`type` in", values, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotIn(List<String> values) {
            addCriterion("`type` not in", values, "type");
            return (Criteria) this;
        }

        public Criteria andTypeBetween(String value1, String value2) {
            addCriterion("`type` between", value1, value2, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotBetween(String value1, String value2) {
            addCriterion("`type` not between", value1, value2, "type");
            return (Criteria) this;
        }

        public Criteria andRenderIsNull() {
            addCriterion("render is null");
            return (Criteria) this;
        }

        public Criteria andRenderIsNotNull() {
            addCriterion("render is not null");
            return (Criteria) this;
        }

        public Criteria andRenderEqualTo(String value) {
            addCriterion("render =", value, "render");
            return (Criteria) this;
        }

        public Criteria andRenderNotEqualTo(String value) {
            addCriterion("render <>", value, "render");
            return (Criteria) this;
        }

        public Criteria andRenderGreaterThan(String value) {
            addCriterion("render >", value, "render");
            return (Criteria) this;
        }

        public Criteria andRenderGreaterThanOrEqualTo(String value) {
            addCriterion("render >=", value, "render");
            return (Criteria) this;
        }

        public Criteria andRenderLessThan(String value) {
            addCriterion("render <", value, "render");
            return (Criteria) this;
        }

        public Criteria andRenderLessThanOrEqualTo(String value) {
            addCriterion("render <=", value, "render");
            return (Criteria) this;
        }

        public Criteria andRenderLike(String value) {
            addCriterion("render like", value, "render");
            return (Criteria) this;
        }

        public Criteria andRenderNotLike(String value) {
            addCriterion("render not like", value, "render");
            return (Criteria) this;
        }

        public Criteria andRenderIn(List<String> values) {
            addCriterion("render in", values, "render");
            return (Criteria) this;
        }

        public Criteria andRenderNotIn(List<String> values) {
            addCriterion("render not in", values, "render");
            return (Criteria) this;
        }

        public Criteria andRenderBetween(String value1, String value2) {
            addCriterion("render between", value1, value2, "render");
            return (Criteria) this;
        }

        public Criteria andRenderNotBetween(String value1, String value2) {
            addCriterion("render not between", value1, value2, "render");
            return (Criteria) this;
        }

        public Criteria andResultCountIsNull() {
            addCriterion("result_count is null");
            return (Criteria) this;
        }

        public Criteria andResultCountIsNotNull() {
            addCriterion("result_count is not null");
            return (Criteria) this;
        }

        public Criteria andResultCountEqualTo(Integer value) {
            addCriterion("result_count =", value, "resultCount");
            return (Criteria) this;
        }

        public Criteria andResultCountNotEqualTo(Integer value) {
            addCriterion("result_count <>", value, "resultCount");
            return (Criteria) this;
        }

        public Criteria andResultCountGreaterThan(Integer value) {
            addCriterion("result_count >", value, "resultCount");
            return (Criteria) this;
        }

        public Criteria andResultCountGreaterThanOrEqualTo(Integer value) {
            addCriterion("result_count >=", value, "resultCount");
            return (Criteria) this;
        }

        public Criteria andResultCountLessThan(Integer value) {
            addCriterion("result_count <", value, "resultCount");
            return (Criteria) this;
        }

        public Criteria andResultCountLessThanOrEqualTo(Integer value) {
            addCriterion("result_count <=", value, "resultCount");
            return (Criteria) this;
        }

        public Criteria andResultCountIn(List<Integer> values) {
            addCriterion("result_count in", values, "resultCount");
            return (Criteria) this;
        }

        public Criteria andResultCountNotIn(List<Integer> values) {
            addCriterion("result_count not in", values, "resultCount");
            return (Criteria) this;
        }

        public Criteria andResultCountBetween(Integer value1, Integer value2) {
            addCriterion("result_count between", value1, value2, "resultCount");
            return (Criteria) this;
        }

        public Criteria andResultCountNotBetween(Integer value1, Integer value2) {
            addCriterion("result_count not between", value1, value2, "resultCount");
            return (Criteria) this;
        }

        public Criteria andResultModeIsNull() {
            addCriterion("result_mode is null");
            return (Criteria) this;
        }

        public Criteria andResultModeIsNotNull() {
            addCriterion("result_mode is not null");
            return (Criteria) this;
        }

        public Criteria andResultModeEqualTo(String value) {
            addCriterion("result_mode =", value, "resultMode");
            return (Criteria) this;
        }

        public Criteria andResultModeNotEqualTo(String value) {
            addCriterion("result_mode <>", value, "resultMode");
            return (Criteria) this;
        }

        public Criteria andResultModeGreaterThan(String value) {
            addCriterion("result_mode >", value, "resultMode");
            return (Criteria) this;
        }

        public Criteria andResultModeGreaterThanOrEqualTo(String value) {
            addCriterion("result_mode >=", value, "resultMode");
            return (Criteria) this;
        }

        public Criteria andResultModeLessThan(String value) {
            addCriterion("result_mode <", value, "resultMode");
            return (Criteria) this;
        }

        public Criteria andResultModeLessThanOrEqualTo(String value) {
            addCriterion("result_mode <=", value, "resultMode");
            return (Criteria) this;
        }

        public Criteria andResultModeLike(String value) {
            addCriterion("result_mode like", value, "resultMode");
            return (Criteria) this;
        }

        public Criteria andResultModeNotLike(String value) {
            addCriterion("result_mode not like", value, "resultMode");
            return (Criteria) this;
        }

        public Criteria andResultModeIn(List<String> values) {
            addCriterion("result_mode in", values, "resultMode");
            return (Criteria) this;
        }

        public Criteria andResultModeNotIn(List<String> values) {
            addCriterion("result_mode not in", values, "resultMode");
            return (Criteria) this;
        }

        public Criteria andResultModeBetween(String value1, String value2) {
            addCriterion("result_mode between", value1, value2, "resultMode");
            return (Criteria) this;
        }

        public Criteria andResultModeNotBetween(String value1, String value2) {
            addCriterion("result_mode not between", value1, value2, "resultMode");
            return (Criteria) this;
        }

        public Criteria andCreateByIsNull() {
            addCriterion("create_by is null");
            return (Criteria) this;
        }

        public Criteria andCreateByIsNotNull() {
            addCriterion("create_by is not null");
            return (Criteria) this;
        }

        public Criteria andCreateByEqualTo(String value) {
            addCriterion("create_by =", value, "createBy");
            return (Criteria) this;
        }

        public Criteria andCreateByNotEqualTo(String value) {
            addCriterion("create_by <>", value, "createBy");
            return (Criteria) this;
        }

        public Criteria andCreateByGreaterThan(String value) {
            addCriterion("create_by >", value, "createBy");
            return (Criteria) this;
        }

        public Criteria andCreateByGreaterThanOrEqualTo(String value) {
            addCriterion("create_by >=", value, "createBy");
            return (Criteria) this;
        }

        public Criteria andCreateByLessThan(String value) {
            addCriterion("create_by <", value, "createBy");
            return (Criteria) this;
        }

        public Criteria andCreateByLessThanOrEqualTo(String value) {
            addCriterion("create_by <=", value, "createBy");
            return (Criteria) this;
        }

        public Criteria andCreateByLike(String value) {
            addCriterion("create_by like", value, "createBy");
            return (Criteria) this;
        }

        public Criteria andCreateByNotLike(String value) {
            addCriterion("create_by not like", value, "createBy");
            return (Criteria) this;
        }

        public Criteria andCreateByIn(List<String> values) {
            addCriterion("create_by in", values, "createBy");
            return (Criteria) this;
        }

        public Criteria andCreateByNotIn(List<String> values) {
            addCriterion("create_by not in", values, "createBy");
            return (Criteria) this;
        }

        public Criteria andCreateByBetween(String value1, String value2) {
            addCriterion("create_by between", value1, value2, "createBy");
            return (Criteria) this;
        }

        public Criteria andCreateByNotBetween(String value1, String value2) {
            addCriterion("create_by not between", value1, value2, "createBy");
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

        public Criteria andStylePriorityIsNull() {
            addCriterion("style_priority is null");
            return (Criteria) this;
        }

        public Criteria andStylePriorityIsNotNull() {
            addCriterion("style_priority is not null");
            return (Criteria) this;
        }

        public Criteria andStylePriorityEqualTo(String value) {
            addCriterion("style_priority =", value, "stylePriority");
            return (Criteria) this;
        }

        public Criteria andStylePriorityNotEqualTo(String value) {
            addCriterion("style_priority <>", value, "stylePriority");
            return (Criteria) this;
        }

        public Criteria andStylePriorityGreaterThan(String value) {
            addCriterion("style_priority >", value, "stylePriority");
            return (Criteria) this;
        }

        public Criteria andStylePriorityGreaterThanOrEqualTo(String value) {
            addCriterion("style_priority >=", value, "stylePriority");
            return (Criteria) this;
        }

        public Criteria andStylePriorityLessThan(String value) {
            addCriterion("style_priority <", value, "stylePriority");
            return (Criteria) this;
        }

        public Criteria andStylePriorityLessThanOrEqualTo(String value) {
            addCriterion("style_priority <=", value, "stylePriority");
            return (Criteria) this;
        }

        public Criteria andStylePriorityLike(String value) {
            addCriterion("style_priority like", value, "stylePriority");
            return (Criteria) this;
        }

        public Criteria andStylePriorityNotLike(String value) {
            addCriterion("style_priority not like", value, "stylePriority");
            return (Criteria) this;
        }

        public Criteria andStylePriorityIn(List<String> values) {
            addCriterion("style_priority in", values, "stylePriority");
            return (Criteria) this;
        }

        public Criteria andStylePriorityNotIn(List<String> values) {
            addCriterion("style_priority not in", values, "stylePriority");
            return (Criteria) this;
        }

        public Criteria andStylePriorityBetween(String value1, String value2) {
            addCriterion("style_priority between", value1, value2, "stylePriority");
            return (Criteria) this;
        }

        public Criteria andStylePriorityNotBetween(String value1, String value2) {
            addCriterion("style_priority not between", value1, value2, "stylePriority");
            return (Criteria) this;
        }

        public Criteria andChartTypeIsNull() {
            addCriterion("chart_type is null");
            return (Criteria) this;
        }

        public Criteria andChartTypeIsNotNull() {
            addCriterion("chart_type is not null");
            return (Criteria) this;
        }

        public Criteria andChartTypeEqualTo(String value) {
            addCriterion("chart_type =", value, "chartType");
            return (Criteria) this;
        }

        public Criteria andChartTypeNotEqualTo(String value) {
            addCriterion("chart_type <>", value, "chartType");
            return (Criteria) this;
        }

        public Criteria andChartTypeGreaterThan(String value) {
            addCriterion("chart_type >", value, "chartType");
            return (Criteria) this;
        }

        public Criteria andChartTypeGreaterThanOrEqualTo(String value) {
            addCriterion("chart_type >=", value, "chartType");
            return (Criteria) this;
        }

        public Criteria andChartTypeLessThan(String value) {
            addCriterion("chart_type <", value, "chartType");
            return (Criteria) this;
        }

        public Criteria andChartTypeLessThanOrEqualTo(String value) {
            addCriterion("chart_type <=", value, "chartType");
            return (Criteria) this;
        }

        public Criteria andChartTypeLike(String value) {
            addCriterion("chart_type like", value, "chartType");
            return (Criteria) this;
        }

        public Criteria andChartTypeNotLike(String value) {
            addCriterion("chart_type not like", value, "chartType");
            return (Criteria) this;
        }

        public Criteria andChartTypeIn(List<String> values) {
            addCriterion("chart_type in", values, "chartType");
            return (Criteria) this;
        }

        public Criteria andChartTypeNotIn(List<String> values) {
            addCriterion("chart_type not in", values, "chartType");
            return (Criteria) this;
        }

        public Criteria andChartTypeBetween(String value1, String value2) {
            addCriterion("chart_type between", value1, value2, "chartType");
            return (Criteria) this;
        }

        public Criteria andChartTypeNotBetween(String value1, String value2) {
            addCriterion("chart_type not between", value1, value2, "chartType");
            return (Criteria) this;
        }

        public Criteria andIsPluginIsNull() {
            addCriterion("is_plugin is null");
            return (Criteria) this;
        }

        public Criteria andIsPluginIsNotNull() {
            addCriterion("is_plugin is not null");
            return (Criteria) this;
        }

        public Criteria andIsPluginEqualTo(Boolean value) {
            addCriterion("is_plugin =", value, "isPlugin");
            return (Criteria) this;
        }

        public Criteria andIsPluginNotEqualTo(Boolean value) {
            addCriterion("is_plugin <>", value, "isPlugin");
            return (Criteria) this;
        }

        public Criteria andIsPluginGreaterThan(Boolean value) {
            addCriterion("is_plugin >", value, "isPlugin");
            return (Criteria) this;
        }

        public Criteria andIsPluginGreaterThanOrEqualTo(Boolean value) {
            addCriterion("is_plugin >=", value, "isPlugin");
            return (Criteria) this;
        }

        public Criteria andIsPluginLessThan(Boolean value) {
            addCriterion("is_plugin <", value, "isPlugin");
            return (Criteria) this;
        }

        public Criteria andIsPluginLessThanOrEqualTo(Boolean value) {
            addCriterion("is_plugin <=", value, "isPlugin");
            return (Criteria) this;
        }

        public Criteria andIsPluginIn(List<Boolean> values) {
            addCriterion("is_plugin in", values, "isPlugin");
            return (Criteria) this;
        }

        public Criteria andIsPluginNotIn(List<Boolean> values) {
            addCriterion("is_plugin not in", values, "isPlugin");
            return (Criteria) this;
        }

        public Criteria andIsPluginBetween(Boolean value1, Boolean value2) {
            addCriterion("is_plugin between", value1, value2, "isPlugin");
            return (Criteria) this;
        }

        public Criteria andIsPluginNotBetween(Boolean value1, Boolean value2) {
            addCriterion("is_plugin not between", value1, value2, "isPlugin");
            return (Criteria) this;
        }

        public Criteria andDataFromIsNull() {
            addCriterion("data_from is null");
            return (Criteria) this;
        }

        public Criteria andDataFromIsNotNull() {
            addCriterion("data_from is not null");
            return (Criteria) this;
        }

        public Criteria andDataFromEqualTo(String value) {
            addCriterion("data_from =", value, "dataFrom");
            return (Criteria) this;
        }

        public Criteria andDataFromNotEqualTo(String value) {
            addCriterion("data_from <>", value, "dataFrom");
            return (Criteria) this;
        }

        public Criteria andDataFromGreaterThan(String value) {
            addCriterion("data_from >", value, "dataFrom");
            return (Criteria) this;
        }

        public Criteria andDataFromGreaterThanOrEqualTo(String value) {
            addCriterion("data_from >=", value, "dataFrom");
            return (Criteria) this;
        }

        public Criteria andDataFromLessThan(String value) {
            addCriterion("data_from <", value, "dataFrom");
            return (Criteria) this;
        }

        public Criteria andDataFromLessThanOrEqualTo(String value) {
            addCriterion("data_from <=", value, "dataFrom");
            return (Criteria) this;
        }

        public Criteria andDataFromLike(String value) {
            addCriterion("data_from like", value, "dataFrom");
            return (Criteria) this;
        }

        public Criteria andDataFromNotLike(String value) {
            addCriterion("data_from not like", value, "dataFrom");
            return (Criteria) this;
        }

        public Criteria andDataFromIn(List<String> values) {
            addCriterion("data_from in", values, "dataFrom");
            return (Criteria) this;
        }

        public Criteria andDataFromNotIn(List<String> values) {
            addCriterion("data_from not in", values, "dataFrom");
            return (Criteria) this;
        }

        public Criteria andDataFromBetween(String value1, String value2) {
            addCriterion("data_from between", value1, value2, "dataFrom");
            return (Criteria) this;
        }

        public Criteria andDataFromNotBetween(String value1, String value2) {
            addCriterion("data_from not between", value1, value2, "dataFrom");
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