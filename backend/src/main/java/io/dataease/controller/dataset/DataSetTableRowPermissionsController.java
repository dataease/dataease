package io.dataease.controller.dataset;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.base.domain.DatasetRowPermissions;
import io.dataease.commons.utils.PageUtils;
import io.dataease.commons.utils.Pager;
import io.dataease.controller.sys.base.BaseGridRequest;
import io.dataease.dto.dataset.DataSetRowPermissionsDTO;
import io.dataease.service.dataset.DataSetTableRowPermissionsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;


@Api(tags = "数据集：数据集行权限")
@ApiSupport(order = 90)
@RestController
@RequestMapping("dataset/rowPermissions")
public class DataSetTableRowPermissionsController {
    @Resource
    private DataSetTableRowPermissionsService dataSetTableRowPermissionsService;

    @ApiOperation("保存")
    @PostMapping("save")
    public void save(@RequestBody DatasetRowPermissions datasetRowPermissions) throws Exception {
        dataSetTableRowPermissionsService.save(datasetRowPermissions);
    }


    @ApiOperation("分页查询")
    @PostMapping("/pageList/{datasetId}/{goPage}/{pageSize}")
    public Pager<List<DataSetRowPermissionsDTO>> rowPermissions(@PathVariable String datasetId, @PathVariable int goPage, @PathVariable int pageSize, @RequestBody BaseGridRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, dataSetTableRowPermissionsService.searchRowPermissions(request, datasetId));
    }

    @ApiOperation("有权限的对象")
    @PostMapping("/authObjs")
    public List<Object> authObjs(@RequestBody DataSetRowPermissionsDTO request) {
        return (List<Object>) dataSetTableRowPermissionsService.authObjs(request);
    }

    @ApiOperation("详情")
    @PostMapping("/dataSetRowPermissionInfo")
    public DataSetRowPermissionsDTO dataSetRowPermissionInfo(@RequestBody DataSetRowPermissionsDTO request) {
        return dataSetTableRowPermissionsService.dataSetRowPermissionInfo(request);
    }

    @ApiOperation("删除")
    @PostMapping("/delete/{id}")
    public void dataSetRowPermissionInfo(@PathVariable String id) {
        dataSetTableRowPermissionsService.delete(id);
    }

}
