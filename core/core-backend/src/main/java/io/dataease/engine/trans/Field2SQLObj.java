package io.dataease.engine.trans;

import io.dataease.api.dataset.union.model.SQLMeta;
import io.dataease.api.dataset.union.model.SQLObj;
import io.dataease.dto.dataset.DatasetTableFieldDTO;
import io.dataease.engine.constant.DeTypeConstants;
import io.dataease.engine.constant.ExtFieldConstant;
import io.dataease.engine.constant.SQLConstants;
import io.dataease.engine.func.FunctionConstant;
import io.dataease.engine.utils.Utils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @Author Junjun
 */
public class Field2SQLObj {

    public static void field2sqlObj(SQLMeta meta, List<DatasetTableFieldDTO> fields) {
        SQLObj tableObj = meta.getTable();
        if (ObjectUtils.isEmpty(tableObj)) {
            return;
        }
        List<SQLObj> xFields = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(fields)) {
            for (int i = 0; i < fields.size(); i++) {
                DatasetTableFieldDTO x = fields.get(i);
                String originField;
                if (ObjectUtils.isNotEmpty(x.getExtField()) && Objects.equals(x.getExtField(), ExtFieldConstant.EXT_CALC)) {
                    // 解析origin name中有关联的字段生成sql表达式
                    originField = Utils.calcFieldRegex(x.getOriginName(), tableObj, fields);
                    // 此处是数据集预览，获取数据库原始字段枚举值等操作使用，如果遇到聚合函数则将originField设置为null
                    for (String func : FunctionConstant.AGG_FUNC) {
                        if (Utils.matchFunction(func, originField)) {
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
                xFields.add(getXFields(x, originField, fieldAlias));
            }
        }
        meta.setXFields(xFields);
    }

    public static SQLObj getXFields(DatasetTableFieldDTO f, String originField, String fieldAlias) {
        String fieldName = "";
        if (originField != null) {
            // 处理横轴字段
            if (Objects.equals(f.getDeExtractType(), DeTypeConstants.DE_TIME)) {
                if (Objects.equals(f.getDeType(), DeTypeConstants.DE_INT) || Objects.equals(f.getDeType(), DeTypeConstants.DE_FLOAT)) {
                    fieldName = String.format(SQLConstants.UNIX_TIMESTAMP, originField);
                } else {
                    fieldName = originField;
                }
            } else if (Objects.equals(f.getDeExtractType(), DeTypeConstants.DE_STRING)) {
                if (Objects.equals(f.getDeType(), DeTypeConstants.DE_INT)) {
                    fieldName = String.format(SQLConstants.CAST, originField, SQLConstants.DEFAULT_INT_FORMAT);
                } else if (Objects.equals(f.getDeType(), DeTypeConstants.DE_FLOAT)) {
                    fieldName = String.format(SQLConstants.CAST, originField, SQLConstants.DEFAULT_FLOAT_FORMAT);
                } else if (Objects.equals(f.getDeType(), DeTypeConstants.DE_TIME)) {
                    fieldName = StringUtils.isEmpty(f.getDateFormat()) ? String.format(SQLConstants.STR_TO_DATE, originField, SQLConstants.DEFAULT_DATE_FORMAT) :
                            String.format(SQLConstants.STR_TO_DATE, originField, f.getDateFormat());
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
