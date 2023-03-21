package io.dataease.api.dataset;

import io.dataease.api.dataset.vo.DatasetTreeNodeVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface DatasetTreeApi {

    @GetMapping("/query/{keyword}")
    List<DatasetTreeNodeVO> query(@PathVariable("keyword") String keyword);
}
