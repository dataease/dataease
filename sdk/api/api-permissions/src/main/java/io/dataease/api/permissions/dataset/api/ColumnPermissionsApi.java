package io.dataease.api.permissions.dataset.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.dataease.api.permissions.dataset.dto.DataSetColumnPermissionsDTO;
import io.dataease.auth.DeApiPath;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import static io.dataease.constant.AuthResourceEnum.DATASET;

@Hidden
@Tag(name = "列权限")
@DeApiPath(value = "/dataset/columnPermissions", rt = DATASET)
public interface ColumnPermissionsApi {

    @Operation(summary = "查询列权限列表")
    @GetMapping("/pager/{datasetId}/{goPage}/{pageSize}")
    public IPage<DataSetColumnPermissionsDTO> columnPermissions(@PathVariable("datasetId") Long datasetId, @PathVariable("goPage") int goPage, @PathVariable("pageSize") int pageSize);

    @Operation(summary = "保存")
    @PostMapping("save")
    public void save(@RequestBody DataSetColumnPermissionsDTO dataSetColumnPermissionsDTO);

    @Operation(summary = "删除")
    @PostMapping("/delete")
    public void delete(@RequestBody DataSetColumnPermissionsDTO dataSetColumnPermissionsDTO);

    @Operation(summary = "获取详细信息")
    @PostMapping("/info")
    public DataSetColumnPermissionsDTO DataSetColumnPermissionInfo(@RequestBody DataSetColumnPermissionsDTO request);

    public List<DataSetColumnPermissionsDTO> list(@RequestBody DataSetColumnPermissionsDTO request);

}
