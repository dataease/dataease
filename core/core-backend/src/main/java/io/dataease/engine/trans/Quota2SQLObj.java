package io.dataease.engine.trans;

import io.dataease.engine.constant.DeTypeConstants;
import io.dataease.engine.constant.ExtFieldConstant;
import io.dataease.engine.constant.SQLConstants;
import io.dataease.engine.utils.Utils;
import io.dataease.extensions.datasource.api.PluginManageApi;
import io.dataease.extensions.datasource.constant.SqlPlaceholderConstants;
import io.dataease.extensions.datasource.dto.CalParam;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.extensions.datasource.dto.DatasourceSchemaDTO;
import io.dataease.extensions.datasource.model.SQLMeta;
import io.dataease.extensions.datasource.model.SQLObj;
import io.dataease.extensions.view.dto.ChartViewFieldDTO;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * @Author Junjun
 */
public class Quota2SQLObj {

    public static void quota2sqlObj(SQLMeta meta, List<ChartViewFieldDTO> fields, List<DatasetTableFieldDTO> originFields, boolean isCross, Map<Long, DatasourceSchemaDTO> dsMap, List<CalParam> fieldParam, List<CalParam> chartParam, PluginManageApi pluginManage) {
        SQLObj tableObj = meta.getTable();
        if (ObjectUtils.isEmpty(tableObj)) {
            return;
        }
        Map<String, String> paramMap = Utils.mergeParam(fieldParam, chartParam);
        List<SQLObj> yFields = new ArrayList<>();
        List<String> yWheres = new ArrayList<>();
        List<SQLObj> yOrders = new ArrayList<>();
        Map<String, String> fieldsDialect = new HashMap<>();
        if (!CollectionUtils.isEmpty(fields)) {
            for (int i = 0; i < fields.size(); i++) {
                ChartViewFieldDTO y = fields.get(i);
                String originField;
                if (ObjectUtils.isNotEmpty(y.getExtField()) && Objects.equals(y.getExtField(), ExtFieldConstant.EXT_CALC)) {
                    // 解析origin name中有关联的字段生成sql表达式
                    String calcFieldExp = Utils.calcFieldRegex(y.getOriginName(), tableObj, originFields, isCross, dsMap, paramMap, pluginManage);
                    // 给计算字段处加一个占位符，后续SQL方言转换后再替换
                    originField = String.format(SqlPlaceholderConstants.CALC_FIELD_PLACEHOLDER, y.getId());
                    fieldsDialect.put(originField, calcFieldExp);
                    if (isCross) {
                        originField = calcFieldExp;
                    }
                } else if (ObjectUtils.isNotEmpty(y.getExtField()) && Objects.equals(y.getExtField(), ExtFieldConstant.EXT_COPY)) {
                    originField = String.format(SQLConstants.FIELD_NAME, tableObj.getTableAlias(), y.getDataeaseName());
                } else {
                    originField = String.format(SQLConstants.FIELD_NAME, tableObj.getTableAlias(), y.getDataeaseName());
                }
                String fieldAlias = String.format(SQLConstants.FIELD_ALIAS_Y_PREFIX, i);
                // 处理纵轴字段
                SQLObj ySQLObj = getYFields(y, originField, fieldAlias);
                if (StringUtils.equalsIgnoreCase("bar-range", meta.getChartType()) && StringUtils.equalsIgnoreCase(y.getGroupType(), "d") && y.getDeType() == 1) {
                    yFields.add(Dimension2SQLObj.getXFields(y, ySQLObj.getFieldName(), fieldAlias, isCross));
                } else {
                    yFields.add(ySQLObj);
                }
                // 处理纵轴过滤
                String wheres = getYWheres(y, originField, fieldAlias);
                if (ObjectUtils.isNotEmpty(wheres)) {
                    yWheres.add(wheres);
                }
                // 处理纵轴排序
                if (StringUtils.isNotEmpty(y.getSort()) && Utils.joinSort(y.getSort())) {
                    yOrders.add(SQLObj.builder()
                            .orderField(originField)
                            .orderAlias(fieldAlias)
                            .orderDirection(y.getSort())
                            .build());
                }
            }
        }
        meta.setYFields(yFields);
        meta.setYWheres(yWheres);
        meta.setYOrders(yOrders);
        meta.setYFieldsDialect(fieldsDialect);
    }

    private static SQLObj getYFields(ChartViewFieldDTO y, String originField, String fieldAlias) {
        String fieldName = "";
        if (StringUtils.equalsIgnoreCase(y.getOriginName(), "*")) {
            fieldName = SQLConstants.AGG_COUNT;
        } else if (SQLConstants.DIMENSION_TYPE.contains(y.getDeType())) {
            if (StringUtils.equalsIgnoreCase(y.getSummary(), "count_distinct")) {
                fieldName = String.format(SQLConstants.AGG_FIELD, "COUNT", "DISTINCT " + originField);
            } else {
                fieldName = String.format(SQLConstants.AGG_FIELD, y.getSummary(), originField);
            }
        } else {
            if (StringUtils.equalsIgnoreCase(y.getSummary(), "avg") || StringUtils.containsIgnoreCase(y.getSummary(), "pop")) {
                String cast = String.format(SQLConstants.CAST, originField, Objects.equals(y.getDeType(), DeTypeConstants.DE_INT) ? SQLConstants.DEFAULT_INT_FORMAT : SQLConstants.DEFAULT_FLOAT_FORMAT);
                String agg = String.format(SQLConstants.AGG_FIELD, y.getSummary(), cast);
                String cast1 = String.format(SQLConstants.CAST, agg, SQLConstants.DEFAULT_FLOAT_FORMAT);
                fieldName = String.format(SQLConstants.ROUND, cast1, "8");
            } else {
                String cast = String.format(SQLConstants.CAST, originField, Objects.equals(y.getDeType(), DeTypeConstants.DE_INT) ? SQLConstants.DEFAULT_INT_FORMAT : SQLConstants.DEFAULT_FLOAT_FORMAT);
                if (StringUtils.equalsIgnoreCase(y.getSummary(), "count_distinct")) {
                    fieldName = String.format(SQLConstants.AGG_FIELD, "COUNT", "DISTINCT " + cast);
                } else if (y.getSummary() == null){
                    // 透视表自定义汇总不用聚合
                    fieldName = cast;
                } else {
                    fieldName = String.format(SQLConstants.AGG_FIELD, y.getSummary(), cast);
                }
            }
        }

        return SQLObj.builder()
                .fieldName(fieldName)
                .fieldAlias(fieldAlias)
                .build();
    }

    private static String getYWheres(ChartViewFieldDTO y, String originField, String fieldAlias) {
        List<SQLObj> list = new ArrayList<>();
        if (!CollectionUtils.isEmpty(y.getFilter()) && y.getFilter().size() > 0) {
            y.getFilter().forEach(f -> {
                String whereTerm = Utils.transFilterTerm(f.getTerm());
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
                    whereValue = String.format(SQLConstants.WHERE_VALUE_VALUE, f.getValue());
                }
                list.add(SQLObj.builder()
                        .whereField(fieldAlias)
                        .whereAlias(fieldAlias)
                        .whereTermAndValue(whereTerm + whereValue)
                        .build());
            });
        }
        List<String> strList = new ArrayList<>();
        list.forEach(ele -> strList.add(ele.getWhereField() + " " + ele.getWhereTermAndValue()));
        return !CollectionUtils.isEmpty(list) ? "(" + String.join(" " + Utils.getLogic(y.getLogic()) + " ", strList) + ")" : null;
    }

}
