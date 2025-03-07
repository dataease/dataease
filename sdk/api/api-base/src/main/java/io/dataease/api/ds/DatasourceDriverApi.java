package io.dataease.api.ds;


import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.ds.vo.DriveDTO;
import io.dataease.api.ds.vo.DriveJarDTO;
import io.dataease.extensions.datasource.dto.DatasourceDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
@Tag(name = "数据源管理:驱动")
@ApiSupport(order = 968)
public interface DatasourceDriverApi {
    /**
     * 查询数据源树
     * @param keyWord 过滤关键字
     * @return
     */
    @Operation(summary = "查询数据源树", hidden = true)
    @GetMapping("/query/{keyWord}")
    List<DatasourceDTO> query(@PathVariable("keyWord") String keyWord);

    @Operation(summary = "列表")
    @GetMapping("/list")
    List<DriveDTO> list();

    @Operation(summary = "根据数据源类型获取")
    @GetMapping("/list/{dsType}")
    List<DriveDTO> listByDsType(@PathVariable("dsType") String dsType);

    @Operation(summary = "保存")
    @PostMapping("/save")
    DriveDTO save(@RequestBody DriveDTO datasourceDrive);

    @Operation(summary = "更新")
    @PostMapping("/update")
    DriveDTO update(@RequestBody DriveDTO datasourceDrive);

    @Operation(summary = "删除")
    @PostMapping("/delete/{driverId}")
    void delete(@PathVariable("driverId") String driverId);

    @Operation(summary = "获取驱动jar列表")
    @GetMapping("/listDriverJar/{driverId}")
    List<DriveJarDTO> listDriverJar(@PathVariable("driverId") String driverId);

    @Operation(summary = "删除驱动jar")
    @PostMapping("/deleteDriverJar/{jarId}")
    void deleteDriverJar(@PathVariable("jarId") String jarId);

    @Operation(summary = "上传驱动jar")
    @PostMapping("/uploadJar")
    DriveJarDTO uploadJar(@RequestParam("deDriverId") String deDriverId, @RequestParam("jarFile") MultipartFile jarFile) throws Exception;
}
