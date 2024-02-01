package io.dataease.service.chart.build;

import io.dataease.dto.chart.FilterParamTO;
import io.dataease.service.chart.FilterBuildTemplate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service("numberRangeWidget")
public class NumberRangeBuild extends FilterBuildTemplate {

    @Override
    protected FilterParamTO buildParam(Map<String, Object> component) {
        FilterParamTO result = new FilterParamTO();
        result.setComponent(component);
        result.setValue(null);
        result.setOperator("eq");
        Object optionObj = null;
        Object valueObj = null;

        Map<String, Object> options = null;
        List<String> values = null;
        if ((optionObj = component.get("options")) != null && (valueObj = (options = (Map<String, Object>) optionObj).get("value")) != null) {
            if (valueObj instanceof List) {
                values = (List<String>) valueObj;
            } else {
                return result;
            }
            String min = values.get(0);
            String max = null;

            if (values.size() > 1) {
                max = values.get(1);
            }
            result.setOperator("between");

            if (StringUtils.isNotBlank(min) && StringUtils.isNotBlank(max)) {
                result.setValue(values);
                return result;
            }

            if (StringUtils.isBlank(min) && StringUtils.isBlank(max)) {
                result.setValue(null);
                return result;
            }

            if (StringUtils.isNotBlank(min)) {
                List<String> tempValues = new ArrayList<>();
                tempValues.add(min);
                result.setValue(tempValues);
                result.setOperator("ge");
                return result;
            }

            if (StringUtils.isNotBlank(max)) {
                List<String> tempValues = new ArrayList<>();
                tempValues.add(max);
                result.setValue(tempValues);
                result.setOperator("le");
                return result;
            }
        }
        return result;
    }
}
