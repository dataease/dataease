package io.dataease.extensions.sync.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 目标插件维护契约，不携带连接凭据和客户端提交的 SQL
 */
public final class SinkMaintenance {
    private SinkMaintenance() {
    }

    /**
     * 当前目标连接的检查结果，能力及限制由目标插件提供，不能作为所有数据库的通用规则
     */
    @Data
    public static class Structure {
        private String version;
        private String definition;
        private String blockedReason;
        private boolean fullRefresh;
        private List<String> operations = new ArrayList<>();
        private List<Column> columns = new ArrayList<>();
        private Map<String, String> indexes = new LinkedHashMap<>();
        private String indexType;
        private List<String> indexFieldTypes = new ArrayList<>();
        private List<Rule> rules = new ArrayList<>();
        private Integer fieldNameMaxLength;
        private Integer fieldCommentMaxLength;
        private Map<String, Integer> sizeLimits = new LinkedHashMap<>();
        private Map<String, Integer> precisionLimits = new LinkedHashMap<>();
        private Map<String, List<String>> typeChanges = new LinkedHashMap<>();
        private Map<String, Map<String, TypeChange>> typeChangeRules = new LinkedHashMap<>();
    }

    /**
     * 当前目标插件的展示规则，最终是否允许修改仍由 plan 校验
     */
    @Data
    public static class Rule {
        private String item;
        private String supported;
        private String restricted;
        /**
         * DATABASE 为官方限制，PLUGIN 为插件范围，TASK 为任务规则；旧插件未提供时按插件范围展示
         */
        private String source;
        private String officialText;
        private String documentationUrl;
        private String applicableVersion;

        public Rule(String item, String supported, String restricted) {
            this.item = item;
            this.supported = supported;
            this.restricted = restricted;
        }

        public Rule() {
        }
    }

    /**
     * SAFE、WARNING、FORBIDDEN 分别用于容量兼容、需确认风险和禁止转换
     * 最终执行仍要重新检查参数及实际数据库结果
     */
    @Data
    public static class TypeChange {
        private String level;
        private String reason;
        private Integer minimumSize;

        public TypeChange(String level, String reason, Integer minimumSize) {
            this.level = level;
            this.reason = reason;
            this.minimumSize = minimumSize;
        }

        public TypeChange() {
        }
    }

    @Data
    public static class Column {
        private TableFieldDTO field;
        private boolean nullable;
        private String defaultExpression;
        private String protectedReason;
    }

    /**
     * 用户提交的字段级最终差异，UPDATE 和 DELETE 使用原目标名称定位，ADD 只携带 after
     */
    @Data
    public static class Change {
        private String action;
        private String originalTargetName;
        private TableFieldDTO after;
    }

    @Data
    public static class Plan {
        private List<Step> steps = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();
        private List<String> blockers = new ArrayList<>();
        /**
         * 字段级检查结果供页面定位，blockers 同时保留以兼容旧页面和执行门禁
         */
        private List<FieldIssue> fieldIssues = new ArrayList<>();
        private boolean requiresReinitialization;
        private boolean riskConfirmationRequired;

        /**
         * 插件和通用校验使用相同的问题契约，页面不解析错误文案来推断字段或属性
         */
        public void reject(Change change, String property, String reason) {
            blockers.add(reason);
            FieldIssue issue = new FieldIssue();
            issue.setOriginalTargetName(change.getOriginalTargetName());
            issue.setFieldName(change.getAfter() == null ? change.getOriginalTargetName() : change.getAfter().getFieldName());
            issue.setProperty(property);
            issue.setReason(reason);
            fieldIssues.add(issue);
        }
    }

    /**
     * 以原目标字段标识修改行，新增字段使用 fieldName；property 使用字段配置的属性名
     */
    @Data
    public static class FieldIssue {
        private String originalTargetName;
        private String fieldName;
        private String property;
        private String reason;
    }

    /**
     * 已规划的单步结构操作，before 和 after 描述该步骤前后而非整批最终字段
     * receipt 用于查询异步作业，状态未知时不能将该步骤重置为未执行
     */
    @Data
    public static class Step {
        private String id;
        private String action;
        private TableFieldDTO before;
        private TableFieldDTO after;
        private String indexName;
        private String state = "PLANNED";
        private String receipt;
        private String error;
    }

    @Data
    public static class Outcome {
        private String state;
        private String receipt;
        private String error;

        public static Outcome of(String state, String receipt, String error) {
            Outcome result = new Outcome();
            result.setState(state);
            result.setReceipt(receipt);
            result.setError(error);
            return result;
        }
    }
}
