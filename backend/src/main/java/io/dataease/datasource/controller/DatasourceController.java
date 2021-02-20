package io.dataease.datasource.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.dataease.base.domain.Datasource;
import io.dataease.commons.utils.PageUtils;
import io.dataease.commons.utils.Pager;
import io.dataease.datasource.service.DatasourceService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("datasource")
@RestController
public class DatasourceController {

    @Resource
    private DatasourceService datasourceService;

    @PostMapping("/add")
    public Datasource addDatasource(@RequestBody Datasource Datasource) {
        return datasourceService.addDatasource(Datasource);
    }

    @PostMapping("/validate")
    public void validate(@RequestBody Datasource Datasource) throws Exception{
        datasourceService.validate(Datasource);
    }

    @GetMapping("/list")
    public List<Datasource> getDatasourceList() throws Exception {
        return datasourceService.getDatasourceList(new Datasource());
    }

    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<Datasource>> getDatasourceList(@RequestBody Datasource request, @PathVariable int goPage, @PathVariable int pageSize) throws Exception{
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, datasourceService.getDatasourceList(request));
    }

    @GetMapping("/delete/{datasourceID}")
    public void deleteDatasource(@PathVariable(value = "datasourceID") String datasourceID) {
        datasourceService.deleteDatasource(datasourceID);
    }

    @PostMapping("/update")
    public void updateDatasource(@RequestBody Datasource Datasource) {
        datasourceService.updateDatasource(Datasource);
    }
}
