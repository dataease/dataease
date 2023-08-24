package io.dataease.service.redis.impl;

import com.google.gson.Gson;
import io.dataease.commons.utils.IPUtils;
import io.dataease.commons.utils.LogUtil;
import io.dataease.plugins.common.base.domain.MyPlugin;
import io.dataease.plugins.entity.PluginOperate;
import io.dataease.service.redis.RedisMessageBroadcast;
import io.dataease.service.sys.PluginService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class PluginMsgService implements RedisMessageBroadcast {

    private static Gson json = new Gson();

    @Resource
    private PluginService pluginService;

    @Override
    public void messageCallBack(Object arg) {
        PluginOperate operate = json.fromJson(json.toJson(arg), PluginOperate.class);
        String domain = IPUtils.domain();
        if (StringUtils.equals(domain, operate.getSenderIp())) return;
        String operateType = operate.getType();
        MyPlugin plugin = operate.getPlugin();
        if (StringUtils.equals("install", operateType)) {
            LogUtil.info("start install plugin [{}] in domain {}", plugin.getName(), domain);
            install(plugin);
        }
        if (StringUtils.equals("uninstall", operateType)) {
            LogUtil.info("start uninstall plugin [{}] in domain {}", plugin.getName(), domain);
            uninstall(plugin);
        }
        if (StringUtils.equals("update", operateType)) {
            LogUtil.info("start update plugin [{}] in domain {}", plugin.getName(), domain);
            updateInstall(plugin);
        }
    }

    private void install(MyPlugin plugin) {
        pluginService.redisBroadcastInstall(plugin);
    }

    private void uninstall(MyPlugin plugin) {
        pluginService.redisBroadcastUnInstall(plugin);
    }

    private void updateInstall(MyPlugin plugin) {
        if (pluginService.redisBroadcastUnInstall(plugin)) {
            pluginService.redisBroadcastInstall(plugin);
        }
    }
}
