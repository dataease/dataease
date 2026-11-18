package io.dataease.extensions.sync.model;

import lombok.Data;

import java.util.List;

/**
 * 当前目标连接的字段命名规则，由目标插件唯一提供，供编辑和维护入口共同使用
 */
@Data
public class SinkFieldNameRules {
    private String version;
    private String pattern;
    private int maxLength;
    private boolean caseSensitive;
    private List<String> reservedPrefixes;
    private boolean reservedPrefixCaseSensitive;
    private List<String> protectedPrefixes;
    private String unsupportedPattern;
    private String description;
    private String documentationUrl;
}
