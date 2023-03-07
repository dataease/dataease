package io.dataease.api.dataset;

import io.dataease.api.dataset.vo.DataSetTreeNodeVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface DataSetTreeApi {

    @GetMapping("/query/{keyword}")
    List<DataSetTreeNodeVO> query(@PathVariable("keyword") String keyword);
}
