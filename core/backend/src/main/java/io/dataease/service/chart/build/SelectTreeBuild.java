package io.dataease.service.chart.build;

import io.dataease.dto.chart.FilterParamTO;
import io.dataease.service.chart.FilterBuildTemplate;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("textSelectTreeWidget")
public class SelectTreeBuild extends FilterBuildTemplate {
    @Override
    protected FilterParamTO buildParam(Map<String, Object> component) {
        FilterParamTO result = new FilterParamTO();
        result.setComponent(component);
        result.setValue(null);
        result.setOperator("eq");
        result.setIsTree(true);
        Object valueObj = null;

        List<String> realValues = null;
        if(ObjectUtils.isEmpty(component.get("options"))) return result;
        Map<String, Object> options = (Map<String, Object>)component.get("options");

        valueObj = options.get("value");
        String defaultValue = "";
        Map<String, Object> attrs = (Map<String, Object>) options.get("attrs");
        boolean multiple = (boolean) attrs.get("multiple");
        if(!ObjectUtils.isEmpty(valueObj)) {
            if(valueObj instanceof List) {
                defaultValue = "";
            }else {
                defaultValue = valueObj.toString();
            }
        }
        if(multiple) {
            if (StringUtils.isBlank(defaultValue)) {
                realValues = new ArrayList<>();
            }else {
                realValues = Arrays.asList(defaultValue.split(","));
            }
        } else {
            if (StringUtils.isBlank(defaultValue)) {
                realValues = new ArrayList<>();
            }else {
                realValues = Arrays.asList(defaultValue.split(",")).stream().limit(1).collect(Collectors.toList());
            }
        }
        result.setOperator(multiple ? "in" : "eq");
        result.setValue(realValues);

        if (CollectionUtils.isNotEmpty(result.getValue())) {
            result.setValue(result.getValue().stream().map(val -> val.replaceAll("-de-", ",")).collect(Collectors.toList()));
        }
        return result;
    }
}
