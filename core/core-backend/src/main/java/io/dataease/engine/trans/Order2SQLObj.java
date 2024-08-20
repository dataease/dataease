package io.dataease.engine.trans;

import io.dataease.api.chart.dto.DeSortField;
import io.dataease.engine.constant.DeTypeConstants;
import io.dataease.engine.constant.ExtFieldConstant;
import io.dataease.engine.constant.SQLConstants;
import io.dataease.engine.utils.Utils;
import io.dataease.extensions.datasource.api.PluginManageApi;
import io.dataease.extensions.datasource.dto.CalParam;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.extensions.datasource.dto.DatasourceSchemaDTO;
import io.dataease.extensions.datasource.model.SQLMeta;
import io.dataease.extensions.datasource.model.SQLObj;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Author Junjun
 */
public class Order2SQLObj {

    public static void getOrders(SQLMeta meta, List<DeSortField> sortFields, List<DatasetTableFieldDTO> originFields, boolean isCross, Map<Long, DatasourceSchemaDTO> dsMap, List<CalParam> fieldParam, List<CalParam> chartParam, PluginManageApi pluginManage) {
        SQLObj tableObj = meta.getTable();
        List<SQLObj> xOrders = meta.getXOrders() == null ? new ArrayList<>() : meta.getXOrders();
        if (ObjectUtils.isEmpty(tableObj)) {
            return;
        }
        if (ObjectUtils.isNotEmpty(sortFields)) {
            int step = originFields.size();
            for (int i = step; i < (step + sortFields.size()); i++) {
                DeSortField deSortField = sortFields.get(i - step);
                SQLObj order = buildSortField(deSortField, tableObj, i, originFields, isCross, dsMap, fieldParam, chartParam, pluginManage);
                xOrders.add(order);
            }
            meta.setXOrders(xOrders);
        }
    }

    private static SQLObj buildSortField(DeSortField f, SQLObj tableObj, int i, List<DatasetTableFieldDTO> originFields, boolean isCross, Map<Long, DatasourceSchemaDTO> dsMap, List<CalParam> fieldParam, List<CalParam> chartParam, PluginManageApi pluginManage) {
        Map<String, String> paramMap = Utils.mergeParam(fieldParam, chartParam);
        String originField;
        if (ObjectUtils.isNotEmpty(f.getExtField()) && Objects.equals(f.getExtField(), ExtFieldConstant.EXT_CALC)) {
            // 解析origin name中有关联的字段生成sql表达式
            originField = Utils.calcFieldRegex(f.getOriginName(), tableObj, originFields, isCross, dsMap, paramMap, pluginManage);
        } else if (ObjectUtils.isNotEmpty(f.getExtField()) && Objects.equals(f.getExtField(), ExtFieldConstant.EXT_COPY)) {
            originField = String.format(SQLConstants.FIELD_NAME, tableObj.getTableAlias(), f.getDataeaseName());
        } else {
            originField = String.format(SQLConstants.FIELD_NAME, tableObj.getTableAlias(), f.getDataeaseName());
        }
        String fieldAlias = String.format(SQLConstants.FIELD_ALIAS_X_PREFIX, i);
        String fieldName = "";
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
                        String.format(SQLConstants.DE_DATE_FORMAT, String.format(SQLConstants.DE_STR_TO_DATE, originField, f.getDateFormat()), SQLConstants.DEFAULT_DATE_FORMAT);
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
        SQLObj result = SQLObj.builder()
                .orderField(String.format(SQLConstants.FIELD_DOT, originField))
                .orderAlias(String.format(SQLConstants.FIELD_DOT, originField))
                .orderDirection(f.getOrderDirection()).build();
        return result;
    }

}
