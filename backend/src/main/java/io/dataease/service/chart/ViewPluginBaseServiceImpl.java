package io.dataease.service.chart;

import com.google.gson.Gson;
import io.dataease.base.domain.DatasetTableField;
import io.dataease.base.domain.DatasetTableFieldExample;
import io.dataease.base.domain.Datasource;
import io.dataease.base.mapper.DatasetTableFieldMapper;
import io.dataease.dto.dataset.DataSetTableUnionDTO;
import io.dataease.dto.dataset.DataTableInfoDTO;
import io.dataease.plugins.common.constants.SQLConstants;
import io.dataease.plugins.common.util.BeanUtils;
import io.dataease.plugins.common.util.ConstantsUtil;
import io.dataease.plugins.view.entity.*;
import io.dataease.plugins.view.service.ViewPluginBaseService;
import io.dataease.service.dataset.DataSetTableService;
import io.dataease.service.dataset.DataSetTableUnionService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.dataease.plugins.common.constants.SQLConstants.TABLE_ALIAS_PREFIX;

@Service
public class ViewPluginBaseServiceImpl implements ViewPluginBaseService {


    private static final Integer STRING = 0;
    private static final Integer TIME = 1;
    private static final Integer INT = 2;
    private static final Integer FLOAT = 3;
    /*private static final Integer BOOLEAN = 4;*/

    @Resource
    private DataSetTableUnionService dataSetTableUnionService;

    @Resource
    private DatasetTableFieldMapper datasetTableFieldMapper;

    @Resource
    private DataSetTableService dataSetTableService;


    @Override
    public PluginSingleField buildField(String dsType, PluginViewField pluginViewField, PluginViewSQL tableObj, int index) {
        PluginSingleField result = new PluginSingleField();
        String FIELD_ALIAS_PREFIX = StringUtils.equals(pluginViewField.getTypeField(), "xAxis") ? SQLConstants.FIELD_ALIAS_X_PREFIX : SQLConstants.FIELD_ALIAS_Y_PREFIX;

        String originField = getOriginName(dsType, pluginViewField, tableObj);

        PluginViewSQL field = null;
        String where = null;
        String alias_fix = ConstantsUtil.constantsValue(dsType, "ALIAS_FIX");
        String fieldAlias = String.format(alias_fix, String.format(FIELD_ALIAS_PREFIX, index));
        if (StringUtils.equals(pluginViewField.getTypeField(), "xAxis") || StringUtils.equals(pluginViewField.getTypeField(), "extStack")) {
            field = getXFields(dsType, pluginViewField, originField, fieldAlias);

        }else {
            field = getYFields(dsType, pluginViewField, originField, fieldAlias);
            where = getYWheres(dsType, pluginViewField, fieldAlias);
        }
        PluginViewSQL sort = addSort(pluginViewField.getSort(), originField, fieldAlias);

        Optional.ofNullable(field).ifPresent(f -> result.setField(f));
        Optional.ofNullable(sort).ifPresent(s -> result.setSort(s));
        Optional.ofNullable(where).ifPresent(w -> result.setWhere(w));
        return result;
    }

    @Override
    public String customWhere(String dsType, List<PluginChartFieldCustomFilter> list, PluginViewSQL pluginViewSQL) {
        return transCustomFilterList(dsType, pluginViewSQL, list);
    }

    @Override
    public String panelWhere(String dsType, List<PluginChartExtFilter> list, PluginViewSQL pluginViewSQL) {
        return transExtFilterList(dsType, pluginViewSQL, list);
    }

    @Override
    public PluginViewSQL getTableObj(PluginViewSet pluginViewSet) {
        String tableName = null;
        DataTableInfoDTO dataTableInfoDTO = new Gson().fromJson(pluginViewSet.getInfo(), DataTableInfoDTO.class);
        switch (pluginViewSet.getType()) {
            case "db":
                tableName = dataTableInfoDTO.getTable();
                break;
            case "sql":
                tableName = dataTableInfoDTO.getSql();
                break;
            case "custom":
                List<DataSetTableUnionDTO> list = dataSetTableUnionService.listByTableId(dataTableInfoDTO.getList().get(0).getTableId());
                Datasource ds = new Datasource();
                ds.setType(pluginViewSet.getDsType());
                tableName = dataSetTableService.getCustomSQLDatasource(dataTableInfoDTO, list, ds);
                break;
            case "union":
                Datasource datasource = new Datasource();
                datasource.setType(pluginViewSet.getDsType());
                Map<String, Object> sqlMap = dataSetTableService.getUnionSQLDatasource(dataTableInfoDTO, datasource);
                tableName = (String) sqlMap.get("sql");
                break;
            default:
                break;
        }

        String keyword = ConstantsUtil.constantsValue(pluginViewSet.getDsType(), "KEYWORD_TABLE");
        String tabelName = (tableName.startsWith("(") && tableName.endsWith(")")) ? tableName : String.format(keyword, tableName);
        String tabelAlias = String.format(TABLE_ALIAS_PREFIX, 0);
        PluginViewSQL tableObj = PluginViewSQL.builder().tableName(tabelName).tableAlias(tabelAlias).build();
        return tableObj;
    }

    private String getOriginName(String dsType, PluginViewField pluginViewField, PluginViewSQL tableObj) {
        String keyword_fix = ConstantsUtil.constantsValue(dsType, "KEYWORD_FIX");
        String originField;
        if (ObjectUtils.isNotEmpty(pluginViewField.getExtField()) && pluginViewField.getExtField() == 2) {
            // 解析origin name中有关联的字段生成sql表达式
            originField = calcFieldRegex(dsType,pluginViewField.getOriginName(), tableObj);
        } else if (ObjectUtils.isNotEmpty(pluginViewField.getExtField()) && pluginViewField.getExtField() == 1) {
            originField = String.format(keyword_fix, tableObj.getTableAlias(), pluginViewField.getOriginName());
        } else {
            originField = String.format(keyword_fix, tableObj.getTableAlias(), pluginViewField.getOriginName());
        }
        return originField;
    }

    private String calcFieldRegex(String dsType, String originField, PluginViewSQL tableObj) {
        originField = originField.replaceAll("[\\t\\n\\r]]", "");
        // 正则提取[xxx]
        String regex = "\\[(.*?)]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(originField);
        Set<String> ids = new HashSet<>();
        while (matcher.find()) {
            String id = matcher.group(1);
            ids.add(id);
        }
        if (CollectionUtils.isEmpty(ids)) {
            return originField;
        }
        DatasetTableFieldExample datasetTableFieldExample = new DatasetTableFieldExample();
        datasetTableFieldExample.createCriteria().andIdIn(new ArrayList<>(ids));
        List<DatasetTableField> calcFields = datasetTableFieldMapper.selectByExample(datasetTableFieldExample);
        String keyword_fix = ConstantsUtil.constantsValue(dsType, "KEYWORD_FIX");
        for (DatasetTableField ele : calcFields) {
            originField = originField.replaceAll("\\[" + ele.getId() + "]", String.format(keyword_fix, tableObj.getTableAlias(), ele.getOriginName()));
        }
        return originField;
    }




    private PluginViewSQL getXFields(String dsType, PluginViewField x, String originField, String fieldAlias) {

        String fieldName = "";
        String calc_sub = ConstantsUtil.constantsValue(dsType, "CALC_SUB");
        String to_date = ConstantsUtil.constantsValue(dsType, "TO_DATE");
        String default_start_date = ConstantsUtil.constantsValue(dsType, "DEFAULT_START_DATE");
        String default_date_format = ConstantsUtil.constantsValue(dsType, "DEFAULT_DATE_FORMAT");
        String to_number = ConstantsUtil.constantsValue(dsType, "TO_NUMBER");
        String to_ms = ConstantsUtil.constantsValue(dsType, "TO_MS");
        String to_char = ConstantsUtil.constantsValue(dsType, "TO_CHAR");
        String default_start_date_c = ConstantsUtil.constantsValue(dsType, "DEFAULT_START_DATE");
        if (x.getDeExtractType() == TIME) {
            if (x.getDeType() == INT || x.getDeType() == FLOAT) { //时间转数值
                if (x.getType().equalsIgnoreCase("DATE")) {
                    String date = String.format(calc_sub, originField, String.format(to_date, default_start_date, default_date_format));
                    fieldName = String.format(to_number, date) + to_ms;
                } else {
                    String toChar = String.format(to_char, originField, default_date_format);
                    String toDate = String.format(to_date, toChar, default_date_format);
                    String toDate1 = String.format(to_date, default_start_date_c, default_date_format);
                    fieldName = String.format(to_number, String.format(calc_sub, toDate, toDate1)) + to_ms;
                }
            } else if (x.getDeType() == TIME) { //格式化显示时间
                String format = transDateFormat(x.getDateStyle(), x.getDatePattern());
                if (x.getType().equalsIgnoreCase("DATE")) {
                    fieldName = String.format(to_char, originField, format);
                } else {
                    String toChar = String.format(to_char, originField, default_date_format);
                    String toDate = String.format(to_date, toChar, default_date_format);
                    fieldName = String.format(to_char, toDate, format);
                }
            } else {
                fieldName = originField;
            }
        } else {
            if (x.getDeType() == TIME) {
                String format = transDateFormat(x.getDateStyle(), x.getDatePattern());
                if (x.getDeExtractType() == STRING) { //字符串转时间
                    String toDate = String.format(to_date, originField, default_date_format);
                    fieldName = String.format(to_char, toDate, format);
                } else {                            //数值转时间
                    String date = originField + "/(1000 * 60 * 60 * 24)+" + String.format(to_date, default_start_date_c, default_date_format);
                    fieldName = String.format(to_char, date, format);
                }
            } else {
                fieldName = originField;
            }
        }
        return PluginViewSQL.builder().fieldName(fieldName).fieldAlias(fieldAlias).build();
    }

    private PluginViewSQL getYFields(String dsType, PluginViewField y, String originField, String fieldAlias) {
        String fieldName = "";
        if (StringUtils.equalsIgnoreCase(y.getOriginName(), "*")) {

            fieldName = ConstantsUtil.constantsValue(dsType, "AGG_COUNT");
        } else if (SQLConstants.DIMENSION_TYPE.contains(y.getDeType())) {
            fieldName = String.format(ConstantsUtil.constantsValue(dsType, "AGG_FIELD"), y.getSummary(), originField);
        } else {
            String cast_constants = ConstantsUtil.constantsValue(dsType, "CAST");
            String agg_field_constants = ConstantsUtil.constantsValue(dsType, "AGG_FIELD");
            String default_int_format = ConstantsUtil.constantsValue(dsType, "DEFAULT_INT_FORMAT");
            String default_float_format = ConstantsUtil.constantsValue(dsType, "DEFAULT_FLOAT_FORMAT");
            if (StringUtils.equalsIgnoreCase(y.getSummary(), "avg") || StringUtils.containsIgnoreCase(y.getSummary(), "pop")) {
                String cast = String.format(cast_constants, originField, y.getDeType() == 2 ? default_int_format : default_float_format);
                String agg = String.format(agg_field_constants, y.getSummary(), cast);
                fieldName = String.format(cast_constants, agg, ConstantsUtil.constantsValue(dsType, "DEFAULT_FLOAT_FORMAT"));
            } else {
                String cast = String.format(cast_constants, originField, y.getDeType() == 2 ? default_int_format : default_float_format);
                fieldName = String.format(agg_field_constants, y.getSummary(), cast);
            }
        }
        return PluginViewSQL.builder().fieldName(fieldName).fieldAlias(fieldAlias).build();
    }

    private PluginViewSQL addSort(String sort, String originField, String  fieldAlias) {
        if (StringUtils.isNotEmpty(sort) && !StringUtils.equalsIgnoreCase(sort, "none")) {
            return PluginViewSQL.builder().orderField(originField).orderAlias(fieldAlias).orderDirection(sort).build();
        }
        return null;
    }

    private String getYWheres(String dsType, PluginViewField y,String fieldAlias) {
        List<PluginViewSQL> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(y.getFilter()) && y.getFilter().size() > 0) {
            y.getFilter().forEach(f -> {
                String whereTerm = transMysqlFilterTerm(f.getTerm());
                String whereValue = "";
                // 原始类型不是时间，在de中被转成时间的字段做处理
                if (StringUtils.equalsIgnoreCase(f.getTerm(), "null")) {
                    whereValue = "";
                } else if (StringUtils.equalsIgnoreCase(f.getTerm(), "not_null")) {
                    whereValue = "";
                } else if (StringUtils.equalsIgnoreCase(f.getTerm(), "empty")) {
                    whereValue = "''";
                } else if (StringUtils.equalsIgnoreCase(f.getTerm(), "not_empty")) {
                    whereValue = "''";
                } else if (StringUtils.containsIgnoreCase(f.getTerm(), "in")) {
                    whereValue = "('" + StringUtils.join(f.getValue(), "','") + "')";
                } else if (StringUtils.containsIgnoreCase(f.getTerm(), "like")) {
                    whereValue = "'%" + f.getValue() + "%'";
                } else {
                    String where_value_value = ConstantsUtil.constantsValue(dsType, "WHERE_VALUE_VALUE");
                    whereValue = String.format(where_value_value, f.getValue());
                }
                list.add(PluginViewSQL.builder().whereField(fieldAlias).whereAlias(fieldAlias).whereTermAndValue(whereTerm + whereValue).build());
            });
        }
        List<String> strList = new ArrayList<>();
        list.forEach(ele -> strList.add(ele.getWhereField() + " " + ele.getWhereTermAndValue()));
        return CollectionUtils.isNotEmpty(list) ? "(" + String.join(" " + getLogic(y.getLogic()) + " ", strList) + ")" : null;
    }

    public String getLogic(String logic) {
        if (logic != null) {
            switch (logic) {
                case "and":
                    return "AND";
                case "or":
                    return "OR";
            }
        }
        return "AND";
    }

    private String transDateFormat(String dateStyle, String datePattern) {

        return null;
    }

    public String transMysqlFilterTerm(String term) {
        switch (term) {
            case "eq":
                return " = ";
            case "not_eq":
                return " <> ";
            case "lt":
                return " < ";
            case "le":
                return " <= ";
            case "gt":
                return " > ";
            case "ge":
                return " >= ";
            case "in":
                return " IN ";
            case "not in":
                return " NOT IN ";
            case "like":
                return " LIKE ";
            case "not like":
                return " NOT LIKE ";
            case "null":
                return " IS NULL ";
            case "not_null":
                return " IS NOT NULL ";
            case "empty":
                return " = ";
            case "not_empty":
                return " <> ";
            case "between":
                return " BETWEEN ";
            default:
                return "";
        }
    }

    public String transCustomFilterList(String dsType, PluginViewSQL tableObj, List<PluginChartFieldCustomFilter> requestList) {
        if (CollectionUtils.isEmpty(requestList)) {
            return null;
        }
        List<String> res = new ArrayList<>();
        String to_date = ConstantsUtil.constantsValue(dsType, "TO_DATE");
        String default_date_format = ConstantsUtil.constantsValue(dsType, "DEFAULT_DATE_FORMAT");
        String cast_c = ConstantsUtil.constantsValue(dsType, "CAST");
        String default_int_format = ConstantsUtil.constantsValue(dsType, "DEFAULT_INT_FORMAT");
        String from_unixtime = ConstantsUtil.constantsValue(dsType, "FROM_UNIXTIME");
        String default_float_format = ConstantsUtil.constantsValue(dsType, "DEFAULT_FLOAT_FORMAT");
        String unix_timestamp = ConstantsUtil.constantsValue(dsType, "UNIX_TIMESTAMP");
        String where_value_value = ConstantsUtil.constantsValue(dsType, "WHERE_VALUE_VALUE");
        for (PluginChartFieldCustomFilter request : requestList) {
            List<PluginViewSQL> list = new ArrayList<>();
            PluginDatasetTableField field = request.getField();

            if (ObjectUtils.isEmpty(field)) {
                continue;
            }
            String whereName = "";

            PluginViewField pluginViewField = BeanUtils.copyBean(new PluginViewField(), field);
            String originName = getOriginName(dsType, pluginViewField, tableObj);



            if (field.getDeType() == 1) {
                if (field.getDeExtractType() == 0 || field.getDeExtractType() == 5) {
                    whereName = String.format(to_date, originName, default_date_format);
                }
                if (field.getDeExtractType() == 2 || field.getDeExtractType() == 3 || field.getDeExtractType() == 4) {
                    String cast = String.format(cast_c, originName, default_int_format) + "/1000";
                    whereName = String.format(from_unixtime, cast, default_float_format);
                }
                if (field.getDeExtractType() == 1) {
                    whereName = originName;
                }
            } else if (field.getDeType() == 2 || field.getDeType() == 3) {
                if (field.getDeExtractType() == 0 || field.getDeExtractType() == 5) {
                    whereName = String.format(cast_c, originName,default_float_format);
                }
                if (field.getDeExtractType() == 1) {
                    whereName = String.format(unix_timestamp, originName) + "*1000";
                }
                if (field.getDeExtractType() == 2 || field.getDeExtractType() == 3 || field.getDeExtractType() == 4) {
                    whereName = originName;
                }
            } else {
                whereName = originName;
            }

            if (StringUtils.equalsIgnoreCase(request.getFilterType(), "enum")) {
                if (CollectionUtils.isNotEmpty(request.getEnumCheckField())) {
                    res.add("(" + whereName + " IN ('" + String.join("','", request.getEnumCheckField()) + "'))");
                }
            } else {
                List<PluginChartCustomFilterItem> filter = request.getFilter();
                for (PluginChartCustomFilterItem filterItemDTO : filter) {
                    String value = filterItemDTO.getValue();
                    String whereTerm = transMysqlFilterTerm(filterItemDTO.getTerm());
                    String whereValue = "";

                    if (StringUtils.equalsIgnoreCase(filterItemDTO.getTerm(), "null")) {
                        whereValue = "";
                    } else if (StringUtils.equalsIgnoreCase(filterItemDTO.getTerm(), "not_null")) {
                        whereValue = "";
                    } else if (StringUtils.equalsIgnoreCase(filterItemDTO.getTerm(), "empty")) {
                        whereValue = "''";
                    } else if (StringUtils.equalsIgnoreCase(filterItemDTO.getTerm(), "not_empty")) {
                        whereValue = "''";
                    } else if (StringUtils.containsIgnoreCase(filterItemDTO.getTerm(), "in")) {
                        whereValue = "('" + StringUtils.join(value, "','") + "')";
                    } else if (StringUtils.containsIgnoreCase(filterItemDTO.getTerm(), "like")) {
                        whereValue = "'%" + value + "%'";
                    } else {
                        if (field.getDeType() == 1) {
                            whereValue = String.format(to_date, "'" + value + "'", default_date_format);
                        } else {
                            whereValue = String.format(where_value_value, value);
                        }
                    }
                    list.add(PluginViewSQL.builder()
                            .whereField(whereName)
                            .whereTermAndValue(whereTerm + whereValue)
                            .build());
                }

                List<String> strList = new ArrayList<>();
                list.forEach(ele -> strList.add(ele.getWhereField() + " " + ele.getWhereTermAndValue()));
                if (CollectionUtils.isNotEmpty(list)) {
                    res.add("(" + String.join(" " + getLogic(request.getLogic()) + " ", strList) + ")");
                }
            }
        }
        return CollectionUtils.isNotEmpty(res) ? "(" + String.join(" AND ", res) + ")" : null;
    }

    public String transExtFilterList(String dsType, PluginViewSQL tableObj, List<PluginChartExtFilter> requestList) {
        if (CollectionUtils.isEmpty(requestList)) {
            return null;
        }
        List<PluginViewSQL> list = new ArrayList<>();
        String where_between = ConstantsUtil.constantsValue(dsType, "WHERE_BETWEEN");
        String default_date_format = ConstantsUtil.constantsValue(dsType, "DEFAULT_DATE_FORMAT");
        String to_date = ConstantsUtil.constantsValue(dsType, "TO_DATE");
        String cast_c = ConstantsUtil.constantsValue(dsType, "CAST");
        String from_unixtime = ConstantsUtil.constantsValue(dsType, "FROM_UNIXTIME");
        String default_int_format = ConstantsUtil.constantsValue(dsType, "DEFAULT_INT_FORMAT");
        String default_float_format = ConstantsUtil.constantsValue(dsType, "DEFAULT_FLOAT_FORMAT");
        String unix_timestamp = ConstantsUtil.constantsValue(dsType, "UNIX_TIMESTAMP");
        String where_value_value = ConstantsUtil.constantsValue(dsType, "WHERE_VALUE_VALUE");

        for (PluginChartExtFilter request : requestList) {
            List<String> value = request.getValue();
            PluginDatasetTableField field = request.getDatasetTableField();
            if (CollectionUtils.isEmpty(value) || ObjectUtils.isEmpty(field)) {
                continue;
            }
            String whereName = "";
            String whereTerm = transMysqlFilterTerm(request.getOperator());
            String whereValue = "";

            String originName = getOriginName(dsType,  BeanUtils.copyBean(new PluginViewField(), field), tableObj);


            if (field.getDeType() == 1) {
                if (field.getDeExtractType() == 0 || field.getDeExtractType() == 5) {
                    whereName = String.format(to_date, originName, default_date_format);
                }
                if (field.getDeExtractType() == 2 || field.getDeExtractType() == 3 || field.getDeExtractType() == 4) {
                    String cast = String.format(cast_c, originName, default_int_format) + "/1000";
                    whereName = String.format(from_unixtime, cast, default_date_format);
                }
                if (field.getDeExtractType() == 1) {
                    whereName = originName;
                }
            } else if (field.getDeType() == 2 || field.getDeType() == 3) {
                if (field.getDeExtractType() == 0 || field.getDeExtractType() == 5) {
                    whereName = String.format(cast_c, originName, default_float_format);
                }
                if (field.getDeExtractType() == 1) {
                    whereName = String.format(unix_timestamp, originName) + "*1000";
                }
                if (field.getDeExtractType() == 2 || field.getDeExtractType() == 3 || field.getDeExtractType() == 4) {
                    whereName = originName;
                }
            } else {
                whereName = originName;
            }

            if (StringUtils.containsIgnoreCase(request.getOperator(), "in")) {
                whereValue = "('" + StringUtils.join(value, "','") + "')";
            } else if (StringUtils.containsIgnoreCase(request.getOperator(), "like")) {
                whereValue = "'%" + value.get(0) + "%'";
            } else if (StringUtils.containsIgnoreCase(request.getOperator(), "between")) {
                if (request.getDatasetTableField().getDeType() == 1) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String startTime = simpleDateFormat.format(new Date(Long.parseLong(value.get(0))));
                    String endTime = simpleDateFormat.format(new Date(Long.parseLong(value.get(1))));
                    String st = String.format(to_date, "'" + startTime + "'", default_date_format);
                    String et = String.format(to_date, "'" + endTime + "'", default_date_format);
                    whereValue = st + " AND " + et;
                } else {
                    whereValue = String.format(where_between, value.get(0), value.get(1));
                }
            } else {
                whereValue = String.format(where_value_value, value.get(0));
            }
            list.add(PluginViewSQL.builder().whereField(whereName).whereTermAndValue(whereTerm + whereValue).build());



        }
        List<String> strList = new ArrayList<>();
        list.forEach(ele -> strList.add(ele.getWhereField() + " " + ele.getWhereTermAndValue()));
        return CollectionUtils.isNotEmpty(list) ? "(" + String.join(" AND ", strList) + ")" : null;
    }
}
