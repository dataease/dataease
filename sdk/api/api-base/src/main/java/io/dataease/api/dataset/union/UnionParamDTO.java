package io.dataease.api.dataset.union;

import lombok.Data;

import java.util.List;

/**
 * @Author gin
 */
@Data
public class UnionParamDTO {
    private String unionType;
    private List<UnionItemDTO> unionFields;
}
