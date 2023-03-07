package io.dataease.datasource.server;

import io.dataease.api.ds.DsTreeApi;
import io.dataease.api.ds.vo.DsTreeDTO;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dsTree")
public class DsTreeServer implements DsTreeApi {

    @Override
    public List<DsTreeDTO> query(String keyWord) {
        return null;
    }
}
