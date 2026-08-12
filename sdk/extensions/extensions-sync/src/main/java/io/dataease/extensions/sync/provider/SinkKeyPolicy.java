package io.dataease.extensions.sync.provider;

/**
 * 目标连接器的 Key 配置策略。
 */
public enum SinkKeyPolicy {
    /** 用户可以不配置 Key。 */
    OPTIONAL,

    /** 用户必须选择至少一个 Key。 */
    REQUIRED,

    /** 用户可以不配置 Key，目标连接器会在保存任务时自动生成。 */
    PROVIDER_GENERATED
}
