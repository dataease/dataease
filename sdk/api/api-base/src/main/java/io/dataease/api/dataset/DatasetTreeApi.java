package io.dataease.api.dataset;

import io.dataease.api.dataset.dto.DatasetNodeDTO;
import io.dataease.api.dataset.union.DatasetGroupInfoDTO;
import io.dataease.api.dataset.vo.DatasetTreeNodeVO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface DatasetTreeApi {

    @PostMapping("save")
    DatasetNodeDTO save(@RequestBody DatasetGroupInfoDTO dto);

    @PostMapping("delete/{id}")
    void delete(@PathVariable String id);

    @PostMapping("tree")
    List<DatasetTreeNodeVO> tree(@RequestBody DatasetNodeDTO datasetNodeDTO);
}
