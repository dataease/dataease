package io.dataease.dataset.server;

import io.dataease.api.dataset.DataSetTreeApi;
import io.dataease.api.dataset.vo.DataSetTreeNodeVO;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/datasetTree")
public class DataSetTreeServer implements DataSetTreeApi {

    @Override
    public List<DataSetTreeNodeVO> query(String keyword) {
        return null;
    }
}
