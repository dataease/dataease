package io.dataease.api.permissions.dataset.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.dataease.api.permissions.dataset.dto.*;
import io.dataease.api.permissions.user.vo.UserFormVO;
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
@Tag(name = "行权限")
@DeApiPath(value = "/dataset/rowPermissions", rt = DATASET)
public interface RowPermissionsApi {

    @Operation(summary = "查询行权限列表")
    @GetMapping("/pager/{datasetId}/{goPage}/{pageSize}")
    public IPage<DataSetRowPermissionsTreeDTO> rowPermissions(@PathVariable("datasetId") Long datasetId, @PathVariable("goPage") int goPage, @PathVariable("pageSize") int pageSize);

    @Operation(summary = "保存")
    @PostMapping("save")
    public void save(@RequestBody DataSetRowPermissionsTreeDTO datasetRowPermissions);

    @Operation(summary = "删除")
    @PostMapping("/delete")
    public void delete(@RequestBody DataSetRowPermissionsTreeDTO datasetRowPermissions);

    @Operation(summary = "授权对象")
    @GetMapping("/authObjs/{datasetId}/{type}")
    public List<Item> authObjs(@PathVariable("datasetId") Long datasetId, @PathVariable("type") String type);

    @Operation(summary = "获取详细信息")
    @PostMapping("/dataSetRowPermissionInfo")
    public DataSetRowPermissionsTreeDTO dataSetRowPermissionInfo(@RequestBody DataSetRowPermissionsTreeDTO request);

    @Operation(summary = "白名单")
    @PostMapping("/whiteListUsers")
    public List<UserFormVO> whiteListUsers(@RequestBody WhiteListUsersRequest request);

    public UserFormVO getUserById(Long id);

    public List<DataSetRowPermissionsTreeDTO> list(DatasetRowPermissionsTreeRequest dataSetRowPermissionsTreeDTO) ;
}
