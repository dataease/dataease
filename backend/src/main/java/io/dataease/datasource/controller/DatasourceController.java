package io.dataease.datasource.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.dataease.base.domain.Datasource;
import io.dataease.commons.utils.PageUtils;
import io.dataease.commons.utils.Pager;
import io.dataease.controller.sys.base.BaseGridRequest;
import io.dataease.datasource.dto.DBTableDTO;
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
    public Datasource addDatasource(@RequestBody Datasource datasource) {
        return datasourceService.addDatasource(datasource);
    }

    @PostMapping("/validate")
    public void validate(@RequestBody Datasource datasource) throws Exception {
        datasourceService.validate(datasource);
    }

    @GetMapping("/list")
    public List<Datasource> getDatasourceList() throws Exception {
        return datasourceService.getDatasourceList(new Datasource());
    }

    @PostMapping("/list/{goPage}/{pageSize}")
    public Pager<List<Datasource>> getDatasourceList(@RequestBody BaseGridRequest request, @PathVariable int goPage, @PathVariable int pageSize) throws Exception {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        // return PageUtils.setPageInfo(page, datasourceService.getDatasourceList(request));
        return PageUtils.setPageInfo(page, datasourceService.gridQuery(request));
    }

    @PostMapping("/delete/{datasourceID}")
    public void deleteDatasource(@PathVariable(value = "datasourceID") String datasourceID) {
        datasourceService.deleteDatasource(datasourceID);
    }

    @PostMapping("/update")
    public void updateDatasource(@RequestBody Datasource Datasource) {
        datasourceService.updateDatasource(Datasource);
    }

    @PostMapping("/getTables")
    public List<DBTableDTO> getTables(@RequestBody Datasource datasource) throws Exception {
        return datasourceService.getTables(datasource);
    }
}
