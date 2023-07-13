package io.dataease.api.permissions.dataset.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.dataease.api.permissions.dataset.dto.*;
import io.dataease.api.permissions.user.vo.UserFormVO;
import io.dataease.auth.DeApiPath;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import static io.dataease.constant.AuthResourceEnum.DATASET;


@DeApiPath(value = "/dataset/rowPermissions", rt = DATASET)
public interface RowPermissionsApi {

    @GetMapping("/pager/{datasetId}/{goPage}/{pageSize}")
    public IPage<DataSetRowPermissionsTreeDTO> rowPermissions(@PathVariable Long datasetId, @PathVariable int goPage, @PathVariable int pageSize);


    @PostMapping("save")
    public void save(@RequestBody DataSetRowPermissionsTreeDTO datasetRowPermissions);

    @PostMapping("/list")
    public List<DataSetRowPermissionsTreeDTO> rowPermissions(@RequestBody DataSetRowPermissionsTreeDTO request);


    @PostMapping("/delete")
    public void delete(@RequestBody DataSetRowPermissionsTreeDTO datasetRowPermissions);


    @GetMapping("/authObjs/{datasetId}/{type}")
    public List<Item> authObjs(@PathVariable Long datasetId, @PathVariable String type);

    @PostMapping("/dataSetRowPermissionInfo")
    public DataSetRowPermissionsTreeDTO dataSetRowPermissionInfo(@RequestBody DataSetRowPermissionsTreeDTO request);

    @PostMapping("/whiteListUsers")
    public List<UserFormVO> whiteListUsers(@RequestBody WhiteListUsersRequest request);

    public UserFormVO getUserById(Long id);

    public List<DataSetRowPermissionsTreeDTO> list(DatasetRowPermissionsTreeRequest dataSetRowPermissionsTreeDTO) ;
}
