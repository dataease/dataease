package io.dataease.base.domain;

import java.util.ArrayList;
import java.util.List;

public class PanelLinkJumpInfoExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public PanelLinkJumpInfoExample() {
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

        public Criteria andLinkJumpIdIsNull() {
            addCriterion("link_jump_id is null");
            return (Criteria) this;
        }

        public Criteria andLinkJumpIdIsNotNull() {
            addCriterion("link_jump_id is not null");
            return (Criteria) this;
        }

        public Criteria andLinkJumpIdEqualTo(String value) {
            addCriterion("link_jump_id =", value, "linkJumpId");
            return (Criteria) this;
        }

        public Criteria andLinkJumpIdNotEqualTo(String value) {
            addCriterion("link_jump_id <>", value, "linkJumpId");
            return (Criteria) this;
        }

        public Criteria andLinkJumpIdGreaterThan(String value) {
            addCriterion("link_jump_id >", value, "linkJumpId");
            return (Criteria) this;
        }

        public Criteria andLinkJumpIdGreaterThanOrEqualTo(String value) {
            addCriterion("link_jump_id >=", value, "linkJumpId");
            return (Criteria) this;
        }

        public Criteria andLinkJumpIdLessThan(String value) {
            addCriterion("link_jump_id <", value, "linkJumpId");
            return (Criteria) this;
        }

        public Criteria andLinkJumpIdLessThanOrEqualTo(String value) {
            addCriterion("link_jump_id <=", value, "linkJumpId");
            return (Criteria) this;
        }

        public Criteria andLinkJumpIdLike(String value) {
            addCriterion("link_jump_id like", value, "linkJumpId");
            return (Criteria) this;
        }

        public Criteria andLinkJumpIdNotLike(String value) {
            addCriterion("link_jump_id not like", value, "linkJumpId");
            return (Criteria) this;
        }

        public Criteria andLinkJumpIdIn(List<String> values) {
            addCriterion("link_jump_id in", values, "linkJumpId");
            return (Criteria) this;
        }

        public Criteria andLinkJumpIdNotIn(List<String> values) {
            addCriterion("link_jump_id not in", values, "linkJumpId");
            return (Criteria) this;
        }

        public Criteria andLinkJumpIdBetween(String value1, String value2) {
            addCriterion("link_jump_id between", value1, value2, "linkJumpId");
            return (Criteria) this;
        }

        public Criteria andLinkJumpIdNotBetween(String value1, String value2) {
            addCriterion("link_jump_id not between", value1, value2, "linkJumpId");
            return (Criteria) this;
        }

        public Criteria andLinkTypeIsNull() {
            addCriterion("link_type is null");
            return (Criteria) this;
        }

        public Criteria andLinkTypeIsNotNull() {
            addCriterion("link_type is not null");
            return (Criteria) this;
        }

        public Criteria andLinkTypeEqualTo(String value) {
            addCriterion("link_type =", value, "linkType");
            return (Criteria) this;
        }

        public Criteria andLinkTypeNotEqualTo(String value) {
            addCriterion("link_type <>", value, "linkType");
            return (Criteria) this;
        }

        public Criteria andLinkTypeGreaterThan(String value) {
            addCriterion("link_type >", value, "linkType");
            return (Criteria) this;
        }

        public Criteria andLinkTypeGreaterThanOrEqualTo(String value) {
            addCriterion("link_type >=", value, "linkType");
            return (Criteria) this;
        }

        public Criteria andLinkTypeLessThan(String value) {
            addCriterion("link_type <", value, "linkType");
            return (Criteria) this;
        }

        public Criteria andLinkTypeLessThanOrEqualTo(String value) {
            addCriterion("link_type <=", value, "linkType");
            return (Criteria) this;
        }

        public Criteria andLinkTypeLike(String value) {
            addCriterion("link_type like", value, "linkType");
            return (Criteria) this;
        }

        public Criteria andLinkTypeNotLike(String value) {
            addCriterion("link_type not like", value, "linkType");
            return (Criteria) this;
        }

        public Criteria andLinkTypeIn(List<String> values) {
            addCriterion("link_type in", values, "linkType");
            return (Criteria) this;
        }

        public Criteria andLinkTypeNotIn(List<String> values) {
            addCriterion("link_type not in", values, "linkType");
            return (Criteria) this;
        }

        public Criteria andLinkTypeBetween(String value1, String value2) {
            addCriterion("link_type between", value1, value2, "linkType");
            return (Criteria) this;
        }

        public Criteria andLinkTypeNotBetween(String value1, String value2) {
            addCriterion("link_type not between", value1, value2, "linkType");
            return (Criteria) this;
        }

        public Criteria andJumpTypeIsNull() {
            addCriterion("jump_type is null");
            return (Criteria) this;
        }

        public Criteria andJumpTypeIsNotNull() {
            addCriterion("jump_type is not null");
            return (Criteria) this;
        }

        public Criteria andJumpTypeEqualTo(String value) {
            addCriterion("jump_type =", value, "jumpType");
            return (Criteria) this;
        }

        public Criteria andJumpTypeNotEqualTo(String value) {
            addCriterion("jump_type <>", value, "jumpType");
            return (Criteria) this;
        }

        public Criteria andJumpTypeGreaterThan(String value) {
            addCriterion("jump_type >", value, "jumpType");
            return (Criteria) this;
        }

        public Criteria andJumpTypeGreaterThanOrEqualTo(String value) {
            addCriterion("jump_type >=", value, "jumpType");
            return (Criteria) this;
        }

        public Criteria andJumpTypeLessThan(String value) {
            addCriterion("jump_type <", value, "jumpType");
            return (Criteria) this;
        }

        public Criteria andJumpTypeLessThanOrEqualTo(String value) {
            addCriterion("jump_type <=", value, "jumpType");
            return (Criteria) this;
        }

        public Criteria andJumpTypeLike(String value) {
            addCriterion("jump_type like", value, "jumpType");
            return (Criteria) this;
        }

        public Criteria andJumpTypeNotLike(String value) {
            addCriterion("jump_type not like", value, "jumpType");
            return (Criteria) this;
        }

        public Criteria andJumpTypeIn(List<String> values) {
            addCriterion("jump_type in", values, "jumpType");
            return (Criteria) this;
        }

        public Criteria andJumpTypeNotIn(List<String> values) {
            addCriterion("jump_type not in", values, "jumpType");
            return (Criteria) this;
        }

        public Criteria andJumpTypeBetween(String value1, String value2) {
            addCriterion("jump_type between", value1, value2, "jumpType");
            return (Criteria) this;
        }

        public Criteria andJumpTypeNotBetween(String value1, String value2) {
            addCriterion("jump_type not between", value1, value2, "jumpType");
            return (Criteria) this;
        }

        public Criteria andTargetPanelIdIsNull() {
            addCriterion("target_panel_id is null");
            return (Criteria) this;
        }

        public Criteria andTargetPanelIdIsNotNull() {
            addCriterion("target_panel_id is not null");
            return (Criteria) this;
        }

        public Criteria andTargetPanelIdEqualTo(String value) {
            addCriterion("target_panel_id =", value, "targetPanelId");
            return (Criteria) this;
        }

        public Criteria andTargetPanelIdNotEqualTo(String value) {
            addCriterion("target_panel_id <>", value, "targetPanelId");
            return (Criteria) this;
        }

        public Criteria andTargetPanelIdGreaterThan(String value) {
            addCriterion("target_panel_id >", value, "targetPanelId");
            return (Criteria) this;
        }

        public Criteria andTargetPanelIdGreaterThanOrEqualTo(String value) {
            addCriterion("target_panel_id >=", value, "targetPanelId");
            return (Criteria) this;
        }

        public Criteria andTargetPanelIdLessThan(String value) {
            addCriterion("target_panel_id <", value, "targetPanelId");
            return (Criteria) this;
        }

        public Criteria andTargetPanelIdLessThanOrEqualTo(String value) {
            addCriterion("target_panel_id <=", value, "targetPanelId");
            return (Criteria) this;
        }

        public Criteria andTargetPanelIdLike(String value) {
            addCriterion("target_panel_id like", value, "targetPanelId");
            return (Criteria) this;
        }

        public Criteria andTargetPanelIdNotLike(String value) {
            addCriterion("target_panel_id not like", value, "targetPanelId");
            return (Criteria) this;
        }

        public Criteria andTargetPanelIdIn(List<String> values) {
            addCriterion("target_panel_id in", values, "targetPanelId");
            return (Criteria) this;
        }

        public Criteria andTargetPanelIdNotIn(List<String> values) {
            addCriterion("target_panel_id not in", values, "targetPanelId");
            return (Criteria) this;
        }

        public Criteria andTargetPanelIdBetween(String value1, String value2) {
            addCriterion("target_panel_id between", value1, value2, "targetPanelId");
            return (Criteria) this;
        }

        public Criteria andTargetPanelIdNotBetween(String value1, String value2) {
            addCriterion("target_panel_id not between", value1, value2, "targetPanelId");
            return (Criteria) this;
        }

        public Criteria andSourceFieldIdIsNull() {
            addCriterion("source_field_id is null");
            return (Criteria) this;
        }

        public Criteria andSourceFieldIdIsNotNull() {
            addCriterion("source_field_id is not null");
            return (Criteria) this;
        }

        public Criteria andSourceFieldIdEqualTo(String value) {
            addCriterion("source_field_id =", value, "sourceFieldId");
            return (Criteria) this;
        }

        public Criteria andSourceFieldIdNotEqualTo(String value) {
            addCriterion("source_field_id <>", value, "sourceFieldId");
            return (Criteria) this;
        }

        public Criteria andSourceFieldIdGreaterThan(String value) {
            addCriterion("source_field_id >", value, "sourceFieldId");
            return (Criteria) this;
        }

        public Criteria andSourceFieldIdGreaterThanOrEqualTo(String value) {
            addCriterion("source_field_id >=", value, "sourceFieldId");
            return (Criteria) this;
        }

        public Criteria andSourceFieldIdLessThan(String value) {
            addCriterion("source_field_id <", value, "sourceFieldId");
            return (Criteria) this;
        }

        public Criteria andSourceFieldIdLessThanOrEqualTo(String value) {
            addCriterion("source_field_id <=", value, "sourceFieldId");
            return (Criteria) this;
        }

        public Criteria andSourceFieldIdLike(String value) {
            addCriterion("source_field_id like", value, "sourceFieldId");
            return (Criteria) this;
        }

        public Criteria andSourceFieldIdNotLike(String value) {
            addCriterion("source_field_id not like", value, "sourceFieldId");
            return (Criteria) this;
        }

        public Criteria andSourceFieldIdIn(List<String> values) {
            addCriterion("source_field_id in", values, "sourceFieldId");
            return (Criteria) this;
        }

        public Criteria andSourceFieldIdNotIn(List<String> values) {
            addCriterion("source_field_id not in", values, "sourceFieldId");
            return (Criteria) this;
        }

        public Criteria andSourceFieldIdBetween(String value1, String value2) {
            addCriterion("source_field_id between", value1, value2, "sourceFieldId");
            return (Criteria) this;
        }

        public Criteria andSourceFieldIdNotBetween(String value1, String value2) {
            addCriterion("source_field_id not between", value1, value2, "sourceFieldId");
            return (Criteria) this;
        }

        public Criteria andContentIsNull() {
            addCriterion("content is null");
            return (Criteria) this;
        }

        public Criteria andContentIsNotNull() {
            addCriterion("content is not null");
            return (Criteria) this;
        }

        public Criteria andContentEqualTo(String value) {
            addCriterion("content =", value, "content");
            return (Criteria) this;
        }

        public Criteria andContentNotEqualTo(String value) {
            addCriterion("content <>", value, "content");
            return (Criteria) this;
        }

        public Criteria andContentGreaterThan(String value) {
            addCriterion("content >", value, "content");
            return (Criteria) this;
        }

        public Criteria andContentGreaterThanOrEqualTo(String value) {
            addCriterion("content >=", value, "content");
            return (Criteria) this;
        }

        public Criteria andContentLessThan(String value) {
            addCriterion("content <", value, "content");
            return (Criteria) this;
        }

        public Criteria andContentLessThanOrEqualTo(String value) {
            addCriterion("content <=", value, "content");
            return (Criteria) this;
        }

        public Criteria andContentLike(String value) {
            addCriterion("content like", value, "content");
            return (Criteria) this;
        }

        public Criteria andContentNotLike(String value) {
            addCriterion("content not like", value, "content");
            return (Criteria) this;
        }

        public Criteria andContentIn(List<String> values) {
            addCriterion("content in", values, "content");
            return (Criteria) this;
        }

        public Criteria andContentNotIn(List<String> values) {
            addCriterion("content not in", values, "content");
            return (Criteria) this;
        }

        public Criteria andContentBetween(String value1, String value2) {
            addCriterion("content between", value1, value2, "content");
            return (Criteria) this;
        }

        public Criteria andContentNotBetween(String value1, String value2) {
            addCriterion("content not between", value1, value2, "content");
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

        public Criteria andAttachParamsIsNull() {
            addCriterion("attach_params is null");
            return (Criteria) this;
        }

        public Criteria andAttachParamsIsNotNull() {
            addCriterion("attach_params is not null");
            return (Criteria) this;
        }

        public Criteria andAttachParamsEqualTo(Boolean value) {
            addCriterion("attach_params =", value, "attachParams");
            return (Criteria) this;
        }

        public Criteria andAttachParamsNotEqualTo(Boolean value) {
            addCriterion("attach_params <>", value, "attachParams");
            return (Criteria) this;
        }

        public Criteria andAttachParamsGreaterThan(Boolean value) {
            addCriterion("attach_params >", value, "attachParams");
            return (Criteria) this;
        }

        public Criteria andAttachParamsGreaterThanOrEqualTo(Boolean value) {
            addCriterion("attach_params >=", value, "attachParams");
            return (Criteria) this;
        }

        public Criteria andAttachParamsLessThan(Boolean value) {
            addCriterion("attach_params <", value, "attachParams");
            return (Criteria) this;
        }

        public Criteria andAttachParamsLessThanOrEqualTo(Boolean value) {
            addCriterion("attach_params <=", value, "attachParams");
            return (Criteria) this;
        }

        public Criteria andAttachParamsIn(List<Boolean> values) {
            addCriterion("attach_params in", values, "attachParams");
            return (Criteria) this;
        }

        public Criteria andAttachParamsNotIn(List<Boolean> values) {
            addCriterion("attach_params not in", values, "attachParams");
            return (Criteria) this;
        }

        public Criteria andAttachParamsBetween(Boolean value1, Boolean value2) {
            addCriterion("attach_params between", value1, value2, "attachParams");
            return (Criteria) this;
        }

        public Criteria andAttachParamsNotBetween(Boolean value1, Boolean value2) {
            addCriterion("attach_params not between", value1, value2, "attachParams");
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