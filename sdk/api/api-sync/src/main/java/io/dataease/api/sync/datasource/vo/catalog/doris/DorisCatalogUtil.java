package io.dataease.api.sync.datasource.vo.catalog.doris;

import com.fasterxml.jackson.core.type.TypeReference;
import io.dataease.api.sync.datasource.vo.catalog.data.type.DorisDataType;
import io.dataease.api.sync.datasource.vo.model.*;
import io.dataease.api.sync.datasource.vo.model.config.Doris;
import io.dataease.utils.JsonUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author fit2cloud
 * @date 2023/9/1 09:51
 **/
public class DorisCatalogUtil {

    private static final String PARAMETER_PLACEHOLDER = "\\$\\{%s}";
    /**
     * 动态分区参数名称前缀
     */
    private static final String PARAMETER_PARTITION_NAME_PREFIX = "p_";

    /**
     * 构建建表语句
     * 数据模式默认为UNIQUE
     *
     * @param source 源数据库
     * @param target 目标数据库
     */
    public static String getCreateTableStatement(Source source, Target target) {
        // doris 建表配置
        DorisConfig dorisConfig = buildDorisConfig(target);
        // 源数据库类型
        dorisConfig.setSourceDbType(source.getDatasource().getType());
        // 分桶字段，默认Hash,使用 Unique keys
        List<String> distributionCols = dorisConfig.getPrimaryKeys();
        String distributionBucket = Objects.nonNull(dorisConfig.getDistributionBucket()) ? dorisConfig.getDistributionBucket() : String.valueOf(10);
        Map<String, String> properties = dorisConfig.getProperties();
        String tableIdentifier = "`" + dorisConfig.getDatabase() + "`.`" + dorisConfig.getTableName() + "`";
        // 建表脚本模版
        String template = DorisCatalogTemplate.DEFAULT_CREATE_TEMPLATE;
        template =
                template.replaceAll(String.format(PARAMETER_PLACEHOLDER, "table_identifier"), tableIdentifier);
        List<TableField> columns = sortFields(dorisConfig.getColumns(), dorisConfig.getPrimaryKeys());
        // 配置分区
        if (dorisConfig.isPartitionEnable()) {
            template = buildTablePartition(dorisConfig, properties, template, columns);
        } else {
            template = template.replaceAll(
                    String.format(PARAMETER_PLACEHOLDER, "partition"),
                    "");
        }
        // 表结构
        List<String> columnDefinition = buildColumnDefinition(dorisConfig, columns);

        // 索引 bitmap
        List<String> indexFieldList = columns.stream().filter(TableField::isFieldIndex).map(TableField::getFieldName).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(indexFieldList)) {
            indexFieldList.forEach(indexField -> columnDefinition.add(
                    String.format(
                            "INDEX `%s_idx` (`%s`) USING BITMAP COMMENT '%s bitmap index'",
                            indexField,
                            indexField,
                            indexField)));
        }
        template =
                template.replaceAll(
                        String.format(PARAMETER_PLACEHOLDER, "column_definition"), String.join(",\n", columnDefinition));

        template = template.replaceAll(String.format(PARAMETER_PLACEHOLDER, "engine_type"), "OLAP");

        // 键
        List<String> keys = dorisConfig.getPrimaryKeys();
        template =
                template.replaceAll(
                        String.format(PARAMETER_PLACEHOLDER, "key_columns"),
                        keys.stream().map(k -> "`" + k + "`").collect(Collectors.joining(",")));

        template =
                template.replaceAll(
                        String.format(PARAMETER_PLACEHOLDER, "table_comment"),
                        "\"" + dorisConfig.getComment() + "\"");

        template =
                template.replaceAll(
                        String.format(PARAMETER_PLACEHOLDER, "distribution_columns"),
                        distributionCols.stream().map(k -> "`" + k + "`").collect(Collectors.joining(",")));
        template =
                template.replaceAll(
                        String.format(PARAMETER_PLACEHOLDER, "distribution_bucket"),
                        distributionBucket);

        String props = "";
        if (properties != null && !properties.isEmpty()) {
            props =
                    properties.entrySet().stream()
                            .map(
                                    entry ->
                                            "\""
                                                    + entry.getKey()
                                                    + "\" = \""
                                                    + entry.getValue()
                                                    + "\"")
                            .collect(Collectors.joining(",\n"));
        }
        template = template.replaceAll(String.format(PARAMETER_PLACEHOLDER, "properties"), props);

        template = template.replaceAll("\n\n", "\n");

        return template;
    }

    /**
     * 字段顺序有一定的规则，需要与键列顺序一致，否则无法建表
     * 根据pkColumns中的顺序将columns重新排序
     *
     * @param columns   表字段列表
     * @param pkColumns key列表
     */
    private static List<TableField> sortFields(List<TableField> columns, List<String> pkColumns) {
        // 根据pkColumns中的顺序将columns重新排序
        List<TableField> reorderedColumns = pkColumns.stream()
                .map(pkColumn -> columns.stream()
                        .filter(column -> column.getFieldName().equals(pkColumn))
                        .findFirst()
                        .orElse(null))
                .collect(Collectors.toList());

        // 添加非pkColumns的字段到reorderedColumns中
        columns.stream()
                .filter(column -> !pkColumns.contains(column.getFieldName()))
                .forEach(reorderedColumns::add);

        return reorderedColumns;
    }

    private static DorisConfig buildDorisConfig(Target target) {
        SyncDatasourceConfiguration dorisConfiguration = JsonUtil.parseObject(target.getDatasource().getConfiguration(), Doris.class);
        DorisProperty dorisProperty = JsonUtil.parseObject(target.getTargetProperty(), DorisProperty.class);
        List<String> primaryKeys = target.getFieldList().stream().filter(TableField::isFieldPk).map(TableField::getFieldName).collect(Collectors.toList());
        List<String> indexFields = target.getFieldList().stream().filter(TableField::isFieldIndex).map(TableField::getFieldName).collect(Collectors.toList());
        DorisConfig dorisConfig = DorisConfig.builder()
                .database(dorisConfiguration.getDataBase())
                .tableName(target.getTableName())
                .primaryKeys(primaryKeys)
                .indexFields(indexFields)
                .columns(target.getFieldList())
                .build();
        // 分区配置
        boolean partitionEnabled = "on".equalsIgnoreCase(dorisProperty.getPartitionEnable());
        if (partitionEnabled) {
            boolean dynamicEnable = "on".equalsIgnoreCase(dorisProperty.getDynamicPartitionEnable());
            String partitionType = "List".equalsIgnoreCase(dorisProperty.getPartitionType()) ? "List" : "Range";
            String rangeStart = "";
            String rangeEnd = "";
            if ("NumberRange".equalsIgnoreCase(dorisProperty.getPartitionType())) {
                rangeStart = String.valueOf(dorisProperty.getManualPartitionStart());
                rangeEnd = String.valueOf(dorisProperty.getManualPartitionEnd());
            }
            // 动态分区不需要时间范围
            if ("DateRange".equalsIgnoreCase(dorisProperty.getPartitionType()) && !dynamicEnable) {
                List<String> partitionTimeRangeList = JsonUtil.parseList(dorisProperty.getManualPartitionTimeRange(),new TypeReference<List<String>>() {});
                rangeStart = partitionTimeRangeList.get(0);
                rangeEnd = partitionTimeRangeList.get(1);
            }
            if ("List".equalsIgnoreCase(dorisProperty.getPartitionType())) {
                rangeStart = PARAMETER_PARTITION_NAME_PREFIX + dorisProperty.getPartitionColumn();
                rangeEnd = dorisProperty.getManualPartitionColumnValue();
            }
            // 手动日期分区间隔单位
            String partitionTimeUnitStr = dorisProperty.getManualPartitionTimeUnit();
            // 动态日期分区间隔单位
            String dynamicPartitionTimeUnitStr = dorisProperty.getDynamicPartitionTimeUnit();

            PartitionTimeUnitEnum partitionTimeUnit;
            if (StringUtils.isNotEmpty(partitionTimeUnitStr) && !dynamicEnable) {
                partitionTimeUnit = PartitionTimeUnitEnum.valueOf(partitionTimeUnitStr.toUpperCase());
                dorisConfig.setPartitionTimeUnitEnum(partitionTimeUnit);
            }
            if (StringUtils.isNotEmpty(dynamicPartitionTimeUnitStr) && dynamicEnable) {
                partitionTimeUnit = PartitionTimeUnitEnum.valueOf(dynamicPartitionTimeUnitStr.toUpperCase());
                dorisConfig.setDynamicPartitionTimeUnitEnum(partitionTimeUnit);
            }
            dorisConfig.setPartitionEnable(true);
            dorisConfig.setPartitionType(partitionType);
            dorisConfig.setPartitionColumn(dorisProperty.getPartitionColumn());
            dorisConfig.setDynamicEnable(dynamicEnable);
            dorisConfig.setDynamicPartitionEnd(dorisProperty.getDynamicPartitionEnd());
            dorisConfig.setPartitionRange(Map.of("start", rangeStart, "end", rangeEnd));
            dorisConfig.setPartitionInterval(dorisProperty.getManualPartitionInterval());
        }
        buildProperties(dorisConfig);
        return dorisConfig;
    }

    /**
     * 构建表特性
     *
     * @param dorisConfig 建表参数
     */
    private static void buildProperties(DorisConfig dorisConfig) {
        Map<String, String> defaultCreateProperties = new HashMap<>(DorisCatalogTemplate.DEFAULT_CREATE_PROPERTIES);
        // 动态分区参数
        if (dorisConfig.isDynamicEnable()) {
            defaultCreateProperties.put(DorisCatalogTemplate.DYNAMIC_PARTITION_ENABLED, "true");
            defaultCreateProperties.put(DorisCatalogTemplate.DYNAMIC_PARTITION_TIME_UNIT, dorisConfig.getDynamicPartitionTimeUnitEnum().getUnit());
            defaultCreateProperties.put(DorisCatalogTemplate.DYNAMIC_PARTITION_END, String.valueOf(dorisConfig.getDynamicPartitionEnd()));
            defaultCreateProperties.put(DorisCatalogTemplate.DYNAMIC_PARTITION_PREFIX, "P");
        }
        dorisConfig.setProperties(defaultCreateProperties);
    }

    /**
     * 分区配置
     * 单列分区
     * 动态分区仅支持日期分区
     *
     * @param dorisConfig sink参数
     * @param properties  表参数
     * @param template    表结构sql
     * @param columns     表字段
     */
    private static String buildTablePartition(DorisConfig dorisConfig, Map<String, String> properties, String template, List<TableField> columns) {
        // 动态分区
        boolean dynamicPartition = dorisConfig.isDynamicEnable();
        // 分区方式
        String partitionType = dorisConfig.getPartitionType();
        // 分区字段
        String partitionColumn = dorisConfig.getPartitionColumn();
        List<TableField> filterColumns = columns.stream().filter(v -> partitionColumn.equalsIgnoreCase(v.getFieldName())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(filterColumns)) {
            throw new RuntimeException("分区字段不存在或为空!");
        }
        String partitionTemplate = "PARTITION BY ${partition_type} ( ${partition_column} )\n"
                + "(\n"
                + "${partition_info}\n"
                + ")\n";
        template = template.replace("${partition}",
                partitionTemplate);
        TableField column = filterColumns.get(0);
        template =
                template.replaceAll(
                        String.format(PARAMETER_PLACEHOLDER, "partition_type"),
                        partitionType.toUpperCase());
        template =
                template.replaceAll(
                        String.format(PARAMETER_PLACEHOLDER, "partition_column"),
                        "`" + partitionColumn + "`");

        // 动态分区
        if (dynamicPartition) {
            template =
                    template.replaceAll(
                            String.format(PARAMETER_PLACEHOLDER, "partition_info"),
                            "");
            properties.put(DorisCatalogTemplate.DYNAMIC_PARTITION_ENABLED, "true");
            properties.put(DorisCatalogTemplate.DYNAMIC_PARTITION_TIME_UNIT, dorisConfig.getDynamicPartitionTimeUnitEnum().name());
            properties.put(DorisCatalogTemplate.DYNAMIC_PARTITION_END, String.valueOf(dorisConfig.getDynamicPartitionEnd()));
            return template;
        } else {
            // 手动分区
            return customPartition(dorisConfig, column, partitionType, template);
        }
    }

    /**
     * 自定义分区
     * 处理 Range List
     * 日期类型 支持 DATE, DATETIME
     * 数值类型 INT, BIGINT, TINYINT, DOUBLE, FLOAT
     * 字符串类型,理论上支持所有类型 BOOLEAN, TINYINT, SMALLINT, INT, BIGINT, LARGEINT, DATE, DATETIME, CHAR, VARCHAR
     *
     * @param dorisConfig   sink配置
     * @param column        分区字段
     * @param partitionType 分区类型
     * @param template      建表脚本模版
     */
    private static String customPartition(DorisConfig dorisConfig, TableField column, String partitionType, String template) {
        switch (PartitionType.valueOf(partitionType)) {
            // 区间分区支持时间以及数值
            case Range -> {
                // 根据分区字段类型判断
                DorisDataType columnDataType = DorisDataType.valueOf(column.getFieldType());
                switch (columnDataType) {
                    case DATE, DATETIME -> {
                        return buildPartitionInfoByDate(dorisConfig, template);
                    }
                    case INT, BIGINT, TINYINT, DOUBLE, FLOAT -> {
                        return buildPartitionInfoByNumber(dorisConfig, template);
                    }
                    default ->
                            throw new RuntimeException("字段数据类型[" + columnDataType + "]不支持分区!(DATE、DATETIME、INT, BIGINT, TINYINT, DOUBLE, FLOAT)");
                }
            }
            case List -> {
                // 从Doris配置中获取分区列的Map
                Map<String, String> rangeMap = dorisConfig.getPartitionRange();
                template = template.replaceAll(String.format(PARAMETER_PLACEHOLDER, "partition_info"), String.format("PARTITION `%s` VALUES IN (%s)", rangeMap.get("start"), rangeMap.get("end")));
            }
            default -> throw new RuntimeException("不支持的分区类型-" + partitionType);
        }

        return template;
    }


    /**
     * 构建数值类型字段分区配置
     *
     * @param dorisConfig sink配置
     * @param template    模板字符串
     * @return 构建后的分区信息字符串
     */
    private static String buildPartitionInfoByNumber(DorisConfig dorisConfig, String template) {
        List<String> partitionValueList = new ArrayList<>();
        // 从Doris配置中获取分区范围的Map
        Map<String, String> rangeMap = dorisConfig.getPartitionRange();
        // 获取起始值和终止值(如果不存在则使用默认值0)
        int start = Integer.parseInt(rangeMap.getOrDefault("start", "0"));
        int end = Integer.parseInt(rangeMap.getOrDefault("end", "0"));
        // 获取分区间隔值
        int period = dorisConfig.getPartitionInterval();
        int currentStartNumber = start;
        while (currentStartNumber <= end) {
            String partitionName = String.valueOf(currentStartNumber);
            String partitionValue = String.valueOf(currentStartNumber);
            partitionValueList.add(createPartition(partitionName, partitionValue));
            // 计算下一个周期的开始值
            currentStartNumber = currentStartNumber + period;
        }
        // 将模板中的参数占位符替换为构建好的分区信息
        template =
                template.replaceAll(
                        String.format(PARAMETER_PLACEHOLDER, "partition_info"),
                        String.join(",", partitionValueList));
        return template;
    }


    /**
     * 构建日期类型字段分区配置
     *
     * @param dorisConfig sink配置
     * @param template    建表脚本模版
     */
    private static String buildPartitionInfoByDate(DorisConfig dorisConfig, String template) {
        Map<String, String> rangeMap = dorisConfig.getPartitionRange();
        String start = rangeMap.get("start");
        String end = rangeMap.get("end");
        ZonedDateTime startTime = buildZonedDateTime(start);
        ZonedDateTime endTime = buildZonedDateTime(end).plusHours(23).plusMinutes(59).plusSeconds(59);
        PartitionTimeUnitEnum unit = dorisConfig.getPartitionTimeUnitEnum();
        List<String> partitionValueList = new ArrayList<>();
        switch (unit) {
            case HOUR -> buildPartitionsByDuration(startTime, endTime, Duration.ofHours(1), unit, partitionValueList);
            case DAY -> buildPartitionsByPeriod(startTime, endTime, Period.ofDays(1), unit, partitionValueList);
            case WEEK -> buildPartitionsByPeriod(startTime, endTime, Period.ofWeeks(1), unit, partitionValueList);
            case MONTH -> buildPartitionsByPeriod(startTime, endTime, Period.ofMonths(1), unit, partitionValueList);
            case YEAR -> buildPartitionsByPeriod(startTime, endTime, Period.ofYears(1), unit, partitionValueList);
            default -> {
            }
        }
        if (CollectionUtils.isNotEmpty(partitionValueList)) {
            template =
                    template.replaceAll(
                            String.format(PARAMETER_PLACEHOLDER, "partition_info"),
                            String.join(",", partitionValueList));
        }
        return template;
    }

    /**
     * 构建时区时间对象
     *
     * @param targetTimeStr 时间
     * @return ZonedDateTime
     */
    private static ZonedDateTime buildZonedDateTime(String targetTimeStr) {
        ZoneId serverTimeZone = TimeZone.getDefault().toZoneId();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return ZonedDateTime.of(LocalDate.parse(targetTimeStr, dateTimeFormatter).atStartOfDay(), serverTimeZone);
    }

    /**
     * 构建区间间隔为天、周、月、年的分区值
     *
     * @param startTime             开始时间
     * @param endTime               结束时间
     * @param period                时间间隔
     * @param partitionTimeUnitEnum 分区时间间隔单位
     * @param partitionValueList    分区值脚本集合
     */
    private static void buildPartitionsByPeriod(ZonedDateTime startTime, ZonedDateTime endTime,
                                                Period period, PartitionTimeUnitEnum partitionTimeUnitEnum,
                                                List<String> partitionValueList) {
        ZonedDateTime currentDateTime = startTime;
        while (currentDateTime.isBefore(endTime) || currentDateTime.isEqual(endTime)) {
            // 获取当前时间的分区名称和分区值
            String partitionName = currentDateTime.format(DateTimeFormatter.ofPattern(partitionTimeUnitEnum.getNameFormatter()));
            // 将分区名称和分区值添加到分区值集合中
            String partitionValue = currentDateTime.format(DateTimeFormatter.ofPattern(partitionTimeUnitEnum.getValueFormatter()));
            partitionValueList.add(createPartition(partitionName, partitionValue));
            // 计算下一个周期的开始时间
            currentDateTime = currentDateTime.plus(period);
        }
    }

    /**
     * 构建区间间隔为小时的分区值
     *
     * @param startTime             开始时间
     * @param endTime               结束时间
     * @param duration              时间间隔
     * @param partitionTimeUnitEnum 分区时间间隔单位
     * @param partitionValueList    分区值集合
     */
    private static void buildPartitionsByDuration(ZonedDateTime startTime, ZonedDateTime endTime,
                                                  Duration duration, PartitionTimeUnitEnum partitionTimeUnitEnum,
                                                  List<String> partitionValueList) {
        ZonedDateTime currentDateTime = startTime;
        while (currentDateTime.isBefore(endTime) || currentDateTime.isEqual(endTime)) {
            // 获取当前时间的分区名称和分区值
            String partitionName = currentDateTime.format(DateTimeFormatter.ofPattern(partitionTimeUnitEnum.getNameFormatter()));
            String partitionValue = currentDateTime.format(DateTimeFormatter.ofPattern(partitionTimeUnitEnum.getValueFormatter()));
            // 将分区名称和分区值添加到分区值集合中
            partitionValueList.add(createPartition(partitionName, partitionValue));
            // 计算下一个周期的开始时间
            currentDateTime = currentDateTime.plus(duration);
        }
    }

    /**
     * 创建区间分区值脚本
     *
     * @param partitionName  分区名称
     * @param partitionValue 分区值
     */
    private static String createPartition(String partitionName, String partitionValue) {
        return String.format(
                "PARTITION `%s%s` VALUES LESS THAN ('%s')\n",
                PARAMETER_PARTITION_NAME_PREFIX, partitionName, partitionValue
        );
    }

    /**
     * 构建字段定义内容
     * 包含字段BITMAP索引配置
     *
     * @param dorisConfig 建表参数
     * @param columns     字段内容
     * @return String 字段定义内容
     */
    private static List<String> buildColumnDefinition(DorisConfig dorisConfig, List<TableField> columns) {
        List<String> columnList = new ArrayList<>();
        for (TableField column : columns) {
            String name = column.getFieldName();
            DorisDataType type = DorisDataType.valueOf(column.getFieldType());
            // 找不到字段类型的忽略字段
            if (DorisDataType.UNKNOWN.name().equalsIgnoreCase(type.name())) {
                continue;
            }
            int columnLength = column.getFieldSize();
            // 精度
            int precisionLength = 0;
            // 部分类型类型不要长度
            switch (type) {
                case DOUBLE, DATETIME, DATE, BOOLEAN -> columnLength = 0;
                case DECIMAL -> {
                    // 设置默认精度
                    columnLength = 0;
                    precisionLength = 1;
                }
                default -> {
                }
            }
            boolean nullable = column.isFieldPk() || column.isFieldIndex();
            Object defaultValue = column.getDefaultValue();
            String comment = column.getRemarks();
            columnList.add(
                    String.format(
                            "`%s` %s%s%s%s%s%s",
                            name,
                            type,
                            columnLength > 0 ? " (" + columnLength + ")" : "",
                            precisionLength > 0 ? " (9, 0)" : "",
                            nullable ? " NOT NULL" : " NULL",
                            nullable || defaultValue == null
                                    ? ""
                                    : " DEFAULT \"" + defaultValue + "\"",
                            " COMMENT \"" + comment + "\""));
        }
        // 默认添加一个入库事件字段 SeaTunnel 同步，无法设置默认时间
        // columnList.add("`ds_data_insert_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT \"内置数据入库时间字段\"");
        return columnList;
    }

}
