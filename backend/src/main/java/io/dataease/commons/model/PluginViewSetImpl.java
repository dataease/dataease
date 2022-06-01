package io.dataease.commons.model;

import io.dataease.plugins.common.base.domain.Datasource;
import io.dataease.plugins.view.entity.PluginViewSet;
import lombok.Data;

@Data
public class PluginViewSetImpl extends PluginViewSet {

    private Datasource ds;
}
