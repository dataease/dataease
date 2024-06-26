package io.dataease.api.dataset;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.dataset.dto.DatasetNodeDTO;
import io.dataease.api.dataset.union.DatasetGroupInfoDTO;
import io.dataease.api.dataset.vo.DataSetBarVO;
import io.dataease.auth.DeApiPath;
import io.dataease.auth.DePermit;
import io.dataease.extensions.datasource.dto.DatasetTableDTO;
import io.dataease.extensions.view.dto.SqlVariableDetails;
import io.dataease.model.BusiNodeRequest;
import io.dataease.model.BusiNodeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import static io.dataease.constant.AuthResourceEnum.DATASET;

@Tag(name = "数据集管理:树")
@ApiSupport(order = 979)
@DeApiPath(value = "/datasetTree", rt = DATASET)
public interface DatasetTreeApi {

    /**
     * 编辑
     *
     * @param dto
     * @return
     * @throws Exception
     */
    @Operation(summary = "保存数据集", hidden = true)
    @DePermit({"#p0.id+':manage'"})
    @PostMapping("save")
    DatasetNodeDTO save(@RequestBody DatasetGroupInfoDTO dto) throws Exception;

    @Operation(summary = "重命名数据集")
    @DePermit({"#p0.id+':manage'"})
    @PostMapping("rename")
    DatasetNodeDTO rename(@RequestBody DatasetGroupInfoDTO dto) throws Exception;

    /**
     * 新建
     *
     * @param dto
     * @return
     * @throws Exception
     */
    @Operation(summary = "创建数据集")
    @DePermit({"#p0.pid+':manage'"})
    @PostMapping("create")
    DatasetNodeDTO create(@RequestBody DatasetGroupInfoDTO dto) throws Exception;

    @Operation(summary = "移动数据集")
    @DePermit({"#p0.id+':manage'", "#p0.pid+':manage'"})
    @PostMapping("move")
    DatasetNodeDTO move(@RequestBody DatasetGroupInfoDTO dto) throws Exception;

    @Operation(summary = "删除数据集")
    @DePermit({"#p0+':manage'"})
    @PostMapping("delete/{id}")
    void delete(@PathVariable("id") Long id);

    @Operation(summary = "查询文件夹以及数据集tree")
    @PostMapping("tree")
    List<BusiNodeVO> tree(@RequestBody BusiNodeRequest request);

    @Operation(summary = "查询数据集对应用户信息")
    @GetMapping("/barInfo/{id}")
    DataSetBarVO barInfo(@PathVariable("id") Long id);

    @Operation(summary = "查询数据集")
    @PostMapping("get/{id}")
    DatasetGroupInfoDTO get(@PathVariable("id") Long id) throws Exception;

    @Operation(summary = "获取数据集详情")
    @PostMapping("details/{id}")
    DatasetGroupInfoDTO details(@PathVariable("id") Long id) throws Exception;

    @Operation(summary = "获取数据集详情")
    @PostMapping("dsDetails")
    List<DatasetTableDTO> panelGetDsDetails(@RequestBody List<Long> ids) throws Exception;

    @Operation(summary = "获取SQL参数")
    @PostMapping("getSqlParams")
    List<SqlVariableDetails> getSqlParams(@RequestBody List<Long> ids) throws Exception;

    @Operation(summary = "带权限查询数据集详情")
    @PostMapping("detailWithPerm")
    List<DatasetTableDTO> detailWithPerm(@RequestBody List<Long> ids) throws Exception;
}
