package io.dataease.plugins.common.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import io.dataease.plugins.common.dto.StaticResource;
import org.apache.commons.lang3.StringUtils;

public abstract class PluginComponentService {

    private static final String SPLIT = ".";
    private static final String DEFAULT_SUFFIX = "js";
    private static final List emptyResult = new ArrayList();

    public abstract List<String> components();

    public List<StaticResource> staticResources(){
        return emptyResult;
    }

    protected abstract InputStream readContent(String name);

    public InputStream vueResource(String name) {
        return vueResource(name, DEFAULT_SUFFIX);
    }

    public InputStream vueResource(String name, String suffix) {
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(suffix)) return null;
        String lastName = SPLIT + suffix;
        if (!name.endsWith(lastName)) name += lastName;

        return readContent(name);
    }
    
}
