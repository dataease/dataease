package io.dataease.extensions.view.dto;

import lombok.Data;

import java.util.List;

/**
 * @Author gin
 */
@Data
public class ChartDrillRequest {
    private List<ChartDimensionDTO> dimensionList;
}
