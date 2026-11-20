package io.dataease.api.visualization.request;

import java.io.Serializable;

public record VisualizationDraftRevision(Boolean checkHistory, String expectedContentId, String contentId)
        implements Serializable {
}
