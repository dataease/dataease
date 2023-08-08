package io.dataease.api.dataset;

import io.dataease.api.dataset.dto.SqlLogDTO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @Author Junjun
 */
public interface DatasetTableSqlLogApi {
    @PostMapping("save")
    void save(@RequestBody SqlLogDTO sqlLogDTO) throws Exception;

    @PostMapping("listByTableId")
    List<SqlLogDTO> listByTableId(@RequestBody SqlLogDTO sqlLogDTO) throws Exception;

    @PostMapping("deleteByTableId/{id}")
    void deleteByTableId(@PathVariable String id) throws Exception;
}
