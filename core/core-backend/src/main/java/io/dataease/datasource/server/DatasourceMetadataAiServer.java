package io.dataease.datasource.server;

import io.dataease.ai.scene.datasource.AiDatasourceMetadataScenes;
import io.dataease.api.ds.DatasourceMetadataAiApi;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/datasource/metadata")
public class DatasourceMetadataAiServer implements DatasourceMetadataAiApi {
    private final AiDatasourceMetadataScenes scenes;

    public DatasourceMetadataAiServer(AiDatasourceMetadataScenes scenes) {
        this.scenes = scenes;
    }

    @Override
    public Task submit(Long datasourceId, Kind kind, List<String> tables) {
        return scenes.submit(datasourceId, kind, tables);
    }

    @Override
    public Task status(Long datasourceId, Kind kind, String taskId) {
        return scenes.status(datasourceId, kind, taskId);
    }
}
