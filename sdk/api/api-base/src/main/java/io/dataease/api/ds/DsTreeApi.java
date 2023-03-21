package io.dataease.api.ds;

import io.dataease.api.ds.vo.DataSourceDTO;
import io.dataease.api.ds.vo.DsTreeDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface DsTreeApi {
    /**
     * 查询数据源树
     * @param keyWord 过滤关键字
     * @return
     */
    @GetMapping("/query/{keyWord}")
    List<DsTreeDTO> query(@PathVariable("keyWord") String keyWord);

    @PostMapping("/save")
    void save(@RequestBody DataSourceDTO dataSourceDTO);

    @PostMapping("/update")
    void update(@RequestBody DataSourceDTO dataSourceDTO);

}
