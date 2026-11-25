package io.dataease.exportCenter.manage;

import io.dataease.utils.LogUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;

/**
 * 导出任务的节点身份。
 * 配置了 quartz.properties.org.quartz.scheduler.instanceId（非 AUTO）时直接使用该稳定值；
 * 为 AUTO 或未配置时使用本机 hostname，避免每次重启的 hostname+timestamp 变化。
 * 恢复逻辑会把 hostname 与 QRTZ_SCHEDULER_STATE 中 AUTO 生成的 hostname+timestamp 实例名
 * 做匹配，判断节点是否存活。
 */
@Component
public class ExportNodeIdentity {

    @Value("${quartz.properties.org.quartz.scheduler.instanceId:}")
    private String quartzInstanceId;

    public String currentInstanceId() {
        if (StringUtils.isNotBlank(quartzInstanceId) && !"AUTO".equals(quartzInstanceId)) {
            return quartzInstanceId;
        }
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            LogUtil.warn("Failed to resolve local hostname for export node identity", e);
            return "unknown";
        }
    }
}
