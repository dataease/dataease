package io.dataease.api.dataset;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.dataset.dto.MultFieldValuesRequest;
import io.dataease.api.dataset.engine.SQLFunctionDTO;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.swagger.v3.oas.annotations.Operation;
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
     * 该接口用于图表计算字段单独保存
     *
     * @param datasetTableFieldDTO
     * @return
     * @throws Exception
     */
    @Operation(summary = "保存字段")
    @PostMapping("save")
    DatasetTableFieldDTO save(@RequestBody DatasetTableFieldDTO datasetTableFieldDTO) throws Exception;

    @Operation(summary = "查询字段")
    @PostMapping("get/{id}")
    DatasetTableFieldDTO get(@PathVariable Long id);

    @Operation(summary = "获取数据集字段")
    @PostMapping("listByDatasetGroup/{id}")
    List<DatasetTableFieldDTO> listByDatasetGroup(@PathVariable Long id);

    @Operation(summary = "获取数据集字段map")
    @PostMapping("listByDsIds")
    Map<String, List<DatasetTableFieldDTO>> listByDsIds(@RequestBody List<Long> ids);

    @Operation(summary = "删除字段")
    @PostMapping("delete/{id}")
    void delete(@PathVariable Long id);

    @Operation(summary = "获取字段分组")
    @PostMapping("listByDQ/{id}")
    Map<String, List<DatasetTableFieldDTO>> listByDQ(@PathVariable Long id);

    @Operation(summary = "获取copilot字段分组")
    @PostMapping("copilotFields/{id}")
    Map<String, List<DatasetTableFieldDTO>> copilotFields(@PathVariable Long id) throws Exception;

    @Operation(summary = "获取字段")
    @GetMapping("listWithPermissions/{id}")
    List<DatasetTableFieldDTO> listFieldsWithPermissions(@PathVariable Long id);

    @Operation(summary = "获取枚举值")
    @PostMapping("multFieldValuesForPermissions")
    List<String> multFieldValuesForPermissions(@RequestBody MultFieldValuesRequest multFieldValuesRequest) throws Exception;

    @Operation(summary = "获取计算字段函数")
    @PostMapping("getFunction")
    List<SQLFunctionDTO> getFunction();

    @Operation(summary = "删除图表计算字段", hidden = true)
    @PostMapping("deleteByChartId/{id}")
    void deleteByChartId(@PathVariable Long id);
}
