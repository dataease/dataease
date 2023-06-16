package io.dataease.api.permissions.auth.api;


import io.dataease.api.permissions.auth.dto.BusiResourceCreator;
import io.dataease.api.permissions.auth.dto.BusiResourceEditor;
import io.dataease.api.permissions.auth.vo.BusiPerVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

public interface InteractiveAuthApi {

    @GetMapping("/menuIds")
    List<Long> menuIds();


    @GetMapping("/resource/{flag}")
    List<BusiPerVO> resource(@PathVariable("flag") String flag);

    /**
     * 下面3个接口为内部调用接口不对外开放
     *
     * @param creator
     */
    @ApiIgnore
    @ApiOperation("保存业务资源")
    @PostMapping("/resource/create")
    void saveResource(@RequestBody BusiResourceCreator creator);

    @ApiIgnore
    @ApiOperation("更新业务资源")
    @PostMapping("/resource/edit")
    void editResource(@RequestBody BusiResourceEditor editor);

    @ApiIgnore
    @ApiOperation("删除业务资源")
    @GetMapping("/resource/del/{id}")
    void delResource(@PathVariable("id") Long id);

}
