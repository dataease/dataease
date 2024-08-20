package io.dataease.engine.trans;

import io.dataease.engine.constant.DeTypeConstants;
import io.dataease.engine.constant.ExtFieldConstant;
import io.dataease.engine.constant.SQLConstants;
import io.dataease.engine.func.FunctionConstant;
import io.dataease.engine.utils.Utils;
import io.dataease.extensions.datasource.api.PluginManageApi;
import io.dataease.extensions.datasource.constant.SqlPlaceholderConstants;
import io.dataease.extensions.datasource.dto.CalParam;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.extensions.datasource.dto.DatasourceSchemaDTO;
import io.dataease.extensions.datasource.model.SQLMeta;
import io.dataease.extensions.datasource.model.SQLObj;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @Author Junjun
 */
public class Field2SQLObj {

    public static void field2sqlObj(SQLMeta meta, List<DatasetTableFieldDTO> fields, List<DatasetTableFieldDTO> originFields, boolean isCross, Map<Long, DatasourceSchemaDTO> dsMap, List<CalParam> fieldParam, List<CalParam> chartParam, PluginManageApi pluginManage) {
        SQLObj tableObj = meta.getTable();
        if (ObjectUtils.isEmpty(tableObj)) {
            return;
        }
        Map<String, String> paramMap = Utils.mergeParam(fieldParam, chartParam);
        List<SQLObj> xFields = new ArrayList<>();
        Map<String, String> fieldsDialect = new HashMap<>();
        if (ObjectUtils.isNotEmpty(fields)) {
            for (int i = 0; i < fields.size(); i++) {
                DatasetTableFieldDTO x = fields.get(i);
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
                    // 此处是数据集预览，获取数据库原始字段枚举值等操作使用，如果遇到聚合函数则将originField设置为null
                    for (String func : FunctionConstant.AGG_FUNC) {
                        if (Utils.matchFunction(func, calcFieldExp)) {
                            originField = null;
                            break;
                        }
                    }
                } else if (ObjectUtils.isNotEmpty(x.getExtField()) && Objects.equals(x.getExtField(), ExtFieldConstant.EXT_COPY)) {
                    originField = String.format(SQLConstants.FIELD_NAME, tableObj.getTableAlias(), x.getDataeaseName());
                } else {
                    originField = String.format(SQLConstants.FIELD_NAME, tableObj.getTableAlias(), x.getDataeaseName());
                }
                String fieldAlias = String.format(SQLConstants.FIELD_ALIAS_X_PREFIX, i);
                // 处理横轴字段
                xFields.add(getXFields(x, originField, fieldAlias, isCross));
            }
        }
        meta.setXFields(xFields);
        meta.setXFieldsDialect(fieldsDialect);
    }

    public static SQLObj getXFields(DatasetTableFieldDTO f, String originField, String fieldAlias, boolean isCross) {
        String fieldName = "";
        if (originField != null) {
            // 处理横轴字段
            if (Objects.equals(f.getDeExtractType(), DeTypeConstants.DE_TIME)) {
                if (Objects.equals(f.getDeType(), DeTypeConstants.DE_INT) || Objects.equals(f.getDeType(), DeTypeConstants.DE_FLOAT)) {
                    fieldName = String.format(SQLConstants.UNIX_TIMESTAMP, originField);
                } else {
                    // 如果都是时间类型，把date和time类型进行字符串拼接
                    if (isCross) {
                        if (StringUtils.equalsIgnoreCase(f.getType(), "date")) {
                            originField = String.format(SQLConstants.DE_STR_TO_DATE, String.format(SQLConstants.CONCAT, originField, "' 00:00:00'"), SQLConstants.DEFAULT_DATE_FORMAT);
                        } else if (StringUtils.equalsIgnoreCase(f.getType(), "time")) {
                            originField = String.format(SQLConstants.DE_STR_TO_DATE, String.format(SQLConstants.CONCAT, "'1970-01-01 '", originField), SQLConstants.DEFAULT_DATE_FORMAT);
                        }
                    }
                    fieldName = originField;
                }
            } else if (Objects.equals(f.getDeExtractType(), DeTypeConstants.DE_STRING)) {
                if (Objects.equals(f.getDeType(), DeTypeConstants.DE_INT)) {
                    fieldName = String.format(SQLConstants.CAST, originField, SQLConstants.DEFAULT_INT_FORMAT);
                } else if (Objects.equals(f.getDeType(), DeTypeConstants.DE_FLOAT)) {
                    fieldName = String.format(SQLConstants.CAST, originField, SQLConstants.DEFAULT_FLOAT_FORMAT);
                } else if (Objects.equals(f.getDeType(), DeTypeConstants.DE_TIME)) {
                    fieldName = StringUtils.isEmpty(f.getDateFormat()) ? String.format(SQLConstants.DE_STR_TO_DATE, originField, SQLConstants.DEFAULT_DATE_FORMAT) :
                            String.format(SQLConstants.DE_STR_TO_DATE, originField, f.getDateFormat());
                } else {
                    fieldName = originField;
                }
            } else {
                if (Objects.equals(f.getDeType(), DeTypeConstants.DE_TIME)) {
                    String cast = String.format(SQLConstants.CAST, originField, SQLConstants.DEFAULT_INT_FORMAT);
                    fieldName = String.format(SQLConstants.FROM_UNIXTIME, cast, SQLConstants.DEFAULT_DATE_FORMAT);
                } else if (Objects.equals(f.getDeType(), DeTypeConstants.DE_INT)) {
                    fieldName = String.format(SQLConstants.CAST, originField, SQLConstants.DEFAULT_INT_FORMAT);
                } else if (Objects.equals(f.getDeType(), DeTypeConstants.DE_FLOAT)) {
                    fieldName = String.format(SQLConstants.CAST, originField, SQLConstants.DEFAULT_FLOAT_FORMAT);
                } else {
                    fieldName = originField;
                }
            }
        } else {
            fieldName = "'-'";
        }
        return SQLObj.builder()
                .fieldName(fieldName)
                .fieldAlias(fieldAlias)
                .build();
    }

}
