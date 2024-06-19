package io.dataease.plugins.view.handler.impl;

import io.dataease.plugins.view.entity.PluginViewParam;
import io.dataease.plugins.view.handler.PluginViewYOYHandler;

import java.util.List;

/**
 * @Author Junjun
 */
public class DefaultViewYOYHandler implements PluginViewYOYHandler {
    @Override
    public List<String[]> yoy(PluginViewParam pluginViewParam, List<String[]> data) {
        return data;
    }
}
