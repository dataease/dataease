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
public class Dimension2SQLObj {

    public static void dimension2sqlObj(SQLMeta meta, List<ChartViewFieldDTO> fields, List<DatasetTableFieldDTO> originFields,
                                        boolean isCross, Map<Long, DatasourceSchemaDTO> dsMap, List<CalParam> fieldParam, List<CalParam> chartParam, PluginManageApi pluginManage) {
        SQLObj tableObj = meta.getTable();
        if (ObjectUtils.isEmpty(tableObj)) {
            return;
        }
        Map<String, String> paramMap = Utils.mergeParam(fieldParam, chartParam);
        List<SQLObj> xFields = new ArrayList<>();
        List<SQLObj> xOrders = new ArrayList<>();
        Map<String, String> fieldsDialect = new HashMap<>();
        if (!CollectionUtils.isEmpty(fields)) {
            for (int i = 0; i < fields.size(); i++) {
                ChartViewFieldDTO x = fields.get(i);
                String originField;
                if (ObjectUtils.isNotEmpty(x.getExtField()) && Objects.equals(x.getExtField(), ExtFieldConstant.EXT_CALC)) {
                    // 解析origin name中有关联的字段生成sql表达式
                    String calcFieldExp = Utils.calcFieldRegex(x.getOriginName(), tableObj, originFields, isCross, dsMap, paramMap,pluginManage);
                    // 给计算字段处加一个占位符，后续SQL方言转换后再替换
                    originField = String.format(SqlPlaceholderConstants.CALC_FIELD_PLACEHOLDER, x.getId());
                    fieldsDialect.put(originField, calcFieldExp);
                    if (isCross) {
                        originField = calcFieldExp;
                    }
                } else if (ObjectUtils.isNotEmpty(x.getExtField()) && Objects.equals(x.getExtField(), ExtFieldConstant.EXT_COPY)) {
                    originField = String.format(SQLConstants.FIELD_NAME, tableObj.getTableAlias(), x.getDataeaseName());
                } else {
                    originField = String.format(SQLConstants.FIELD_NAME, tableObj.getTableAlias(), x.getDataeaseName());
                }
                String fieldAlias = String.format(SQLConstants.FIELD_ALIAS_X_PREFIX, i);
                // 处理横轴字段
                xFields.add(getXFields(x, originField, fieldAlias, isCross));

                // 处理横轴排序
                if (StringUtils.isNotEmpty(x.getSort()) && Utils.joinSort(x.getSort())) {
                    xOrders.add(SQLObj.builder()
                            .orderField(originField)
                            .orderAlias(fieldAlias)
                            .orderDirection(x.getSort())
                            .build());
                }
            }
        }
        meta.setXFields(xFields);
        meta.setXOrders(xOrders);
        meta.setXFieldsDialect(fieldsDialect);
    }

    public static SQLObj getXFields(ChartViewFieldDTO x, String originField, String fieldAlias, boolean isCross) {
        String fieldName = "";
        if (Objects.equals(x.getDeExtractType(), DeTypeConstants.DE_TIME)) {
            if (Objects.equals(x.getDeType(), DeTypeConstants.DE_INT) || Objects.equals(x.getDeType(), DeTypeConstants.DE_FLOAT)) {
                fieldName = String.format(SQLConstants.UNIX_TIMESTAMP, originField);
            } else if (Objects.equals(x.getDeType(), DeTypeConstants.DE_TIME)) {
                // 如果都是时间类型，把date和time类型进行字符串拼接
                if (isCross) {
                    if (StringUtils.equalsIgnoreCase(x.getType(), "date")) {
                        originField = String.format(SQLConstants.DE_STR_TO_DATE, String.format(SQLConstants.CONCAT, originField, "' 00:00:00'"), SQLConstants.DEFAULT_DATE_FORMAT);
                    } else if (StringUtils.equalsIgnoreCase(x.getType(), "time")) {
                        originField = String.format(SQLConstants.DE_STR_TO_DATE, String.format(SQLConstants.CONCAT, "'1970-01-01 '", originField), SQLConstants.DEFAULT_DATE_FORMAT);
                    }
                }
                String format = Utils.transDateFormat(x.getDateStyle(), x.getDatePattern());
                if (StringUtils.equalsIgnoreCase(x.getDateStyle(), "y_Q")) {
                    fieldName = String.format(format,
                            String.format(SQLConstants.DE_DATE_FORMAT, originField, "yyyy"),
                            String.format(SQLConstants.QUARTER, originField));
                } else {
                    fieldName = String.format(SQLConstants.DE_CAST_DATE_FORMAT, originField,
                            SQLConstants.DEFAULT_DATE_FORMAT,
                            format);
                }
            } else {
                fieldName = originField;
            }
        } else {
            if (Objects.equals(x.getDeType(), DeTypeConstants.DE_TIME)) {
                String format = Utils.transDateFormat(x.getDateStyle(), x.getDatePattern());
                if (Objects.equals(x.getDeExtractType(), DeTypeConstants.DE_STRING) || Objects.equals(x.getDeExtractType(), DeTypeConstants.DE_LOCATION)) {
                    if (StringUtils.equalsIgnoreCase(x.getDateStyle(), "y_Q")) {
                        fieldName = String.format(format,
                                String.format(SQLConstants.DE_DATE_FORMAT, String.format(SQLConstants.DE_STR_TO_DATE, originField, SQLConstants.DEFAULT_DATE_FORMAT), "yyyy"),
                                String.format(SQLConstants.QUARTER, String.format(SQLConstants.DE_STR_TO_DATE, originField, SQLConstants.DEFAULT_DATE_FORMAT)));
                    } else {
                        String s = String.format(SQLConstants.DE_STR_TO_DATE, originField, StringUtils.isEmpty(x.getDateFormat()) ? SQLConstants.DEFAULT_DATE_FORMAT : x.getDateFormat());
                        fieldName = String.format(SQLConstants.DE_CAST_DATE_FORMAT, s, SQLConstants.DEFAULT_DATE_FORMAT, format);
                    }
                } else if (Objects.equals(x.getDeExtractType(), DeTypeConstants.DE_INT) || Objects.equals(x.getDeExtractType(), DeTypeConstants.DE_FLOAT) || Objects.equals(x.getDeExtractType(), DeTypeConstants.DE_BOOL)) {
                    String cast = String.format(SQLConstants.CAST, originField, SQLConstants.DEFAULT_INT_FORMAT);
                    String from_unixtime = String.format(SQLConstants.FROM_UNIXTIME, cast, SQLConstants.DEFAULT_DATE_FORMAT);
                    if (StringUtils.equalsIgnoreCase(x.getDateStyle(), "y_Q")) {
                        fieldName = String.format(format,
                                String.format(SQLConstants.DE_DATE_FORMAT, from_unixtime, "yyyy"),
                                String.format(SQLConstants.QUARTER, from_unixtime));
                    } else {
                        fieldName = String.format(SQLConstants.DE_CAST_DATE_FORMAT, from_unixtime, SQLConstants.DEFAULT_DATE_FORMAT, format);
                    }
                } else {
                    fieldName = String.format(SQLConstants.DE_DATE_FORMAT, originField, format);
                }
            } else if (Objects.equals(x.getDeType(), DeTypeConstants.DE_STRING) && Objects.equals(x.getDeExtractType(), DeTypeConstants.DE_STRING)) {
                fieldName = originField;
            } else {
                if (Objects.equals(x.getDeType(), DeTypeConstants.DE_INT)) {
                    fieldName = String.format(SQLConstants.CAST, originField, SQLConstants.DEFAULT_INT_FORMAT);
                } else if (Objects.equals(x.getDeType(), DeTypeConstants.DE_FLOAT)) {
                    fieldName = String.format(SQLConstants.CAST, originField, SQLConstants.DEFAULT_FLOAT_FORMAT);
                } else {
                    fieldName = originField;
                }
            }
        }
        return SQLObj.builder()
                .fieldName(fieldName)
                .fieldAlias(fieldAlias)
                .build();
    }

}
