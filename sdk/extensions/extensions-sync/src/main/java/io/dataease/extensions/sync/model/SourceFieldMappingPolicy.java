package io.dataease.extensions.sync.model;

/**
 * 源字段到目标字段的默认映射策略。
 *
 * @author jianneng
 */
public enum SourceFieldMappingPolicy {
    /**
     * 可由目标数据源自动选择类型。
     */
    AUTO,
    /**
     * 源端连接器无法可靠验证类型转换，需由用户选择目标类型。
     */
    REQUIRE_MANUAL_MAPPING
}
