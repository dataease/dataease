package io.dataease.job.sechedule.strategy;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TaskStrategyFactory {


    private static Map<String, TaskHandler> strategyMap = new ConcurrentHashMap<>();

    public static TaskHandler getInvokeStrategy(String name) {
        return strategyMap.get(name);
    }

    public static void register(String name, TaskHandler handler) {
        if (StringUtils.isEmpty(name) || null == handler) {
            return;
        }
        strategyMap.put(name, handler);
    }
}
