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
        return viewIds(componentData, false);
    }

    /** 需要随画布保存、回读的视图 ID：查询组件的自定义样式存放在视图上，不能按图表过滤掉。 */
    public static Set<Long> canvasViewIds(String componentData) {
        return viewIds(componentData, true);
    }

    private static Set<Long> viewIds(String componentData, boolean includeQuery) {
        Set<Long> ids = new LinkedHashSet<>();
        if (componentData == null || componentData.isBlank()) {
            return ids;
        }
        JsonNode components = JsonUtil.parseObject(componentData, JsonNode.class);
        if (components == null || !components.isArray()) {
            throw DEException.of(ErrorCategory.VALIDATION, "i18n_error_invalid_parameter");
        }
        collectViewIds(components, ids, includeQuery);
        return ids;
    }

    private static void collectViewIds(JsonNode components, Set<Long> ids, boolean includeQuery) {
        if (!components.isArray()) {
            return;
        }
        for (JsonNode component : components) {
            switch (component.path("component").asText()) {
                case "VQuery" -> {
                    if (includeQuery) {
                        addViewId(component, ids);
                    }
                }
                case "UserView" -> {
                    if (includeQuery || !"VQuery".equals(component.path("innerType").asText())) {
                        addViewId(component, ids);
                    }
                }
                case "Group" -> collectViewIds(component.path("propValue"), ids, includeQuery);
                case "DeTabs" -> {
                    JsonNode tabs = component.path("propValue");
                    if (tabs.isArray()) {
                        tabs.forEach(tab -> collectViewIds(tab.path("componentData"), ids, includeQuery));
                    }
                }
                default -> {
                }
            }
        }
    }

    private static void addViewId(JsonNode component, Set<Long> ids) {
        Long id = chartId(component.path("id"));
        if (id != null) {
            ids.add(id);
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
