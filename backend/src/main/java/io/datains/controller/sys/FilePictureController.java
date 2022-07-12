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
    public void uploadFile(@RequestBody FilePicture filePicture) {
        filePictureService.save(filePicture);
    }

    @ApiOperation("获取图片")
    @GetMapping("/getList")
    public List<FilePicture> getList() {
        return filePictureService.getList(null);
    }

}

