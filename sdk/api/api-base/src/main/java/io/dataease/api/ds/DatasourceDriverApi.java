package io.dataease.api.ds;


import io.dataease.api.ds.vo.DatasourceDTO;
import io.dataease.api.ds.vo.DriveDTO;
import io.dataease.api.ds.vo.DriveJarDTO;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DatasourceDriverApi {
    /**
     * 查询数据源树
     * @param keyWord 过滤关键字
     * @return
     */
    @GetMapping("/query/{keyWord}")
    List<DatasourceDTO> query(@PathVariable("keyWord") String keyWord);

    @GetMapping("/list")
    List<DriveDTO> list();

    @GetMapping("/list/{dsType}")
    List<DriveDTO> listByDsType(@PathVariable("dsType") String dsType);

    @PostMapping("/save")
    DriveDTO save(@RequestBody DriveDTO datasourceDrive);

    @PostMapping("/update")
    DriveDTO update(@RequestBody DriveDTO datasourceDrive);

    @DeleteMapping("/delete/{driverId}")
    void delete(@PathVariable("driverId") String driverId);

    @GetMapping("/listDriverJar/{driverId}")
    List<DriveJarDTO> listDriverJar(@PathVariable("driverId") String driverId);

    @DeleteMapping("/deleteDriverJar/{jarId}")
    void deleteDriverJar(@PathVariable("jarId") String jarId);

    @PostMapping("/uploadJar")
    DriveJarDTO uploadJar(@RequestParam("deDriverId") String deDriverId, @RequestParam("jarFile") MultipartFile jarFile) throws Exception;
}
