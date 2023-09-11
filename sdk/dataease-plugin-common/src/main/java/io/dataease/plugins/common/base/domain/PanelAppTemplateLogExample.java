package io.dataease.plugins.common.base.domain;

import java.util.ArrayList;
import java.util.List;

public class PanelAppTemplateLogExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public PanelAppTemplateLogExample() {
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

        public Criteria andAppTemplateIdIsNull() {
            addCriterion("app_template_id is null");
            return (Criteria) this;
        }

        public Criteria andAppTemplateIdIsNotNull() {
            addCriterion("app_template_id is not null");
            return (Criteria) this;
        }

        public Criteria andAppTemplateIdEqualTo(String value) {
            addCriterion("app_template_id =", value, "appTemplateId");
            return (Criteria) this;
        }

        public Criteria andAppTemplateIdNotEqualTo(String value) {
            addCriterion("app_template_id <>", value, "appTemplateId");
            return (Criteria) this;
        }

        public Criteria andAppTemplateIdGreaterThan(String value) {
            addCriterion("app_template_id >", value, "appTemplateId");
            return (Criteria) this;
        }

        public Criteria andAppTemplateIdGreaterThanOrEqualTo(String value) {
            addCriterion("app_template_id >=", value, "appTemplateId");
            return (Criteria) this;
        }

        public Criteria andAppTemplateIdLessThan(String value) {
            addCriterion("app_template_id <", value, "appTemplateId");
            return (Criteria) this;
        }

        public Criteria andAppTemplateIdLessThanOrEqualTo(String value) {
            addCriterion("app_template_id <=", value, "appTemplateId");
            return (Criteria) this;
        }

        public Criteria andAppTemplateIdLike(String value) {
            addCriterion("app_template_id like", value, "appTemplateId");
            return (Criteria) this;
        }

        public Criteria andAppTemplateIdNotLike(String value) {
            addCriterion("app_template_id not like", value, "appTemplateId");
            return (Criteria) this;
        }

        public Criteria andAppTemplateIdIn(List<String> values) {
            addCriterion("app_template_id in", values, "appTemplateId");
            return (Criteria) this;
        }

        public Criteria andAppTemplateIdNotIn(List<String> values) {
            addCriterion("app_template_id not in", values, "appTemplateId");
            return (Criteria) this;
        }

        public Criteria andAppTemplateIdBetween(String value1, String value2) {
            addCriterion("app_template_id between", value1, value2, "appTemplateId");
            return (Criteria) this;
        }

        public Criteria andAppTemplateIdNotBetween(String value1, String value2) {
            addCriterion("app_template_id not between", value1, value2, "appTemplateId");
            return (Criteria) this;
        }

        public Criteria andAppTemplateNameIsNull() {
            addCriterion("app_template_name is null");
            return (Criteria) this;
        }

        public Criteria andAppTemplateNameIsNotNull() {
            addCriterion("app_template_name is not null");
            return (Criteria) this;
        }

        public Criteria andAppTemplateNameEqualTo(String value) {
            addCriterion("app_template_name =", value, "appTemplateName");
            return (Criteria) this;
        }

        public Criteria andAppTemplateNameNotEqualTo(String value) {
            addCriterion("app_template_name <>", value, "appTemplateName");
            return (Criteria) this;
        }

        public Criteria andAppTemplateNameGreaterThan(String value) {
            addCriterion("app_template_name >", value, "appTemplateName");
            return (Criteria) this;
        }

        public Criteria andAppTemplateNameGreaterThanOrEqualTo(String value) {
            addCriterion("app_template_name >=", value, "appTemplateName");
            return (Criteria) this;
        }

        public Criteria andAppTemplateNameLessThan(String value) {
            addCriterion("app_template_name <", value, "appTemplateName");
            return (Criteria) this;
        }

        public Criteria andAppTemplateNameLessThanOrEqualTo(String value) {
            addCriterion("app_template_name <=", value, "appTemplateName");
            return (Criteria) this;
        }

        public Criteria andAppTemplateNameLike(String value) {
            addCriterion("app_template_name like", value, "appTemplateName");
            return (Criteria) this;
        }

        public Criteria andAppTemplateNameNotLike(String value) {
            addCriterion("app_template_name not like", value, "appTemplateName");
            return (Criteria) this;
        }

        public Criteria andAppTemplateNameIn(List<String> values) {
            addCriterion("app_template_name in", values, "appTemplateName");
            return (Criteria) this;
        }

        public Criteria andAppTemplateNameNotIn(List<String> values) {
            addCriterion("app_template_name not in", values, "appTemplateName");
            return (Criteria) this;
        }

        public Criteria andAppTemplateNameBetween(String value1, String value2) {
            addCriterion("app_template_name between", value1, value2, "appTemplateName");
            return (Criteria) this;
        }

        public Criteria andAppTemplateNameNotBetween(String value1, String value2) {
            addCriterion("app_template_name not between", value1, value2, "appTemplateName");
            return (Criteria) this;
        }

        public Criteria andDatasourceIdIsNull() {
            addCriterion("datasource_id is null");
            return (Criteria) this;
        }

        public Criteria andDatasourceIdIsNotNull() {
            addCriterion("datasource_id is not null");
            return (Criteria) this;
        }

        public Criteria andDatasourceIdEqualTo(String value) {
            addCriterion("datasource_id =", value, "datasourceId");
            return (Criteria) this;
        }

        public Criteria andDatasourceIdNotEqualTo(String value) {
            addCriterion("datasource_id <>", value, "datasourceId");
            return (Criteria) this;
        }

        public Criteria andDatasourceIdGreaterThan(String value) {
            addCriterion("datasource_id >", value, "datasourceId");
            return (Criteria) this;
        }

        public Criteria andDatasourceIdGreaterThanOrEqualTo(String value) {
            addCriterion("datasource_id >=", value, "datasourceId");
            return (Criteria) this;
        }

        public Criteria andDatasourceIdLessThan(String value) {
            addCriterion("datasource_id <", value, "datasourceId");
            return (Criteria) this;
        }

        public Criteria andDatasourceIdLessThanOrEqualTo(String value) {
            addCriterion("datasource_id <=", value, "datasourceId");
            return (Criteria) this;
        }

        public Criteria andDatasourceIdLike(String value) {
            addCriterion("datasource_id like", value, "datasourceId");
            return (Criteria) this;
        }

        public Criteria andDatasourceIdNotLike(String value) {
            addCriterion("datasource_id not like", value, "datasourceId");
            return (Criteria) this;
        }

        public Criteria andDatasourceIdIn(List<String> values) {
            addCriterion("datasource_id in", values, "datasourceId");
            return (Criteria) this;
        }

        public Criteria andDatasourceIdNotIn(List<String> values) {
            addCriterion("datasource_id not in", values, "datasourceId");
            return (Criteria) this;
        }

        public Criteria andDatasourceIdBetween(String value1, String value2) {
            addCriterion("datasource_id between", value1, value2, "datasourceId");
            return (Criteria) this;
        }

        public Criteria andDatasourceIdNotBetween(String value1, String value2) {
            addCriterion("datasource_id not between", value1, value2, "datasourceId");
            return (Criteria) this;
        }

        public Criteria andDatasourceFromIsNull() {
            addCriterion("datasource_from is null");
            return (Criteria) this;
        }

        public Criteria andDatasourceFromIsNotNull() {
            addCriterion("datasource_from is not null");
            return (Criteria) this;
        }

        public Criteria andDatasourceFromEqualTo(String value) {
            addCriterion("datasource_from =", value, "datasourceFrom");
            return (Criteria) this;
        }

        public Criteria andDatasourceFromNotEqualTo(String value) {
            addCriterion("datasource_from <>", value, "datasourceFrom");
            return (Criteria) this;
        }

        public Criteria andDatasourceFromGreaterThan(String value) {
            addCriterion("datasource_from >", value, "datasourceFrom");
            return (Criteria) this;
        }

        public Criteria andDatasourceFromGreaterThanOrEqualTo(String value) {
            addCriterion("datasource_from >=", value, "datasourceFrom");
            return (Criteria) this;
        }

        public Criteria andDatasourceFromLessThan(String value) {
            addCriterion("datasource_from <", value, "datasourceFrom");
            return (Criteria) this;
        }

        public Criteria andDatasourceFromLessThanOrEqualTo(String value) {
            addCriterion("datasource_from <=", value, "datasourceFrom");
            return (Criteria) this;
        }

        public Criteria andDatasourceFromLike(String value) {
            addCriterion("datasource_from like", value, "datasourceFrom");
            return (Criteria) this;
        }

        public Criteria andDatasourceFromNotLike(String value) {
            addCriterion("datasource_from not like", value, "datasourceFrom");
            return (Criteria) this;
        }

        public Criteria andDatasourceFromIn(List<String> values) {
            addCriterion("datasource_from in", values, "datasourceFrom");
            return (Criteria) this;
        }

        public Criteria andDatasourceFromNotIn(List<String> values) {
            addCriterion("datasource_from not in", values, "datasourceFrom");
            return (Criteria) this;
        }

        public Criteria andDatasourceFromBetween(String value1, String value2) {
            addCriterion("datasource_from between", value1, value2, "datasourceFrom");
            return (Criteria) this;
        }

        public Criteria andDatasourceFromNotBetween(String value1, String value2) {
            addCriterion("datasource_from not between", value1, value2, "datasourceFrom");
            return (Criteria) this;
        }

        public Criteria andSourceDatasourceNameIsNull() {
            addCriterion("source_datasource_name is null");
            return (Criteria) this;
        }

        public Criteria andSourceDatasourceNameIsNotNull() {
            addCriterion("source_datasource_name is not null");
            return (Criteria) this;
        }

        public Criteria andSourceDatasourceNameEqualTo(String value) {
            addCriterion("source_datasource_name =", value, "sourceDatasourceName");
            return (Criteria) this;
        }

        public Criteria andSourceDatasourceNameNotEqualTo(String value) {
            addCriterion("source_datasource_name <>", value, "sourceDatasourceName");
            return (Criteria) this;
        }

        public Criteria andSourceDatasourceNameGreaterThan(String value) {
            addCriterion("source_datasource_name >", value, "sourceDatasourceName");
            return (Criteria) this;
        }

        public Criteria andSourceDatasourceNameGreaterThanOrEqualTo(String value) {
            addCriterion("source_datasource_name >=", value, "sourceDatasourceName");
            return (Criteria) this;
        }

        public Criteria andSourceDatasourceNameLessThan(String value) {
            addCriterion("source_datasource_name <", value, "sourceDatasourceName");
            return (Criteria) this;
        }

        public Criteria andSourceDatasourceNameLessThanOrEqualTo(String value) {
            addCriterion("source_datasource_name <=", value, "sourceDatasourceName");
            return (Criteria) this;
        }

        public Criteria andSourceDatasourceNameLike(String value) {
            addCriterion("source_datasource_name like", value, "sourceDatasourceName");
            return (Criteria) this;
        }

        public Criteria andSourceDatasourceNameNotLike(String value) {
            addCriterion("source_datasource_name not like", value, "sourceDatasourceName");
            return (Criteria) this;
        }

        public Criteria andSourceDatasourceNameIn(List<String> values) {
            addCriterion("source_datasource_name in", values, "sourceDatasourceName");
            return (Criteria) this;
        }

        public Criteria andSourceDatasourceNameNotIn(List<String> values) {
            addCriterion("source_datasource_name not in", values, "sourceDatasourceName");
            return (Criteria) this;
        }

        public Criteria andSourceDatasourceNameBetween(String value1, String value2) {
            addCriterion("source_datasource_name between", value1, value2, "sourceDatasourceName");
            return (Criteria) this;
        }

        public Criteria andSourceDatasourceNameNotBetween(String value1, String value2) {
            addCriterion("source_datasource_name not between", value1, value2, "sourceDatasourceName");
            return (Criteria) this;
        }

        public Criteria andDatasetGroupIdIsNull() {
            addCriterion("dataset_group_id is null");
            return (Criteria) this;
        }

        public Criteria andDatasetGroupIdIsNotNull() {
            addCriterion("dataset_group_id is not null");
            return (Criteria) this;
        }

        public Criteria andDatasetGroupIdEqualTo(String value) {
            addCriterion("dataset_group_id =", value, "datasetGroupId");
            return (Criteria) this;
        }

        public Criteria andDatasetGroupIdNotEqualTo(String value) {
            addCriterion("dataset_group_id <>", value, "datasetGroupId");
            return (Criteria) this;
        }

        public Criteria andDatasetGroupIdGreaterThan(String value) {
            addCriterion("dataset_group_id >", value, "datasetGroupId");
            return (Criteria) this;
        }

        public Criteria andDatasetGroupIdGreaterThanOrEqualTo(String value) {
            addCriterion("dataset_group_id >=", value, "datasetGroupId");
            return (Criteria) this;
        }

        public Criteria andDatasetGroupIdLessThan(String value) {
            addCriterion("dataset_group_id <", value, "datasetGroupId");
            return (Criteria) this;
        }

        public Criteria andDatasetGroupIdLessThanOrEqualTo(String value) {
            addCriterion("dataset_group_id <=", value, "datasetGroupId");
            return (Criteria) this;
        }

        public Criteria andDatasetGroupIdLike(String value) {
            addCriterion("dataset_group_id like", value, "datasetGroupId");
            return (Criteria) this;
        }

        public Criteria andDatasetGroupIdNotLike(String value) {
            addCriterion("dataset_group_id not like", value, "datasetGroupId");
            return (Criteria) this;
        }

        public Criteria andDatasetGroupIdIn(List<String> values) {
            addCriterion("dataset_group_id in", values, "datasetGroupId");
            return (Criteria) this;
        }

        public Criteria andDatasetGroupIdNotIn(List<String> values) {
            addCriterion("dataset_group_id not in", values, "datasetGroupId");
            return (Criteria) this;
        }

        public Criteria andDatasetGroupIdBetween(String value1, String value2) {
            addCriterion("dataset_group_id between", value1, value2, "datasetGroupId");
            return (Criteria) this;
        }

        public Criteria andDatasetGroupIdNotBetween(String value1, String value2) {
            addCriterion("dataset_group_id not between", value1, value2, "datasetGroupId");
            return (Criteria) this;
        }

        public Criteria andSourceDatasetGroupNameIsNull() {
            addCriterion("source_dataset_group_name is null");
            return (Criteria) this;
        }

        public Criteria andSourceDatasetGroupNameIsNotNull() {
            addCriterion("source_dataset_group_name is not null");
            return (Criteria) this;
        }

        public Criteria andSourceDatasetGroupNameEqualTo(String value) {
            addCriterion("source_dataset_group_name =", value, "sourceDatasetGroupName");
            return (Criteria) this;
        }

        public Criteria andSourceDatasetGroupNameNotEqualTo(String value) {
            addCriterion("source_dataset_group_name <>", value, "sourceDatasetGroupName");
            return (Criteria) this;
        }

        public Criteria andSourceDatasetGroupNameGreaterThan(String value) {
            addCriterion("source_dataset_group_name >", value, "sourceDatasetGroupName");
            return (Criteria) this;
        }

        public Criteria andSourceDatasetGroupNameGreaterThanOrEqualTo(String value) {
            addCriterion("source_dataset_group_name >=", value, "sourceDatasetGroupName");
            return (Criteria) this;
        }

        public Criteria andSourceDatasetGroupNameLessThan(String value) {
            addCriterion("source_dataset_group_name <", value, "sourceDatasetGroupName");
            return (Criteria) this;
        }

        public Criteria andSourceDatasetGroupNameLessThanOrEqualTo(String value) {
            addCriterion("source_dataset_group_name <=", value, "sourceDatasetGroupName");
            return (Criteria) this;
        }

        public Criteria andSourceDatasetGroupNameLike(String value) {
            addCriterion("source_dataset_group_name like", value, "sourceDatasetGroupName");
            return (Criteria) this;
        }

        public Criteria andSourceDatasetGroupNameNotLike(String value) {
            addCriterion("source_dataset_group_name not like", value, "sourceDatasetGroupName");
            return (Criteria) this;
        }

        public Criteria andSourceDatasetGroupNameIn(List<String> values) {
            addCriterion("source_dataset_group_name in", values, "sourceDatasetGroupName");
            return (Criteria) this;
        }

        public Criteria andSourceDatasetGroupNameNotIn(List<String> values) {
            addCriterion("source_dataset_group_name not in", values, "sourceDatasetGroupName");
            return (Criteria) this;
        }

        public Criteria andSourceDatasetGroupNameBetween(String value1, String value2) {
            addCriterion("source_dataset_group_name between", value1, value2, "sourceDatasetGroupName");
            return (Criteria) this;
        }

        public Criteria andSourceDatasetGroupNameNotBetween(String value1, String value2) {
            addCriterion("source_dataset_group_name not between", value1, value2, "sourceDatasetGroupName");
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

        public Criteria andSourcePanelNameIsNull() {
            addCriterion("source_panel_name is null");
            return (Criteria) this;
        }

        public Criteria andSourcePanelNameIsNotNull() {
            addCriterion("source_panel_name is not null");
            return (Criteria) this;
        }

        public Criteria andSourcePanelNameEqualTo(String value) {
            addCriterion("source_panel_name =", value, "sourcePanelName");
            return (Criteria) this;
        }

        public Criteria andSourcePanelNameNotEqualTo(String value) {
            addCriterion("source_panel_name <>", value, "sourcePanelName");
            return (Criteria) this;
        }

        public Criteria andSourcePanelNameGreaterThan(String value) {
            addCriterion("source_panel_name >", value, "sourcePanelName");
            return (Criteria) this;
        }

        public Criteria andSourcePanelNameGreaterThanOrEqualTo(String value) {
            addCriterion("source_panel_name >=", value, "sourcePanelName");
            return (Criteria) this;
        }

        public Criteria andSourcePanelNameLessThan(String value) {
            addCriterion("source_panel_name <", value, "sourcePanelName");
            return (Criteria) this;
        }

        public Criteria andSourcePanelNameLessThanOrEqualTo(String value) {
            addCriterion("source_panel_name <=", value, "sourcePanelName");
            return (Criteria) this;
        }

        public Criteria andSourcePanelNameLike(String value) {
            addCriterion("source_panel_name like", value, "sourcePanelName");
            return (Criteria) this;
        }

        public Criteria andSourcePanelNameNotLike(String value) {
            addCriterion("source_panel_name not like", value, "sourcePanelName");
            return (Criteria) this;
        }

        public Criteria andSourcePanelNameIn(List<String> values) {
            addCriterion("source_panel_name in", values, "sourcePanelName");
            return (Criteria) this;
        }

        public Criteria andSourcePanelNameNotIn(List<String> values) {
            addCriterion("source_panel_name not in", values, "sourcePanelName");
            return (Criteria) this;
        }

        public Criteria andSourcePanelNameBetween(String value1, String value2) {
            addCriterion("source_panel_name between", value1, value2, "sourcePanelName");
            return (Criteria) this;
        }

        public Criteria andSourcePanelNameNotBetween(String value1, String value2) {
            addCriterion("source_panel_name not between", value1, value2, "sourcePanelName");
            return (Criteria) this;
        }

        public Criteria andApplyTimeIsNull() {
            addCriterion("apply_time is null");
            return (Criteria) this;
        }

        public Criteria andApplyTimeIsNotNull() {
            addCriterion("apply_time is not null");
            return (Criteria) this;
        }

        public Criteria andApplyTimeEqualTo(Long value) {
            addCriterion("apply_time =", value, "applyTime");
            return (Criteria) this;
        }

        public Criteria andApplyTimeNotEqualTo(Long value) {
            addCriterion("apply_time <>", value, "applyTime");
            return (Criteria) this;
        }

        public Criteria andApplyTimeGreaterThan(Long value) {
            addCriterion("apply_time >", value, "applyTime");
            return (Criteria) this;
        }

        public Criteria andApplyTimeGreaterThanOrEqualTo(Long value) {
            addCriterion("apply_time >=", value, "applyTime");
            return (Criteria) this;
        }

        public Criteria andApplyTimeLessThan(Long value) {
            addCriterion("apply_time <", value, "applyTime");
            return (Criteria) this;
        }

        public Criteria andApplyTimeLessThanOrEqualTo(Long value) {
            addCriterion("apply_time <=", value, "applyTime");
            return (Criteria) this;
        }

        public Criteria andApplyTimeIn(List<Long> values) {
            addCriterion("apply_time in", values, "applyTime");
            return (Criteria) this;
        }

        public Criteria andApplyTimeNotIn(List<Long> values) {
            addCriterion("apply_time not in", values, "applyTime");
            return (Criteria) this;
        }

        public Criteria andApplyTimeBetween(Long value1, Long value2) {
            addCriterion("apply_time between", value1, value2, "applyTime");
            return (Criteria) this;
        }

        public Criteria andApplyTimeNotBetween(Long value1, Long value2) {
            addCriterion("apply_time not between", value1, value2, "applyTime");
            return (Criteria) this;
        }

        public Criteria andApplyPersionIsNull() {
            addCriterion("apply_persion is null");
            return (Criteria) this;
        }

        public Criteria andApplyPersionIsNotNull() {
            addCriterion("apply_persion is not null");
            return (Criteria) this;
        }

        public Criteria andApplyPersionEqualTo(String value) {
            addCriterion("apply_persion =", value, "applyPersion");
            return (Criteria) this;
        }

        public Criteria andApplyPersionNotEqualTo(String value) {
            addCriterion("apply_persion <>", value, "applyPersion");
            return (Criteria) this;
        }

        public Criteria andApplyPersionGreaterThan(String value) {
            addCriterion("apply_persion >", value, "applyPersion");
            return (Criteria) this;
        }

        public Criteria andApplyPersionGreaterThanOrEqualTo(String value) {
            addCriterion("apply_persion >=", value, "applyPersion");
            return (Criteria) this;
        }

        public Criteria andApplyPersionLessThan(String value) {
            addCriterion("apply_persion <", value, "applyPersion");
            return (Criteria) this;
        }

        public Criteria andApplyPersionLessThanOrEqualTo(String value) {
            addCriterion("apply_persion <=", value, "applyPersion");
            return (Criteria) this;
        }

        public Criteria andApplyPersionLike(String value) {
            addCriterion("apply_persion like", value, "applyPersion");
            return (Criteria) this;
        }

        public Criteria andApplyPersionNotLike(String value) {
            addCriterion("apply_persion not like", value, "applyPersion");
            return (Criteria) this;
        }

        public Criteria andApplyPersionIn(List<String> values) {
            addCriterion("apply_persion in", values, "applyPersion");
            return (Criteria) this;
        }

        public Criteria andApplyPersionNotIn(List<String> values) {
            addCriterion("apply_persion not in", values, "applyPersion");
            return (Criteria) this;
        }

        public Criteria andApplyPersionBetween(String value1, String value2) {
            addCriterion("apply_persion between", value1, value2, "applyPersion");
            return (Criteria) this;
        }

        public Criteria andApplyPersionNotBetween(String value1, String value2) {
            addCriterion("apply_persion not between", value1, value2, "applyPersion");
            return (Criteria) this;
        }

        public Criteria andIsSuccessIsNull() {
            addCriterion("is_success is null");
            return (Criteria) this;
        }

        public Criteria andIsSuccessIsNotNull() {
            addCriterion("is_success is not null");
            return (Criteria) this;
        }

        public Criteria andIsSuccessEqualTo(Boolean value) {
            addCriterion("is_success =", value, "isSuccess");
            return (Criteria) this;
        }

        public Criteria andIsSuccessNotEqualTo(Boolean value) {
            addCriterion("is_success <>", value, "isSuccess");
            return (Criteria) this;
        }

        public Criteria andIsSuccessGreaterThan(Boolean value) {
            addCriterion("is_success >", value, "isSuccess");
            return (Criteria) this;
        }

        public Criteria andIsSuccessGreaterThanOrEqualTo(Boolean value) {
            addCriterion("is_success >=", value, "isSuccess");
            return (Criteria) this;
        }

        public Criteria andIsSuccessLessThan(Boolean value) {
            addCriterion("is_success <", value, "isSuccess");
            return (Criteria) this;
        }

        public Criteria andIsSuccessLessThanOrEqualTo(Boolean value) {
            addCriterion("is_success <=", value, "isSuccess");
            return (Criteria) this;
        }

        public Criteria andIsSuccessIn(List<Boolean> values) {
            addCriterion("is_success in", values, "isSuccess");
            return (Criteria) this;
        }

        public Criteria andIsSuccessNotIn(List<Boolean> values) {
            addCriterion("is_success not in", values, "isSuccess");
            return (Criteria) this;
        }

        public Criteria andIsSuccessBetween(Boolean value1, Boolean value2) {
            addCriterion("is_success between", value1, value2, "isSuccess");
            return (Criteria) this;
        }

        public Criteria andIsSuccessNotBetween(Boolean value1, Boolean value2) {
            addCriterion("is_success not between", value1, value2, "isSuccess");
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