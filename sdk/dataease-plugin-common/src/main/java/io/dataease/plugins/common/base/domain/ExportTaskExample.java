package io.dataease.plugins.common.base.domain;

import java.util.ArrayList;
import java.util.List;

public class ExportTaskExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public ExportTaskExample() {
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

        public Criteria andFileNameIsNull() {
            addCriterion("file_name is null");
            return (Criteria) this;
        }

        public Criteria andFileNameIsNotNull() {
            addCriterion("file_name is not null");
            return (Criteria) this;
        }

        public Criteria andFileNameEqualTo(String value) {
            addCriterion("file_name =", value, "fileName");
            return (Criteria) this;
        }

        public Criteria andFileNameNotEqualTo(String value) {
            addCriterion("file_name <>", value, "fileName");
            return (Criteria) this;
        }

        public Criteria andFileNameGreaterThan(String value) {
            addCriterion("file_name >", value, "fileName");
            return (Criteria) this;
        }

        public Criteria andFileNameGreaterThanOrEqualTo(String value) {
            addCriterion("file_name >=", value, "fileName");
            return (Criteria) this;
        }

        public Criteria andFileNameLessThan(String value) {
            addCriterion("file_name <", value, "fileName");
            return (Criteria) this;
        }

        public Criteria andFileNameLessThanOrEqualTo(String value) {
            addCriterion("file_name <=", value, "fileName");
            return (Criteria) this;
        }

        public Criteria andFileNameLike(String value) {
            addCriterion("file_name like", value, "fileName");
            return (Criteria) this;
        }

        public Criteria andFileNameNotLike(String value) {
            addCriterion("file_name not like", value, "fileName");
            return (Criteria) this;
        }

        public Criteria andFileNameIn(List<String> values) {
            addCriterion("file_name in", values, "fileName");
            return (Criteria) this;
        }

        public Criteria andFileNameNotIn(List<String> values) {
            addCriterion("file_name not in", values, "fileName");
            return (Criteria) this;
        }

        public Criteria andFileNameBetween(String value1, String value2) {
            addCriterion("file_name between", value1, value2, "fileName");
            return (Criteria) this;
        }

        public Criteria andFileNameNotBetween(String value1, String value2) {
            addCriterion("file_name not between", value1, value2, "fileName");
            return (Criteria) this;
        }

        public Criteria andFileSizeIsNull() {
            addCriterion("file_size is null");
            return (Criteria) this;
        }

        public Criteria andFileSizeIsNotNull() {
            addCriterion("file_size is not null");
            return (Criteria) this;
        }

        public Criteria andFileSizeEqualTo(Double value) {
            addCriterion("file_size =", value, "fileSize");
            return (Criteria) this;
        }

        public Criteria andFileSizeNotEqualTo(Double value) {
            addCriterion("file_size <>", value, "fileSize");
            return (Criteria) this;
        }

        public Criteria andFileSizeGreaterThan(Double value) {
            addCriterion("file_size >", value, "fileSize");
            return (Criteria) this;
        }

        public Criteria andFileSizeGreaterThanOrEqualTo(Double value) {
            addCriterion("file_size >=", value, "fileSize");
            return (Criteria) this;
        }

        public Criteria andFileSizeLessThan(Double value) {
            addCriterion("file_size <", value, "fileSize");
            return (Criteria) this;
        }

        public Criteria andFileSizeLessThanOrEqualTo(Double value) {
            addCriterion("file_size <=", value, "fileSize");
            return (Criteria) this;
        }

        public Criteria andFileSizeIn(List<Double> values) {
            addCriterion("file_size in", values, "fileSize");
            return (Criteria) this;
        }

        public Criteria andFileSizeNotIn(List<Double> values) {
            addCriterion("file_size not in", values, "fileSize");
            return (Criteria) this;
        }

        public Criteria andFileSizeBetween(Double value1, Double value2) {
            addCriterion("file_size between", value1, value2, "fileSize");
            return (Criteria) this;
        }

        public Criteria andFileSizeNotBetween(Double value1, Double value2) {
            addCriterion("file_size not between", value1, value2, "fileSize");
            return (Criteria) this;
        }

        public Criteria andFileSizeUnitIsNull() {
            addCriterion("file_size_unit is null");
            return (Criteria) this;
        }

        public Criteria andFileSizeUnitIsNotNull() {
            addCriterion("file_size_unit is not null");
            return (Criteria) this;
        }

        public Criteria andFileSizeUnitEqualTo(String value) {
            addCriterion("file_size_unit =", value, "fileSizeUnit");
            return (Criteria) this;
        }

        public Criteria andFileSizeUnitNotEqualTo(String value) {
            addCriterion("file_size_unit <>", value, "fileSizeUnit");
            return (Criteria) this;
        }

        public Criteria andFileSizeUnitGreaterThan(String value) {
            addCriterion("file_size_unit >", value, "fileSizeUnit");
            return (Criteria) this;
        }

        public Criteria andFileSizeUnitGreaterThanOrEqualTo(String value) {
            addCriterion("file_size_unit >=", value, "fileSizeUnit");
            return (Criteria) this;
        }

        public Criteria andFileSizeUnitLessThan(String value) {
            addCriterion("file_size_unit <", value, "fileSizeUnit");
            return (Criteria) this;
        }

        public Criteria andFileSizeUnitLessThanOrEqualTo(String value) {
            addCriterion("file_size_unit <=", value, "fileSizeUnit");
            return (Criteria) this;
        }

        public Criteria andFileSizeUnitLike(String value) {
            addCriterion("file_size_unit like", value, "fileSizeUnit");
            return (Criteria) this;
        }

        public Criteria andFileSizeUnitNotLike(String value) {
            addCriterion("file_size_unit not like", value, "fileSizeUnit");
            return (Criteria) this;
        }

        public Criteria andFileSizeUnitIn(List<String> values) {
            addCriterion("file_size_unit in", values, "fileSizeUnit");
            return (Criteria) this;
        }

        public Criteria andFileSizeUnitNotIn(List<String> values) {
            addCriterion("file_size_unit not in", values, "fileSizeUnit");
            return (Criteria) this;
        }

        public Criteria andFileSizeUnitBetween(String value1, String value2) {
            addCriterion("file_size_unit between", value1, value2, "fileSizeUnit");
            return (Criteria) this;
        }

        public Criteria andFileSizeUnitNotBetween(String value1, String value2) {
            addCriterion("file_size_unit not between", value1, value2, "fileSizeUnit");
            return (Criteria) this;
        }

        public Criteria andExportFromIsNull() {
            addCriterion("export_from is null");
            return (Criteria) this;
        }

        public Criteria andExportFromIsNotNull() {
            addCriterion("export_from is not null");
            return (Criteria) this;
        }

        public Criteria andExportFromEqualTo(String value) {
            addCriterion("export_from =", value, "exportFrom");
            return (Criteria) this;
        }

        public Criteria andExportFromNotEqualTo(String value) {
            addCriterion("export_from <>", value, "exportFrom");
            return (Criteria) this;
        }

        public Criteria andExportFromGreaterThan(String value) {
            addCriterion("export_from >", value, "exportFrom");
            return (Criteria) this;
        }

        public Criteria andExportFromGreaterThanOrEqualTo(String value) {
            addCriterion("export_from >=", value, "exportFrom");
            return (Criteria) this;
        }

        public Criteria andExportFromLessThan(String value) {
            addCriterion("export_from <", value, "exportFrom");
            return (Criteria) this;
        }

        public Criteria andExportFromLessThanOrEqualTo(String value) {
            addCriterion("export_from <=", value, "exportFrom");
            return (Criteria) this;
        }

        public Criteria andExportFromLike(String value) {
            addCriterion("export_from like", value, "exportFrom");
            return (Criteria) this;
        }

        public Criteria andExportFromNotLike(String value) {
            addCriterion("export_from not like", value, "exportFrom");
            return (Criteria) this;
        }

        public Criteria andExportFromIn(List<String> values) {
            addCriterion("export_from in", values, "exportFrom");
            return (Criteria) this;
        }

        public Criteria andExportFromNotIn(List<String> values) {
            addCriterion("export_from not in", values, "exportFrom");
            return (Criteria) this;
        }

        public Criteria andExportFromBetween(String value1, String value2) {
            addCriterion("export_from between", value1, value2, "exportFrom");
            return (Criteria) this;
        }

        public Criteria andExportFromNotBetween(String value1, String value2) {
            addCriterion("export_from not between", value1, value2, "exportFrom");
            return (Criteria) this;
        }

        public Criteria andExportStatusIsNull() {
            addCriterion("export_status is null");
            return (Criteria) this;
        }

        public Criteria andExportStatusIsNotNull() {
            addCriterion("export_status is not null");
            return (Criteria) this;
        }

        public Criteria andExportStatusEqualTo(String value) {
            addCriterion("export_status =", value, "exportStatus");
            return (Criteria) this;
        }

        public Criteria andExportStatusNotEqualTo(String value) {
            addCriterion("export_status <>", value, "exportStatus");
            return (Criteria) this;
        }

        public Criteria andExportStatusGreaterThan(String value) {
            addCriterion("export_status >", value, "exportStatus");
            return (Criteria) this;
        }

        public Criteria andExportStatusGreaterThanOrEqualTo(String value) {
            addCriterion("export_status >=", value, "exportStatus");
            return (Criteria) this;
        }

        public Criteria andExportStatusLessThan(String value) {
            addCriterion("export_status <", value, "exportStatus");
            return (Criteria) this;
        }

        public Criteria andExportStatusLessThanOrEqualTo(String value) {
            addCriterion("export_status <=", value, "exportStatus");
            return (Criteria) this;
        }

        public Criteria andExportStatusLike(String value) {
            addCriterion("export_status like", value, "exportStatus");
            return (Criteria) this;
        }

        public Criteria andExportStatusNotLike(String value) {
            addCriterion("export_status not like", value, "exportStatus");
            return (Criteria) this;
        }

        public Criteria andExportStatusIn(List<String> values) {
            addCriterion("export_status in", values, "exportStatus");
            return (Criteria) this;
        }

        public Criteria andExportStatusNotIn(List<String> values) {
            addCriterion("export_status not in", values, "exportStatus");
            return (Criteria) this;
        }

        public Criteria andExportStatusBetween(String value1, String value2) {
            addCriterion("export_status between", value1, value2, "exportStatus");
            return (Criteria) this;
        }

        public Criteria andExportStatusNotBetween(String value1, String value2) {
            addCriterion("export_status not between", value1, value2, "exportStatus");
            return (Criteria) this;
        }

        public Criteria andExportFromTypeIsNull() {
            addCriterion("export_from_type is null");
            return (Criteria) this;
        }

        public Criteria andExportFromTypeIsNotNull() {
            addCriterion("export_from_type is not null");
            return (Criteria) this;
        }

        public Criteria andExportFromTypeEqualTo(String value) {
            addCriterion("export_from_type =", value, "exportFromType");
            return (Criteria) this;
        }

        public Criteria andExportFromTypeNotEqualTo(String value) {
            addCriterion("export_from_type <>", value, "exportFromType");
            return (Criteria) this;
        }

        public Criteria andExportFromTypeGreaterThan(String value) {
            addCriterion("export_from_type >", value, "exportFromType");
            return (Criteria) this;
        }

        public Criteria andExportFromTypeGreaterThanOrEqualTo(String value) {
            addCriterion("export_from_type >=", value, "exportFromType");
            return (Criteria) this;
        }

        public Criteria andExportFromTypeLessThan(String value) {
            addCriterion("export_from_type <", value, "exportFromType");
            return (Criteria) this;
        }

        public Criteria andExportFromTypeLessThanOrEqualTo(String value) {
            addCriterion("export_from_type <=", value, "exportFromType");
            return (Criteria) this;
        }

        public Criteria andExportFromTypeLike(String value) {
            addCriterion("export_from_type like", value, "exportFromType");
            return (Criteria) this;
        }

        public Criteria andExportFromTypeNotLike(String value) {
            addCriterion("export_from_type not like", value, "exportFromType");
            return (Criteria) this;
        }

        public Criteria andExportFromTypeIn(List<String> values) {
            addCriterion("export_from_type in", values, "exportFromType");
            return (Criteria) this;
        }

        public Criteria andExportFromTypeNotIn(List<String> values) {
            addCriterion("export_from_type not in", values, "exportFromType");
            return (Criteria) this;
        }

        public Criteria andExportFromTypeBetween(String value1, String value2) {
            addCriterion("export_from_type between", value1, value2, "exportFromType");
            return (Criteria) this;
        }

        public Criteria andExportFromTypeNotBetween(String value1, String value2) {
            addCriterion("export_from_type not between", value1, value2, "exportFromType");
            return (Criteria) this;
        }

        public Criteria andExportTimeIsNull() {
            addCriterion("export_time is null");
            return (Criteria) this;
        }

        public Criteria andExportTimeIsNotNull() {
            addCriterion("export_time is not null");
            return (Criteria) this;
        }

        public Criteria andExportTimeEqualTo(Long value) {
            addCriterion("export_time =", value, "exportTime");
            return (Criteria) this;
        }

        public Criteria andExportTimeNotEqualTo(Long value) {
            addCriterion("export_time <>", value, "exportTime");
            return (Criteria) this;
        }

        public Criteria andExportTimeGreaterThan(Long value) {
            addCriterion("export_time >", value, "exportTime");
            return (Criteria) this;
        }

        public Criteria andExportTimeGreaterThanOrEqualTo(Long value) {
            addCriterion("export_time >=", value, "exportTime");
            return (Criteria) this;
        }

        public Criteria andExportTimeLessThan(Long value) {
            addCriterion("export_time <", value, "exportTime");
            return (Criteria) this;
        }

        public Criteria andExportTimeLessThanOrEqualTo(Long value) {
            addCriterion("export_time <=", value, "exportTime");
            return (Criteria) this;
        }

        public Criteria andExportTimeIn(List<Long> values) {
            addCriterion("export_time in", values, "exportTime");
            return (Criteria) this;
        }

        public Criteria andExportTimeNotIn(List<Long> values) {
            addCriterion("export_time not in", values, "exportTime");
            return (Criteria) this;
        }

        public Criteria andExportTimeBetween(Long value1, Long value2) {
            addCriterion("export_time between", value1, value2, "exportTime");
            return (Criteria) this;
        }

        public Criteria andExportTimeNotBetween(Long value1, Long value2) {
            addCriterion("export_time not between", value1, value2, "exportTime");
            return (Criteria) this;
        }

        public Criteria andExportPogressIsNull() {
            addCriterion("export_pogress is null");
            return (Criteria) this;
        }

        public Criteria andExportPogressIsNotNull() {
            addCriterion("export_pogress is not null");
            return (Criteria) this;
        }

        public Criteria andExportPogressEqualTo(String value) {
            addCriterion("export_pogress =", value, "exportPogress");
            return (Criteria) this;
        }

        public Criteria andExportPogressNotEqualTo(String value) {
            addCriterion("export_pogress <>", value, "exportPogress");
            return (Criteria) this;
        }

        public Criteria andExportPogressGreaterThan(String value) {
            addCriterion("export_pogress >", value, "exportPogress");
            return (Criteria) this;
        }

        public Criteria andExportPogressGreaterThanOrEqualTo(String value) {
            addCriterion("export_pogress >=", value, "exportPogress");
            return (Criteria) this;
        }

        public Criteria andExportPogressLessThan(String value) {
            addCriterion("export_pogress <", value, "exportPogress");
            return (Criteria) this;
        }

        public Criteria andExportPogressLessThanOrEqualTo(String value) {
            addCriterion("export_pogress <=", value, "exportPogress");
            return (Criteria) this;
        }

        public Criteria andExportPogressLike(String value) {
            addCriterion("export_pogress like", value, "exportPogress");
            return (Criteria) this;
        }

        public Criteria andExportPogressNotLike(String value) {
            addCriterion("export_pogress not like", value, "exportPogress");
            return (Criteria) this;
        }

        public Criteria andExportPogressIn(List<String> values) {
            addCriterion("export_pogress in", values, "exportPogress");
            return (Criteria) this;
        }

        public Criteria andExportPogressNotIn(List<String> values) {
            addCriterion("export_pogress not in", values, "exportPogress");
            return (Criteria) this;
        }

        public Criteria andExportPogressBetween(String value1, String value2) {
            addCriterion("export_pogress between", value1, value2, "exportPogress");
            return (Criteria) this;
        }

        public Criteria andExportPogressNotBetween(String value1, String value2) {
            addCriterion("export_pogress not between", value1, value2, "exportPogress");
            return (Criteria) this;
        }

        public Criteria andExportMachineNameIsNull() {
            addCriterion("export_machine_name is null");
            return (Criteria) this;
        }

        public Criteria andExportMachineNameIsNotNull() {
            addCriterion("export_machine_name is not null");
            return (Criteria) this;
        }

        public Criteria andExportMachineNameEqualTo(String value) {
            addCriterion("export_machine_name =", value, "exportMachineName");
            return (Criteria) this;
        }

        public Criteria andExportMachineNameNotEqualTo(String value) {
            addCriterion("export_machine_name <>", value, "exportMachineName");
            return (Criteria) this;
        }

        public Criteria andExportMachineNameGreaterThan(String value) {
            addCriterion("export_machine_name >", value, "exportMachineName");
            return (Criteria) this;
        }

        public Criteria andExportMachineNameGreaterThanOrEqualTo(String value) {
            addCriterion("export_machine_name >=", value, "exportMachineName");
            return (Criteria) this;
        }

        public Criteria andExportMachineNameLessThan(String value) {
            addCriterion("export_machine_name <", value, "exportMachineName");
            return (Criteria) this;
        }

        public Criteria andExportMachineNameLessThanOrEqualTo(String value) {
            addCriterion("export_machine_name <=", value, "exportMachineName");
            return (Criteria) this;
        }

        public Criteria andExportMachineNameLike(String value) {
            addCriterion("export_machine_name like", value, "exportMachineName");
            return (Criteria) this;
        }

        public Criteria andExportMachineNameNotLike(String value) {
            addCriterion("export_machine_name not like", value, "exportMachineName");
            return (Criteria) this;
        }

        public Criteria andExportMachineNameIn(List<String> values) {
            addCriterion("export_machine_name in", values, "exportMachineName");
            return (Criteria) this;
        }

        public Criteria andExportMachineNameNotIn(List<String> values) {
            addCriterion("export_machine_name not in", values, "exportMachineName");
            return (Criteria) this;
        }

        public Criteria andExportMachineNameBetween(String value1, String value2) {
            addCriterion("export_machine_name between", value1, value2, "exportMachineName");
            return (Criteria) this;
        }

        public Criteria andExportMachineNameNotBetween(String value1, String value2) {
            addCriterion("export_machine_name not between", value1, value2, "exportMachineName");
            return (Criteria) this;
        }

        public Criteria andMsgIsNull() {
            addCriterion("msg is null");
            return (Criteria) this;
        }

        public Criteria andMsgIsNotNull() {
            addCriterion("msg is not null");
            return (Criteria) this;
        }

        public Criteria andMsgEqualTo(String value) {
            addCriterion("msg =", value, "msg");
            return (Criteria) this;
        }

        public Criteria andMsgNotEqualTo(String value) {
            addCriterion("msg <>", value, "msg");
            return (Criteria) this;
        }

        public Criteria andMsgGreaterThan(String value) {
            addCriterion("msg >", value, "msg");
            return (Criteria) this;
        }

        public Criteria andMsgGreaterThanOrEqualTo(String value) {
            addCriterion("msg >=", value, "msg");
            return (Criteria) this;
        }

        public Criteria andMsgLessThan(String value) {
            addCriterion("msg <", value, "msg");
            return (Criteria) this;
        }

        public Criteria andMsgLessThanOrEqualTo(String value) {
            addCriterion("msg <=", value, "msg");
            return (Criteria) this;
        }

        public Criteria andMsgLike(String value) {
            addCriterion("msg like", value, "msg");
            return (Criteria) this;
        }

        public Criteria andMsgNotLike(String value) {
            addCriterion("msg not like", value, "msg");
            return (Criteria) this;
        }

        public Criteria andMsgIn(List<String> values) {
            addCriterion("msg in", values, "msg");
            return (Criteria) this;
        }

        public Criteria andMsgNotIn(List<String> values) {
            addCriterion("msg not in", values, "msg");
            return (Criteria) this;
        }

        public Criteria andMsgBetween(String value1, String value2) {
            addCriterion("msg between", value1, value2, "msg");
            return (Criteria) this;
        }

        public Criteria andMsgNotBetween(String value1, String value2) {
            addCriterion("msg not between", value1, value2, "msg");
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