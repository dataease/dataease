package io.dataease.datasource.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.base.domain.Datasource;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.PageUtils;
import io.dataease.commons.utils.Pager;
import io.dataease.controller.request.DatasourceUnionRequest;
import io.dataease.controller.sys.base.BaseGridRequest;
import io.dataease.datasource.dto.DBTableDTO;
import io.dataease.datasource.service.DatasourceService;
import io.dataease.dto.DatasourceDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import java.util.List;

@Api(tags = "数据源：数据源管理")
@ApiSupport(order = 30)
@RequestMapping("datasource")
@RestController
public class DatasourceController {

    @Resource
    private DatasourceService datasourceService;

    @ApiOperation("新增数据源")
    @PostMapping("/add")
    public Datasource addDatasource(@RequestBody Datasource datasource) {
        return datasourceService.addDatasource(datasource);
    }

    @ApiOperation("验证数据源")
    @PostMapping("/validate")
    public void validate(@RequestBody Datasource datasource) throws Exception {
        datasourceService.validate(datasource);
    }

    @ApiOperation("查询当前用户数据源")
    @GetMapping("/list")
    public List<DatasourceDTO> getDatasourceList() throws Exception {
        DatasourceUnionRequest request = new DatasourceUnionRequest();
        request.setUserId(String.valueOf(AuthUtils.getUser().getUserId()));
        return datasourceService.getDatasourceList(request);
    }

    @ApiIgnore
    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<DatasourceDTO>> getDatasourceList(@RequestBody BaseGridRequest request, @PathVariable int goPage, @PathVariable int pageSize) throws Exception {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        // return PageUtils.setPageInfo(page, datasourceService.getDatasourceList(request));
        return PageUtils.setPageInfo(page, datasourceService.gridQuery(request));
    }

    @ApiOperation("删除数据源")
    @PostMapping("/delete/{datasourceID}")
    public void deleteDatasource(@PathVariable(value = "datasourceID") String datasourceID) throws Exception {
        datasourceService.deleteDatasource(datasourceID);
    }

    @ApiOperation("更新数据源")
    @PostMapping("/update")
    public void updateDatasource(@RequestBody Datasource Datasource) {
        datasourceService.updateDatasource(Datasource);
    }

    @ApiOperation("查询数据源下属所有表")
    @PostMapping("/getTables")
    public List<DBTableDTO> getTables(@RequestBody Datasource datasource) throws Exception {
        return datasourceService.getTables(datasource);
    }

    @ApiIgnore
    @PostMapping("/getSchema")
    public List<String> getSchema(@RequestBody Datasource datasource) throws Exception {
       return datasourceService.getSchema(datasource);
    }


}
