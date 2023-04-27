package io.dataease.engine.trans;

import io.dataease.api.dataset.dto.DatasetTableFieldDTO;
import io.dataease.api.dataset.union.model.SQLMeta;
import io.dataease.api.dataset.union.model.SQLObj;
import io.dataease.dataset.utils.TableUtils;
import io.dataease.engine.constant.DeTypeConstants;
import io.dataease.engine.constant.DeTypeConstants;
import io.dataease.engine.constant.ExtFieldConstant;
import io.dataease.engine.constant.SQLConstants;
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

    public static void field2sqlObj(SQLMeta meta, List<DatasetTableFieldDTO> fields, List<DatasetTableFieldDTO> originFields) {
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
                    originField = Utils.calcFieldRegex(x.getOriginName(), tableObj, originFields);
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

    private static SQLObj getXFields(DatasetTableFieldDTO f, String originField, String fieldAlias) {
        String fieldName = "";
        // 处理横轴字段
        if (Objects.equals(f.getDeExtractType(), DeTypeConstants.DE_TIME)) {
            if (Objects.equals(f.getDeType(), DeTypeConstants.DE_INT) || Objects.equals(f.getDeType(), DeTypeConstants.DE_FLOAT)) {
                fieldName = String.format(SQLConstants.UNIX_TIMESTAMP, originField) + "*1000";
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
                        String.format(SQLConstants.DATE_FORMAT, String.format(SQLConstants.STR_TO_DATE, originField, f.getDateFormat()), SQLConstants.DEFAULT_DATE_FORMAT);
            } else {
                fieldName = originField;
            }
        } else {
            if (Objects.equals(f.getDeType(), DeTypeConstants.DE_TIME)) {
                String cast = String.format(SQLConstants.CAST, originField, SQLConstants.DEFAULT_INT_FORMAT) + "/1000";
                fieldName = String.format(SQLConstants.FROM_UNIXTIME, cast, SQLConstants.DEFAULT_DATE_FORMAT);
            } else if (Objects.equals(f.getDeType(), DeTypeConstants.DE_INT)) {
                fieldName = String.format(SQLConstants.CAST, originField, SQLConstants.DEFAULT_INT_FORMAT);
            } else if (Objects.equals(f.getDeType(), DeTypeConstants.DE_FLOAT)) {
                fieldName = String.format(SQLConstants.CAST, originField, SQLConstants.DEFAULT_FLOAT_FORMAT);
            } else {
                fieldName = originField;
            }
        }
        return SQLObj.builder()
                .fieldName(fieldName)
                .fieldAlias(fieldAlias)
                .build();
    }

}
