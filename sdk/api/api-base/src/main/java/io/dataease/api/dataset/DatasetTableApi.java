package io.dataease.api.dataset;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.dataset.dto.MultFieldValuesRequest;
import io.dataease.api.dataset.engine.SQLFunctionDTO;
import io.dataease.dto.dataset.DatasetTableFieldDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * @Author Junjun
 */
@Tag(name = "数据集管理:表")
@ApiSupport(order = 977)
public interface DatasetTableApi {

    /**
     * 该接口用于视图计算字段单独保存
     *
     * @param datasetTableFieldDTO
     * @return
     * @throws Exception
     */
    @PostMapping("save")
    DatasetTableFieldDTO save(@RequestBody DatasetTableFieldDTO datasetTableFieldDTO) throws Exception;

    @PostMapping("get/{id}")
    DatasetTableFieldDTO get(@PathVariable Long id);

    @PostMapping("listByDatasetGroup/{id}")
    List<DatasetTableFieldDTO> listByDatasetGroup(@PathVariable Long id);

    @PostMapping("delete/{id}")
    void delete(@PathVariable Long id);

    @PostMapping("listByDQ/{id}")
    Map<String, List<DatasetTableFieldDTO>> listByDQ(@PathVariable Long id);

    @GetMapping ("listWithPermissions/{id}")
    List<DatasetTableFieldDTO> listFieldsWithPermissions(@PathVariable Long id);


    @PostMapping("multFieldValuesForPermissions")
    List<String> multFieldValuesForPermissions(@RequestBody MultFieldValuesRequest multFieldValuesRequest) throws Exception;

    @PostMapping("getFunction")
    List<SQLFunctionDTO> getFunction();

    @PostMapping("deleteByChartId/{id}")
    void deleteByChartId(@PathVariable Long id);
}
