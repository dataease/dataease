package io.dataease.api.exportCenter;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.exportCenter.vo.ExportTaskDTO;
import io.dataease.auth.DeApiPath;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import static io.dataease.constant.AuthResourceEnum.DATASOURCE;

@Tag(name = "数据导出中心")
@ApiSupport(order = 971)
@DeApiPath(value = "/exportCenter", rt = DATASOURCE)
public interface ExportCenterApi {


    @PostMapping("/exportTasks/{status}")
    public List<ExportTaskDTO> exportTasks(@PathVariable String status) ;

    @GetMapping("/delete/{id}")
    public void  delete(@PathVariable String id);

    @PostMapping("/delete")
    public void  delete(@RequestBody List<String> ids);

    @PostMapping("/deleteAll/{type}")
    public void  deleteAll(@PathVariable String type);

    @GetMapping("/download/{id}")
    public void download(@PathVariable String id, HttpServletResponse response) throws Exception ;

    @PostMapping("/retry/{id}")
    public void  retry(@PathVariable String id);

}
