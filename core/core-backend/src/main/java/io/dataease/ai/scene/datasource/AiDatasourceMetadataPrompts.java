package io.dataease.ai.scene.datasource;

import io.dataease.ai.support.AiPromptResources;

final class AiDatasourceMetadataPrompts {
    private AiDatasourceMetadataPrompts() {}

    static final String RELATIONS = AiPromptResources.read("datasource-relations.md");
    static final String SEMANTICS = AiPromptResources.read("datasource-semantics.md");
}
