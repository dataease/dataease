package io.dataease.api.permissions.dataset.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.dataease.api.permissions.dataset.dto.DataSetColumnPermissionsDTO;
import io.dataease.api.permissions.dataset.dto.DataSetRowPermissionsTreeDTO;
import io.dataease.api.permissions.dataset.dto.Item;
import io.dataease.auth.DeApiPath;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import static io.dataease.constant.AuthResourceEnum.DATASET;


@DeApiPath(value = "/dataset/columnPermissions", rt = DATASET)
public interface ColumnPermissionsApi {

    @GetMapping("/pager/{datasetId}/{goPage}/{pageSize}")
    public IPage<DataSetColumnPermissionsDTO> columnPermissions(@PathVariable Long datasetId, @PathVariable int goPage, @PathVariable int pageSize);

    @PostMapping("save")
    public void save(@RequestBody DataSetColumnPermissionsDTO dataSetColumnPermissionsDTO);

    @PostMapping("/delete")
    public void delete(@RequestBody DataSetColumnPermissionsDTO dataSetColumnPermissionsDTO);


    public List<DataSetColumnPermissionsDTO> list(@RequestBody DataSetColumnPermissionsDTO request);

}
