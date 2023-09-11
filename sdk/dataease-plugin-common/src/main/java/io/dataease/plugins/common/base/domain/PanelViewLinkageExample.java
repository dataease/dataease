package io.dataease.plugins.common.base.domain;

import java.util.ArrayList;
import java.util.List;

public class PanelViewLinkageExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public PanelViewLinkageExample() {
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

        public Criteria andTargetViewIdIsNull() {
            addCriterion("target_view_id is null");
            return (Criteria) this;
        }

        public Criteria andTargetViewIdIsNotNull() {
            addCriterion("target_view_id is not null");
            return (Criteria) this;
        }

        public Criteria andTargetViewIdEqualTo(String value) {
            addCriterion("target_view_id =", value, "targetViewId");
            return (Criteria) this;
        }

        public Criteria andTargetViewIdNotEqualTo(String value) {
            addCriterion("target_view_id <>", value, "targetViewId");
            return (Criteria) this;
        }

        public Criteria andTargetViewIdGreaterThan(String value) {
            addCriterion("target_view_id >", value, "targetViewId");
            return (Criteria) this;
        }

        public Criteria andTargetViewIdGreaterThanOrEqualTo(String value) {
            addCriterion("target_view_id >=", value, "targetViewId");
            return (Criteria) this;
        }

        public Criteria andTargetViewIdLessThan(String value) {
            addCriterion("target_view_id <", value, "targetViewId");
            return (Criteria) this;
        }

        public Criteria andTargetViewIdLessThanOrEqualTo(String value) {
            addCriterion("target_view_id <=", value, "targetViewId");
            return (Criteria) this;
        }

        public Criteria andTargetViewIdLike(String value) {
            addCriterion("target_view_id like", value, "targetViewId");
            return (Criteria) this;
        }

        public Criteria andTargetViewIdNotLike(String value) {
            addCriterion("target_view_id not like", value, "targetViewId");
            return (Criteria) this;
        }

        public Criteria andTargetViewIdIn(List<String> values) {
            addCriterion("target_view_id in", values, "targetViewId");
            return (Criteria) this;
        }

        public Criteria andTargetViewIdNotIn(List<String> values) {
            addCriterion("target_view_id not in", values, "targetViewId");
            return (Criteria) this;
        }

        public Criteria andTargetViewIdBetween(String value1, String value2) {
            addCriterion("target_view_id between", value1, value2, "targetViewId");
            return (Criteria) this;
        }

        public Criteria andTargetViewIdNotBetween(String value1, String value2) {
            addCriterion("target_view_id not between", value1, value2, "targetViewId");
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

        public Criteria andUpdatePeopleIsNull() {
            addCriterion("update_people is null");
            return (Criteria) this;
        }

        public Criteria andUpdatePeopleIsNotNull() {
            addCriterion("update_people is not null");
            return (Criteria) this;
        }

        public Criteria andUpdatePeopleEqualTo(String value) {
            addCriterion("update_people =", value, "updatePeople");
            return (Criteria) this;
        }

        public Criteria andUpdatePeopleNotEqualTo(String value) {
            addCriterion("update_people <>", value, "updatePeople");
            return (Criteria) this;
        }

        public Criteria andUpdatePeopleGreaterThan(String value) {
            addCriterion("update_people >", value, "updatePeople");
            return (Criteria) this;
        }

        public Criteria andUpdatePeopleGreaterThanOrEqualTo(String value) {
            addCriterion("update_people >=", value, "updatePeople");
            return (Criteria) this;
        }

        public Criteria andUpdatePeopleLessThan(String value) {
            addCriterion("update_people <", value, "updatePeople");
            return (Criteria) this;
        }

        public Criteria andUpdatePeopleLessThanOrEqualTo(String value) {
            addCriterion("update_people <=", value, "updatePeople");
            return (Criteria) this;
        }

        public Criteria andUpdatePeopleLike(String value) {
            addCriterion("update_people like", value, "updatePeople");
            return (Criteria) this;
        }

        public Criteria andUpdatePeopleNotLike(String value) {
            addCriterion("update_people not like", value, "updatePeople");
            return (Criteria) this;
        }

        public Criteria andUpdatePeopleIn(List<String> values) {
            addCriterion("update_people in", values, "updatePeople");
            return (Criteria) this;
        }

        public Criteria andUpdatePeopleNotIn(List<String> values) {
            addCriterion("update_people not in", values, "updatePeople");
            return (Criteria) this;
        }

        public Criteria andUpdatePeopleBetween(String value1, String value2) {
            addCriterion("update_people between", value1, value2, "updatePeople");
            return (Criteria) this;
        }

        public Criteria andUpdatePeopleNotBetween(String value1, String value2) {
            addCriterion("update_people not between", value1, value2, "updatePeople");
            return (Criteria) this;
        }

        public Criteria andLinkageActiveIsNull() {
            addCriterion("linkage_active is null");
            return (Criteria) this;
        }

        public Criteria andLinkageActiveIsNotNull() {
            addCriterion("linkage_active is not null");
            return (Criteria) this;
        }

        public Criteria andLinkageActiveEqualTo(Boolean value) {
            addCriterion("linkage_active =", value, "linkageActive");
            return (Criteria) this;
        }

        public Criteria andLinkageActiveNotEqualTo(Boolean value) {
            addCriterion("linkage_active <>", value, "linkageActive");
            return (Criteria) this;
        }

        public Criteria andLinkageActiveGreaterThan(Boolean value) {
            addCriterion("linkage_active >", value, "linkageActive");
            return (Criteria) this;
        }

        public Criteria andLinkageActiveGreaterThanOrEqualTo(Boolean value) {
            addCriterion("linkage_active >=", value, "linkageActive");
            return (Criteria) this;
        }

        public Criteria andLinkageActiveLessThan(Boolean value) {
            addCriterion("linkage_active <", value, "linkageActive");
            return (Criteria) this;
        }

        public Criteria andLinkageActiveLessThanOrEqualTo(Boolean value) {
            addCriterion("linkage_active <=", value, "linkageActive");
            return (Criteria) this;
        }

        public Criteria andLinkageActiveIn(List<Boolean> values) {
            addCriterion("linkage_active in", values, "linkageActive");
            return (Criteria) this;
        }

        public Criteria andLinkageActiveNotIn(List<Boolean> values) {
            addCriterion("linkage_active not in", values, "linkageActive");
            return (Criteria) this;
        }

        public Criteria andLinkageActiveBetween(Boolean value1, Boolean value2) {
            addCriterion("linkage_active between", value1, value2, "linkageActive");
            return (Criteria) this;
        }

        public Criteria andLinkageActiveNotBetween(Boolean value1, Boolean value2) {
            addCriterion("linkage_active not between", value1, value2, "linkageActive");
            return (Criteria) this;
        }

        public Criteria andExt1IsNull() {
            addCriterion("ext1 is null");
            return (Criteria) this;
        }

        public Criteria andExt1IsNotNull() {
            addCriterion("ext1 is not null");
            return (Criteria) this;
        }

        public Criteria andExt1EqualTo(String value) {
            addCriterion("ext1 =", value, "ext1");
            return (Criteria) this;
        }

        public Criteria andExt1NotEqualTo(String value) {
            addCriterion("ext1 <>", value, "ext1");
            return (Criteria) this;
        }

        public Criteria andExt1GreaterThan(String value) {
            addCriterion("ext1 >", value, "ext1");
            return (Criteria) this;
        }

        public Criteria andExt1GreaterThanOrEqualTo(String value) {
            addCriterion("ext1 >=", value, "ext1");
            return (Criteria) this;
        }

        public Criteria andExt1LessThan(String value) {
            addCriterion("ext1 <", value, "ext1");
            return (Criteria) this;
        }

        public Criteria andExt1LessThanOrEqualTo(String value) {
            addCriterion("ext1 <=", value, "ext1");
            return (Criteria) this;
        }

        public Criteria andExt1Like(String value) {
            addCriterion("ext1 like", value, "ext1");
            return (Criteria) this;
        }

        public Criteria andExt1NotLike(String value) {
            addCriterion("ext1 not like", value, "ext1");
            return (Criteria) this;
        }

        public Criteria andExt1In(List<String> values) {
            addCriterion("ext1 in", values, "ext1");
            return (Criteria) this;
        }

        public Criteria andExt1NotIn(List<String> values) {
            addCriterion("ext1 not in", values, "ext1");
            return (Criteria) this;
        }

        public Criteria andExt1Between(String value1, String value2) {
            addCriterion("ext1 between", value1, value2, "ext1");
            return (Criteria) this;
        }

        public Criteria andExt1NotBetween(String value1, String value2) {
            addCriterion("ext1 not between", value1, value2, "ext1");
            return (Criteria) this;
        }

        public Criteria andExt2IsNull() {
            addCriterion("ext2 is null");
            return (Criteria) this;
        }

        public Criteria andExt2IsNotNull() {
            addCriterion("ext2 is not null");
            return (Criteria) this;
        }

        public Criteria andExt2EqualTo(String value) {
            addCriterion("ext2 =", value, "ext2");
            return (Criteria) this;
        }

        public Criteria andExt2NotEqualTo(String value) {
            addCriterion("ext2 <>", value, "ext2");
            return (Criteria) this;
        }

        public Criteria andExt2GreaterThan(String value) {
            addCriterion("ext2 >", value, "ext2");
            return (Criteria) this;
        }

        public Criteria andExt2GreaterThanOrEqualTo(String value) {
            addCriterion("ext2 >=", value, "ext2");
            return (Criteria) this;
        }

        public Criteria andExt2LessThan(String value) {
            addCriterion("ext2 <", value, "ext2");
            return (Criteria) this;
        }

        public Criteria andExt2LessThanOrEqualTo(String value) {
            addCriterion("ext2 <=", value, "ext2");
            return (Criteria) this;
        }

        public Criteria andExt2Like(String value) {
            addCriterion("ext2 like", value, "ext2");
            return (Criteria) this;
        }

        public Criteria andExt2NotLike(String value) {
            addCriterion("ext2 not like", value, "ext2");
            return (Criteria) this;
        }

        public Criteria andExt2In(List<String> values) {
            addCriterion("ext2 in", values, "ext2");
            return (Criteria) this;
        }

        public Criteria andExt2NotIn(List<String> values) {
            addCriterion("ext2 not in", values, "ext2");
            return (Criteria) this;
        }

        public Criteria andExt2Between(String value1, String value2) {
            addCriterion("ext2 between", value1, value2, "ext2");
            return (Criteria) this;
        }

        public Criteria andExt2NotBetween(String value1, String value2) {
            addCriterion("ext2 not between", value1, value2, "ext2");
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