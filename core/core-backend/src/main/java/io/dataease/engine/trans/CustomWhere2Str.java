package io.dataease.engine.trans;

import io.dataease.api.chart.dto.ChartCustomFilterItemDTO;
import io.dataease.api.chart.dto.ChartFieldCustomFilterDTO;
import io.dataease.dto.dataset.DatasetTableFieldDTO;
import io.dataease.api.dataset.union.model.SQLMeta;
import io.dataease.api.dataset.union.model.SQLObj;
import io.dataease.engine.constant.SQLConstants;
import io.dataease.engine.utils.Utils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author Junjun
 */
public class CustomWhere2Str {

    public static void customWhere2sqlObj(SQLMeta meta, List<ChartFieldCustomFilterDTO> fields, List<DatasetTableFieldDTO> originFields) {
        SQLObj tableObj = meta.getTable();
        if (ObjectUtils.isEmpty(tableObj)) {
            return;
        }
        List<String> res = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(fields)) {
            for (ChartFieldCustomFilterDTO request : fields) {
                List<SQLObj> list = new ArrayList<>();
                DatasetTableFieldDTO field = request.getField();

                if (ObjectUtils.isEmpty(field)) {
                    continue;
                }
                String whereName = "";
                String originName;
                if (ObjectUtils.isNotEmpty(field.getExtField()) && field.getExtField() == 2) {
                    // 解析origin name中有关联的字段生成sql表达式
                    originName = Utils.calcFieldRegex(field.getOriginName(), tableObj, originFields);
                } else if (ObjectUtils.isNotEmpty(field.getExtField()) && field.getExtField() == 1) {
                    originName = String.format(SQLConstants.FIELD_NAME, tableObj.getTableAlias(), field.getDataeaseName());
                } else {
                    originName = String.format(SQLConstants.FIELD_NAME, tableObj.getTableAlias(), field.getDataeaseName());
                }
                if (field.getDeType() == 1) {
                    if (field.getDeExtractType() == 0 || field.getDeExtractType() == 5) {
                        whereName = String.format(SQLConstants.STR_TO_DATE, originName, StringUtils.isNotEmpty(field.getDateFormat()) ? field.getDateFormat() : SQLConstants.DEFAULT_DATE_FORMAT);
                    }
                    if (field.getDeExtractType() == 2 || field.getDeExtractType() == 3 || field.getDeExtractType() == 4) {
                        String cast = String.format(SQLConstants.CAST, originName, SQLConstants.DEFAULT_INT_FORMAT);
                        whereName = String.format(SQLConstants.FROM_UNIXTIME, cast, SQLConstants.DEFAULT_DATE_FORMAT);
                    }
                    if (field.getDeExtractType() == 1) {
                        whereName = originName;
                    }
                } else if (field.getDeType() == 2 || field.getDeType() == 3) {
                    if (field.getDeExtractType() == 0 || field.getDeExtractType() == 5) {
                        whereName = String.format(SQLConstants.CAST, originName, SQLConstants.DEFAULT_FLOAT_FORMAT);
                    }
                    if (field.getDeExtractType() == 1) {
                        whereName = String.format(SQLConstants.UNIX_TIMESTAMP, originName);
                    }
                    if (field.getDeExtractType() == 2 || field.getDeExtractType() == 3 || field.getDeExtractType() == 4) {
                        whereName = originName;
                    }
                } else {
                    whereName = originName;
                }

                if (StringUtils.equalsIgnoreCase(request.getFilterType(), "enum")) {
                    if (ObjectUtils.isNotEmpty(request.getEnumCheckField())) {
                        res.add("(" + whereName + " IN ('" + String.join("','", request.getEnumCheckField()) + "'))");
                    }
                } else {
                    List<ChartCustomFilterItemDTO> filter = request.getFilter();
                    for (ChartCustomFilterItemDTO filterItemDTO : filter) {
                        String value = filterItemDTO.getValue();
                        String whereTerm = Utils.transFilterTerm(filterItemDTO.getTerm());
                        String whereValue = "";

                        if (StringUtils.equalsIgnoreCase(filterItemDTO.getTerm(), "null")) {
                            whereValue = "";
                        } else if (StringUtils.equalsIgnoreCase(filterItemDTO.getTerm(), "not_null")) {
                            whereValue = "";
                        } else if (StringUtils.equalsIgnoreCase(filterItemDTO.getTerm(), "empty")) {
                            whereValue = "''";
                        } else if (StringUtils.equalsIgnoreCase(filterItemDTO.getTerm(), "not_empty")) {
                            whereValue = "''";
                        } else if (StringUtils.containsIgnoreCase(filterItemDTO.getTerm(), "in") || StringUtils.containsIgnoreCase(filterItemDTO.getTerm(), "not in")) {
                            whereValue = "('" + String.join("','", value.split(",")) + "')";
                        } else if (StringUtils.containsIgnoreCase(filterItemDTO.getTerm(), "like")) {
                            whereValue = "'%" + value + "%'";
                        } else {
                            whereValue = String.format(SQLConstants.WHERE_VALUE_VALUE, value);
                        }
                        list.add(SQLObj.builder()
                                .whereField(whereName)
                                .whereTermAndValue(whereTerm + whereValue)
                                .build());
                    }
                    List<String> strList = new ArrayList<>();
                    list.forEach(ele -> strList.add(ele.getWhereField() + " " + ele.getWhereTermAndValue()));
                    if (ObjectUtils.isNotEmpty(list)) {
                        res.add("(" + String.join(" " + Utils.getLogic(request.getLogic()) + " ", strList) + ")");
                    }
                }
            }
            meta.setCustomWheres(ObjectUtils.isNotEmpty(res) ? "(" + String.join(" AND ", res) + ")" : null);
        }
    }

}
