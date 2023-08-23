package io.dataease.plugins.xpack.theme.service;

import java.util.List;

import io.dataease.plugins.xpack.theme.dto.*;

import io.dataease.plugins.common.service.PluginComponentService;

public abstract class ThemeXpackService extends PluginComponentService{

    public abstract List<ThemeBaseDTO> themes(List<String> itemKeys);

    public abstract void addTheme(ThemeCreateRequest request);

    public abstract void renameTheme(ThemeRenameRequest request);

    public abstract void switchSenior(Integer themeId);

    public abstract void switchStatus(Integer themeId);

    public abstract void saveThemeItems(ThemeRequest request);

    public abstract List<ThemeItem> queryItems(int id);

    public abstract void deleteTheme(int id);
    
}
