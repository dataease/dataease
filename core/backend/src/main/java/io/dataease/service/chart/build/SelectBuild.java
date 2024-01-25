package io.dataease.service.chart.build;

import io.dataease.commons.utils.BeanUtils;
import io.dataease.commons.utils.LogUtil;
import io.dataease.controller.dataset.DataSetTableFieldController;
import io.dataease.controller.request.dataset.MultFieldValuesRequest;
import io.dataease.dto.chart.FilterParamTO;
import io.dataease.dto.dataset.DeSortDTO;
import io.dataease.service.chart.FilterBuildTemplate;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("selectWidget")
public class SelectBuild extends FilterBuildTemplate {

    @Resource
    private DataSetTableFieldController dataSetTableFieldController;

    @Override
    protected FilterParamTO buildParam(Map<String, Object> component) {
        FilterParamTO result = new FilterParamTO();
        result.setComponent(component);
        result.setValue(null);
        result.setOperator("eq");
        Object valueObj = null;

        List<String> realValues = null;
        if (ObjectUtils.isEmpty(component.get("options"))) return result;
        Map<String, Object> options = (Map<String, Object>) component.get("options");

        valueObj = options.get("value");
        String defaultValue = "";
        Map<String, Object> attrs = (Map<String, Object>) options.get("attrs");
        boolean multiple = (boolean) attrs.get("multiple");
        if (!ObjectUtils.isEmpty(valueObj)) {
            if (valueObj instanceof List) {
                defaultValue = "";
            } else {
                defaultValue = valueObj.toString();
            }
        }
        boolean isSelectFirst = StringUtils.equals("custom", component.get("type").toString()) && ObjectUtils.isNotEmpty(attrs.get("selectFirst")) && (boolean) attrs.get("selectFirst");
        if (isSelectFirst) {
            defaultValue = getFirst(attrs);
        }
        if (multiple) {
            if (StringUtils.isBlank(defaultValue)) {
                realValues = new ArrayList<>();
            } else {
                realValues = Arrays.asList(defaultValue.split(","));
            }
        } else {
            if (StringUtils.isBlank(defaultValue)) {
                realValues = new ArrayList<>();
            } else {
                realValues = Arrays.asList(defaultValue.split(",")).stream().limit(1).collect(Collectors.toList());
            }
        }
        result.setOperator(multiple ? "in" : "eq");
        result.setValue(realValues);
        return result;
    }

    private String getFirst(Map<String, Object> attrs) {
        MultFieldValuesRequest request = new MultFieldValuesRequest();
        request.setFieldIds(Arrays.stream(attrs.get("fieldId").toString().split(",")).collect(Collectors.toList()));
        if (ObjectUtils.isNotEmpty(attrs.get("sort"))) {
            DeSortDTO sort = BeanUtils.copyBean(new DeSortDTO(), attrs.get("sort"));
            request.setSort(sort);
        }
        List<Object> list = null;
        try {
            list = dataSetTableFieldController.multFieldValues(request);
        } catch (Exception e) {
            LogUtil.error(e.getMessage());
            return null;
        }
        if (CollectionUtils.isEmpty(list)) return null;
        return list.get(0).toString();
    }
}
