package io.dataease.engine.utils;

import io.dataease.api.dataset.union.model.SQLObj;
import io.dataease.dataset.dao.auto.entity.CoreDatasetTableField;
import io.dataease.engine.constant.SQLConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    public static boolean joinSort(String sort) {
        return (StringUtils.equalsIgnoreCase(sort, "asc") || StringUtils.equalsIgnoreCase(sort, "desc"));
    }

    public static String calcFieldRegex(String originField, SQLObj tableObj, List<CoreDatasetTableField> calcFields) {
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
        for (CoreDatasetTableField ele : calcFields) {
            if (StringUtils.containsIgnoreCase(originField, ele.getId())) {
                originField = originField.replaceAll("\\[" + ele.getId() + "]",
                        String.format(SQLConstants.FIELD_NAME, tableObj.getTableAlias(), ele.getDataeaseName()));
            }
        }
        return originField;
    }

    public static String getLogic(String logic) {
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

    public static String transDateFormat(String dateStyle, String datePattern) {
        String split = "-";
        if (StringUtils.equalsIgnoreCase(datePattern, "date_sub")) {
            split = "-";
        } else if (StringUtils.equalsIgnoreCase(datePattern, "date_split")) {
            split = "/";
        } else {
            split = "-";
        }

        if (StringUtils.isEmpty(dateStyle)) {
            return "%Y-%m-%d %H:%i:%S";
        }

        switch (dateStyle) {
            case "y":
                return "%Y";
            case "y_Q":
                return "CONCAT(%s,'" + split + "',%s)";
            case "y_M":
                return "%Y" + split + "%m";
            case "y_W":
                return "%Y" + split + "%u";
            case "y_M_d":
                return "%Y" + split + "%m" + split + "%d";
            case "H_m_s":
                return "%H:%i:%S";
            case "y_M_d_H_m":
                return "%Y" + split + "%m" + split + "%d" + " %H:%i";
            case "y_M_d_H_m_s":
                return "%Y" + split + "%m" + split + "%d" + " %H:%i:%S";
            default:
                return "%Y-%m-%d %H:%i:%S";
        }
    }

    public static String transMysqlFilterTerm(String term) {
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
}
