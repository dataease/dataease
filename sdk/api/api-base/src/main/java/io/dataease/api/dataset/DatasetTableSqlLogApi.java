package io.dataease.api.dataset;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.dataset.dto.SqlLogDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @Author Junjun
 */
@Tag(name = "数据集管理:SQL日志")
@ApiSupport(order = 977)
public interface DatasetTableSqlLogApi {
    @PostMapping("save")
    void save(@RequestBody SqlLogDTO sqlLogDTO) throws Exception;

    @PostMapping("listByTableId")
    List<SqlLogDTO> listByTableId(@RequestBody SqlLogDTO sqlLogDTO) throws Exception;

    @PostMapping("deleteByTableId/{id}")
    void deleteByTableId(@PathVariable String id) throws Exception;
}
