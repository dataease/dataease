package io.dataease.visualization.utils;

import com.fasterxml.jackson.databind.JsonNode;
import io.dataease.exception.DEException;
import io.dataease.exception.ErrorCategory;
import io.dataease.extensions.view.dto.ChartViewDTO;
import io.dataease.utils.JsonUtil;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class VisualizationUtils {

    public static Set<Long> activeChartIds(String componentData) {
        Set<Long> ids = new LinkedHashSet<>();
        if (componentData == null || componentData.isBlank()) {
            return ids;
        }
        JsonNode components = JsonUtil.parseObject(componentData, JsonNode.class);
        if (components == null || !components.isArray()) {
            throw DEException.of(ErrorCategory.VALIDATION, "i18n_error_invalid_parameter");
        }
        collectChartIds(components, ids);
        return ids;
    }

    private static void collectChartIds(JsonNode components, Set<Long> ids) {
        if (!components.isArray()) {
            return;
        }
        for (JsonNode component : components) {
            switch (component.path("component").asText()) {
                case "UserView" -> {
                    if (!"VQuery".equals(component.path("innerType").asText())) {
                        Long id = chartId(component.path("id"));
                        if (id != null) {
                            ids.add(id);
                        }
                    }
                }
                case "Group" -> collectChartIds(component.path("propValue"), ids);
                case "DeTabs" -> {
                    JsonNode tabs = component.path("propValue");
                    if (tabs.isArray()) {
                        tabs.forEach(tab -> collectChartIds(tab.path("componentData"), ids));
                    }
                }
                default -> {
                }
            }
        }
    }

    private static Long chartId(JsonNode id) {
        if (id.isIntegralNumber() && id.canConvertToLong()) {
            return id.longValue();
        }
        if (id.isTextual()) {
            try {
                return Long.valueOf(id.textValue());
            } catch (NumberFormatException ignored) {
                // Unsaved components can have temporary IDs.
            }
        }
        return null;
    }

    public static Map<Long, String> viewTransToStr(Map<Long, ChartViewDTO> source) {
        Map<Long, String> result = new HashMap<>();
        source.forEach((key, value) -> {
            result.put(key, (String) JsonUtil.toJSONString(value));
        });
        return result;
    }

    public static Map<Long, ChartViewDTO> viewTransToObj(Map<Long, String> source) {
        Map<Long, ChartViewDTO> result = new HashMap<>();
        source.forEach((key, value) -> {
            result.put(key, JsonUtil.parseObject(value, ChartViewDTO.class));
        });
        return result;
    }
}
