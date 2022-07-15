package io.datains.controller.sys;


import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.datains.base.domain.FilePicture;
import io.datains.controller.sys.request.SysUserCreateRequest;
import io.datains.service.FilePictureService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 图片表 前端控制器
 * </p>
 *
 * @author Mr.zhang
 * @since 2022-07-11
 */
@Api(tags = "图片表 前端控制器")
@ApiSupport(order = 130)
@RestController
@RequestMapping("/api/filePicture")
public class FilePictureController {

    @Resource
    private FilePictureService filePictureService;

    @ApiOperation("上传图片")
    @PostMapping("/uploadFile")
    public boolean uploadFile(@RequestBody FilePicture filePicture) {
        filePictureService.save(filePicture);
        return true;
    }

    @ApiOperation("获取图片")
    @GetMapping("/getList")
    public List<FilePicture> getList() {
        return filePictureService.getList(null);
    }

    @ApiOperation("删除图片")
    @GetMapping("/del")
    public boolean del(@RequestParam Integer id) {
         filePictureService.del(id);
         return true;
    }

    @ApiOperation("删除图片")
    @GetMapping("/delName")
    public boolean delName(@RequestParam String name) {
        filePictureService.delName(name);
        return true;
    }

    @ApiOperation("修改图片")
    @PostMapping("/update")
    public boolean update(@RequestBody FilePicture filePicture) {
        filePictureService.update(filePicture);
        return true;
    }

    @ApiOperation("修改分组")
    @GetMapping("/updateName")
    public boolean updateName(@RequestParam String oldName,@RequestParam String newName) {
        filePictureService.updateName(oldName,newName);
        return true;
    }

}

