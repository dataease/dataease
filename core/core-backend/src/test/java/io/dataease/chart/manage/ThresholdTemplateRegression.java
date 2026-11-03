package io.dataease.chart.manage;

import io.dataease.api.chart.request.ThresholdCheckRequest;
import io.dataease.constant.DeTypeConstants;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.extensions.view.dto.ChartViewDTO;
import io.dataease.extensions.view.dto.ChartViewFieldDTO;
import io.dataease.i18n.Translator;
import org.springframework.context.support.StaticMessageSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Standalone regression; synthetic chart boundary, no application, database or notification sending. */
public class ThresholdTemplateRegression {
    private static final String DATA = "<span id=\"changeText-2\" style=\"color: black\">"
            + "<span data-mce-content=\"[告警数据]\">[告警数据]</span></span>";
    private static final String RULES = "{\"logic\":\"and\",\"items\":[{\"type\":\"item\","
            + "\"fieldId\":100,\"filterType\":\"logic\",\"term\":\"gt\",\"value\":\"0\"}]}";
    private static int cases;

    public static void main(String[] args) throws Exception {
        new Translator().setMessageSource(new StaticMessageSource());
        var manage = new ChartViewThresholdManage();
        var boundary = ChartViewThresholdManage.class.getDeclaredField("chartViewManege");
        boundary.setAccessible(true);
        boundary.set(manage, new ChartViewManege() {
            @Override
            public ChartViewDTO getChart(Long id, String resourceTable, boolean forThreshold) {
                var field = new DatasetTableFieldDTO();
                field.setId(100L);
                field.setName("sales_amount");
                field.setDataeaseName("amount");
                field.setDeType(DeTypeConstants.DE_INT);
                var axis = new ChartViewFieldDTO();
                axis.setId(100L);
                axis.setName("sales_amount");
                axis.setDeType(DeTypeConstants.DE_INT);
                var rows = new ArrayList<Map<String, Object>>();
                for (int i = 1; i <= 12; i++) {
                    rows.add(new HashMap<>(Map.of("amount", i)));
                }
                var chart = new ChartViewDTO();
                chart.setXAxis(List.of(axis));
                chart.setData(new HashMap<>(Map.of("fields", List.of(field), "tableRow", rows)));
                return chart;
            }
        });
        String valid = "<span id=\"changeText-100\"><span style=\"font-weight:bold\">[sales_amount]</span></span>";
        for (boolean showValue : new boolean[]{false, true}) {
            check(manage, DATA, null, showValue, 12);
            for (String empty : new String[]{"", " \n\t", "&nbsp;<br>", "&#160;&#x200B;&#65279;",
                    "<b><i>&nbsp;</i></b>", "\u200B\uFEFF"}) {
                String tag = "<span style=\"color:red\" id=\"changeText-100\">" + empty + "</span>";
                check(manage, tag + DATA, 5, showValue, 12);
                check(manage, DATA + tag, 5, showValue, 12);
                check(manage, tag + valid + DATA, 5, showValue, 5);
            }
            check(manage, valid + DATA, null, showValue, 5);
            check(manage, DATA + valid, 8, showValue, 8);
            check(manage, valid + DATA, 0, showValue, 5);
            check(manage, valid + DATA, 20, showValue, 12);
            check(manage, "<b>普通文字" + "长内容".repeat(500) + "</b>[检测时间][触发告警]" + DATA,
                    5, showValue, 12);
        }
        System.out.println("PASS: " + cases + " actual checkThreshold message generation cases");
    }

    private static void check(ChartViewThresholdManage manage, String template, Integer limit,
                              boolean showValue, int expectedRows) throws Exception {
        var request = new ThresholdCheckRequest();
        request.setChartId(1L);
        request.setThresholdRules(RULES);
        request.setThresholdTemplate(template);
        request.setThresholdLimit(limit);
        request.setShowFieldValue(showValue);
        var result = manage.checkThreshold(request);
        if (!result.isValid()) throw new AssertionError(result.getMsg());
        String content = result.getContent();
        int rows = content.split("<tr ", -1).length - 2;
        if (rows != expectedRows) throw new AssertionError("Expected " + expectedRows + " rows, got " + rows);
        if (content.contains("[检测时间]") || content.contains("[触发告警]") || content.contains("[告警数据]")) {
            throw new AssertionError("Unreplaced built-in placeholder");
        }
        if (template.contains("普通文字") && !content.contains("<b>普通文字" + "长内容".repeat(500) + "</b>")) {
            throw new AssertionError("Styled long text changed");
        }
        cases++;
    }
}
