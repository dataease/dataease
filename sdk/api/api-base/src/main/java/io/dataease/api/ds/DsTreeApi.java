package io.dataease.api.ds;

import io.dataease.api.ds.dto.DsTreeDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

public interface DsTreeApi {
    /**
     * 查询数据源树
     * @param keyWord 过滤关键字
     * @return
     */
    @GetMapping("/query/{keyWord}")
    List<DsTreeDTO> query(@PathVariable("keyWord") String keyWord);

    @GetMapping("/testQuery")
    List<Map> testQuery();
}
