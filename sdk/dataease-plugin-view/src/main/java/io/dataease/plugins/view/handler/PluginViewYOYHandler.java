package io.dataease.plugins.view.handler;

import io.dataease.plugins.view.entity.PluginViewParam;

import java.util.List;

/**
 * @Author Junjun
 */
public interface PluginViewYOYHandler {
    List<String[]> yoy(PluginViewParam pluginViewParam, List<String[]> data);
}
